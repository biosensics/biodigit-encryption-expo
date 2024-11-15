package com.biosensics.biodigitencryptionexpo

import java.util.Base64
import java.util.regex.Pattern
import org.apache.commons.compress.archivers.tar.TarArchiveEntry
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream
import java.io.ByteArrayOutputStream
import java.io.ByteArrayInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.io.InputStream
import java.io.OutputStream
import java.io.PipedInputStream
import java.io.PipedOutputStream

import org.bson.BsonDocument
import org.bson.BsonString
import org.bson.BsonWriter
import org.bson.io.BasicOutputBuffer
import org.bson.codecs.BsonDocumentCodec
import org.bson.codecs.EncoderContext
import org.bson.codecs.DecoderContext
import org.bson.io.ByteBufferBsonInput
import org.bson.BsonBinaryReader

fun b64UUID(uuid: String): String {
    // Lowercase the UUID
    val lowerUuid = uuid.lowercase()

    // Check if the string is a valid UUID
    if (!isUUID(lowerUuid)) {
        throw IllegalArgumentException("Invalid UUID")
    }

    // Remove dashes from the UUID
    val strippedUuid = lowerUuid.replace("-", "")

    // Decode the hex string into bytes
    val bytes = strippedUuid.chunked(2).map { it.toInt(16).toByte() }.toByteArray()

    // Encode the bytes into a Base64 string
    val b64ID = Base64.getEncoder().encodeToString(bytes)

    // Convert the Base64 string to the custom S64 format
    return b64toS64(b64ID)
}

fun isUUID(uuid: String): Boolean {
    val uuidRegex =
        Pattern.compile("^[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$")
    return uuidRegex.matcher(uuid).matches()
}

fun b64toS64(str: String): String {
    return str.replace("/", "%").replace("=", "")
}

fun unB64UUID(id: String): String {
    // Check if the string is a valid Base64 UUID
    if (!isB64UUID(id)) {
        throw IllegalArgumentException("Invalid Base64 UUID")
    }

    // Convert S64 format back to Base64
    val b64ID = s64toB64(id)

    // Decode the Base64 string into bytes
    val bytes = Base64.getDecoder().decode(b64ID)

    // Convert bytes to a hexadecimal string
    val hexID = bytes.joinToString("") { "%02x".format(it) }

    // Format the hexadecimal string into UUID format
    val uuid = "${hexID.substring(0, 8)}-${hexID.substring(8, 12)}-${hexID.substring(12, 16)}-${hexID.substring(16, 20)}-${hexID.substring(20)}"

    // Validate the formatted UUID
    if (!isUUID(uuid)) {
        throw IllegalArgumentException("Invalid UUID")
    }

    return uuid
}

fun isB64UUID(uuid: String): Boolean {
    val b64UUIDRegex = Pattern.compile("^[A-Za-z0-9%+]{22}$")
    return b64UUIDRegex.matcher(uuid).matches()
}

fun s64toB64(str: String): String {
    // Replace '%' with '/' and pad with '=' to make length a multiple of 4
    return str.replace("%", "/") + "=".repeat((4 - (str.length % 4)) % 4)
}

fun tarFile(inputStream: InputStream, filename: String, fileSize: Long, outputStream: OutputStream) {
    val tarOutput = TarArchiveOutputStream(outputStream)

    // Create the TAR entry with the provided size
    val entry = TarArchiveEntry(filename)
    entry.size = fileSize
    tarOutput.putArchiveEntry(entry)

    // Write the file content in chunks
    val buffer = ByteArray(16 * 1024) // Chunk size of 16 KB
    var bytesRead: Int
    while (inputStream.read(buffer).also { bytesRead = it } != -1) {
        tarOutput.write(buffer, 0, bytesRead)
    }

    tarOutput.closeArchiveEntry()
    tarOutput.close()
}

// BSON serialization using org.bson
fun getBsonHeader(messageId: String): ByteArray {
    val buffer = BasicOutputBuffer()
    val writer: BsonWriter = org.bson.BsonBinaryWriter(buffer)
    val codec = BsonDocumentCodec()

    // Create a BSON document for the header
    val bsonDocument = BsonDocument()
    bsonDocument.put("v", BsonString("1"))
    bsonDocument.put("mid", BsonString(b64UUID(messageId)))

    // Serialize the document
    codec.encode(
        writer,
        bsonDocument,
        EncoderContext.builder().isEncodingCollectibleDocument(true).build()
    )
    return buffer.toByteArray()
}

fun pipeTransforms(
    inputStream: InputStream,
    outputStream: OutputStream,
    transforms: List<(InputStream, OutputStream) -> Unit>
) {
    require(transforms.isNotEmpty()) { "Transforms list must not be empty" }

    // Start with the initial input stream
    var currentInput = inputStream

    // Iterate through the transforms, chaining them with Piped Streams
    val threads = mutableListOf<Thread>()

    transforms.forEachIndexed { index, transform ->
        // Create a PipedStream for each transform (except the last one, which just outputs to the outputStream)
        val scopedCurrentInput = currentInput
        val pipedInput = if (index == transforms.size - 1) null else PipedInputStream() // this will be the input for the next transform
        val pipedOutput = if (index == transforms.size - 1) outputStream else PipedOutputStream(pipedInput!!) // this is the output for the current transform


        val thread = Thread {
            try {
                transform(scopedCurrentInput, pipedOutput)
            } finally {
                pipedOutput.close()
            }
        }
        threads.add(thread)
        thread.start()

        currentInput = pipedInput ?: currentInput
    }

    // Wait for all threads to finish
    threads.forEach { it.join() }
}

fun encryptFile(file: ByteArray, filename: String, messageId: String, key: SymKey): ByteArray {
    val fileSize = file.size.toLong() // Determine the size of the file
    val tarOutputStream = ByteArrayOutputStream()

    // Create a TAR file with the provided file, filename, and file size
    tarFile(
        inputStream = ByteArrayInputStream(file),
        filename = filename,
        fileSize = fileSize,
        outputStream = tarOutputStream
    )

    // Serialize the header to BSON
    val bsonHeader = getBsonHeader(messageId)

    // Calculate the BSON header length
    val bsonLength = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(bsonHeader.size).array()

    // Encrypt the TAR file
    val tarInputStream = ByteArrayInputStream(tarOutputStream.toByteArray())
    val encryptedTarOutputStream = ByteArrayOutputStream()
    key.encryptStream(tarInputStream, encryptedTarOutputStream)

    // Create the final output
    val output = ByteArrayOutputStream()
    output.write("SEALD.IO_".toByteArray(Charsets.UTF_8)) // Write the prefix
    output.write(bsonLength) // Write the BSON header length
    output.write(bsonHeader) // Write the BSON header
    output.write(encryptedTarOutputStream.toByteArray()) // Write the encrypted TAR file

    return output.toByteArray()
}

fun encryptFilePiped(file: ByteArray, filename: String, messageId: String, key: SymKey): ByteArray {
    val fileSize = file.size.toLong() // Determine the size of the file

    // Serialize the header to BSON
    val bsonHeader = getBsonHeader(messageId)

    // Calculate the BSON header length
    val bsonLength =
        ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(bsonHeader.size).array()

    // Create the final output
    val output = ByteArrayOutputStream()
    output.write("SEALD.IO_".toByteArray(Charsets.UTF_8)) // Write the prefix
    output.write(bsonLength) // Write the BSON header length
    output.write(bsonHeader) // Write the BSON header

    pipeTransforms(
        ByteArrayInputStream(file), output, listOf(
            { inputStream, outputStream ->
                tarFile(
                    inputStream = inputStream,
                    filename = filename,
                    fileSize = fileSize,
                    outputStream = outputStream
                )
            },
            { inputStream, outputStream -> key.encryptStream(inputStream, outputStream) })
    )

    return output.toByteArray()
}

data class ClearFile(
    val filename: String,
    val sessionId: String,
    val fileContent: ByteArray
)

fun parseFileHeader(fileReader: ByteArrayInputStream): String {
    val initString = ByteArray(9)
    if (fileReader.read(initString) != 9 || !initString.contentEquals("SEALD.IO_".toByteArray(Charsets.UTF_8))) {
        throw IllegalArgumentException("Error: Invalid file header")
    }

    val bsonLengthBytes = ByteArray(4)
    if (fileReader.read(bsonLengthBytes) != 4) {
        throw IllegalArgumentException("Error: Invalid BSON length")
    }

    val bsonLength = ByteBuffer.wrap(bsonLengthBytes).order(ByteOrder.LITTLE_ENDIAN).int
    val headerBuff = ByteArray(bsonLength)
    if (fileReader.read(headerBuff) != bsonLength) {
        throw IllegalArgumentException("Error: Failed to read BSON header")
    }

    // Use a ByteBuffer to create the BsonBinaryReader
    val byteBuffer = ByteBuffer.wrap(headerBuff)
    val bsonInput = ByteBufferBsonInput(org.bson.ByteBufNIO(byteBuffer))
    val bsonReader = BsonBinaryReader(bsonInput)

    val codec = BsonDocumentCodec()
    val bsonDocument = codec.decode(bsonReader, DecoderContext.builder().build())

    val messageId = bsonDocument["mid"]?.asString()?.value ?: throw IllegalArgumentException("Error: Missing messageId in header")
    return unB64UUID(messageId)
}

fun unTarFile(inputStream: InputStream, outputStream: OutputStream): String {
    val tarInput = TarArchiveInputStream(inputStream)
    val entry: TarArchiveEntry = tarInput.nextTarEntry ?: throw IllegalArgumentException("Error: TAR file is empty")
    val buffer = ByteArray(16 * 1024) // Chunk size of 16 KB
    var bytesRead: Int
    while (tarInput.read(buffer).also { bytesRead = it } != -1) {
        outputStream.write(buffer, 0, bytesRead)
    }
    tarInput.close()
    return entry.name // Return the file name from the TAR entry
}

fun decryptFile(file: ByteArray, key: SymKey): ClearFile {
    // Wrap the input file ByteArray into a ByteArrayInputStream for streaming
    val fileInputStream = ByteArrayInputStream(file)

    // Parse the header to get the session ID (message ID)
    val sessionId = parseFileHeader(fileInputStream)

    if (fileInputStream.available() == 0) {
        throw IllegalArgumentException("Error: Unexpected EOF while reading file")
    }

    // Decrypt the remaining encrypted data
    val decryptedTarOutputStream = ByteArrayOutputStream()
    key.decryptStream(fileInputStream, decryptedTarOutputStream)

    // Extract the TAR file contents
    val clearDataOutputStream = ByteArrayOutputStream()
    val clearFilename = unTarFile(
        inputStream = ByteArrayInputStream(decryptedTarOutputStream.toByteArray()),
        outputStream = clearDataOutputStream
    )

    return ClearFile(
        filename = clearFilename,
        sessionId = sessionId,
        fileContent = clearDataOutputStream.toByteArray()
    )
}
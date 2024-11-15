package com.biosensics.biodigitencryptionexpo

import java.io.InputStream
import java.io.OutputStream
import javax.crypto.Cipher
import javax.crypto.Mac
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec
import java.security.SecureRandom
import java.util.Base64

class SymKey(val key: ByteArray) {
    val keySize: Int = key.size * 8

    init {
        require(keySize == 128 || keySize == 192 || keySize == 256) { "INVALID_ARG : Key size is invalid" }
    }

    companion object {
        // Generate a new AES key
        fun generate(size: Int = 256): SymKey {
            val key = ByteArray(size / 8)
            SecureRandom().nextBytes(key)
            return SymKey(key)
        }

        // Create AES key from a Base64 string
        fun fromB64(messageKey: String): SymKey {
            return SymKey(Base64.getDecoder().decode(messageKey))
        }
    }

    // Utility method to calculate HMAC
    private fun calculateHMAC(data: ByteArray): ByteArray {
        val mac = Mac.getInstance("HmacSHA256")
        mac.init(SecretKeySpec(key, "HmacSHA256"))
        return mac.doFinal(data)
    }

    // Encode key to Base64 for storage or transmission
    fun toB64(): String = Base64.getEncoder().encodeToString(key)


    // Byte array encryption method
    fun encrypt(clearText: ByteArray): ByteArray {
        val iv = ByteArray(16)
        SecureRandom().nextBytes(iv)

        // Initialize cipher for encryption
        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        cipher.init(Cipher.ENCRYPT_MODE, SecretKeySpec(key, "AES"), IvParameterSpec(iv))

        // Encrypt data
        val cipherText = cipher.doFinal(clearText)

        // Calculate HMAC
        val hmac = calculateHMAC(iv + cipherText)

        // Return concatenated IV, encrypted data, and HMAC
        return iv + cipherText + hmac
    }

    // Byte array decryption method
    fun decrypt(cipheredMessage: ByteArray): ByteArray {
        val iv = cipheredMessage.copyOfRange(0, 16)
        val cipherText = cipheredMessage.copyOfRange(16, cipheredMessage.size - 32)
        val hmac = cipheredMessage.copyOfRange(cipheredMessage.size - 32, cipheredMessage.size)

        // Verify HMAC
        if (!calculateHMAC(iv + cipherText).contentEquals(hmac)) {
            throw IllegalArgumentException("INVALID_HMAC")
        }

        // Initialize cipher for decryption
        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        cipher.init(Cipher.DECRYPT_MODE, SecretKeySpec(key, "AES"), IvParameterSpec(iv))

        // Decrypt data
        return cipher.doFinal(cipherText)
    }


    // Stream encryption method
    fun encryptStream(inputStream: InputStream, outputStream: OutputStream) {
        val iv = ByteArray(16)
        SecureRandom().nextBytes(iv)
        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        cipher.init(Cipher.ENCRYPT_MODE, SecretKeySpec(key, "AES"), IvParameterSpec(iv))

        // Write the IV to the output stream first
        outputStream.write(iv)

        // Wrap the output stream with HMAC calculation
        val hmac = Mac.getInstance("HmacSHA256")
        hmac.init(SecretKeySpec(key, "HmacSHA256"))
        hmac.update(iv) // add IV to HMAC

        // Use CipherOutputStream to handle encryption
        val buffer = ByteArray(16 * 1024)
        var bytesRead: Int
        while (inputStream.read(buffer).also { bytesRead = it } != -1) {
            val chunk = cipher.update(buffer, 0, bytesRead)
            outputStream.write(chunk)
            hmac.update(chunk)
        }
        val chunk = cipher.doFinal()
        outputStream.write(chunk)
        hmac.update(chunk)

        // Append HMAC to the encrypted output
        outputStream.write(hmac.doFinal())
        outputStream.flush()
    }

    // Stream decryption method
    fun decryptStream(inputStream: InputStream, outputStream: OutputStream) {
        // Read the IV from the input stream
        val iv = ByteArray(16)
        if (inputStream.read(iv) != iv.size) throw IllegalArgumentException("Invalid IV size")

        val hmac = Mac.getInstance("HmacSHA256")
        hmac.init(SecretKeySpec(key, "HmacSHA256"))
        hmac.update(iv)

        // Cipher for decryption
        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        cipher.init(Cipher.DECRYPT_MODE, SecretKeySpec(key, "AES"), IvParameterSpec(iv))

        // Use a buffer to keep track of the last 32 bytes for HMAC
        val hmacBuffer = ByteArray(32)
        if (inputStream.read(hmacBuffer) != hmacBuffer.size) throw IllegalArgumentException("Stream too small for HMAC")

        val buffer = ByteArray(1024)
        var bytesRead: Int
        // Main loop for decryption, leaving the last 32 bytes in hmacBuffer
        while (inputStream.read(buffer).also { bytesRead = it } != -1) {
            // Buffer with what was previously in hmacBuffer + the current chunk
            val actualBuffer = ByteArray(bytesRead + 32)
            System.arraycopy(hmacBuffer, 0, actualBuffer, 0, 32)
            System.arraycopy(buffer, 0, actualBuffer, 32, bytesRead)

            // Copy the last 32 bytes from this buffer into hmacBuffer for later
            System.arraycopy(actualBuffer, bytesRead, hmacBuffer, 0, 32)

            // Update HMAC with the current chunk (without the last 32 bytes that we keep as possible HMAC)
            hmac.update(actualBuffer, 0, bytesRead)

            // Update cipher with the current chunk (without the last 32 bytes that we keep as possible HMAC)
            val chunk = cipher.update(actualBuffer, 0, bytesRead)
            outputStream.write(chunk)
        }

        // Process the final chunk from the cipher
        val finalChunk = cipher.doFinal()
        outputStream.write(finalChunk)

        // Verify HMAC after reading all encrypted content
        val computedHmac = hmac.doFinal()
        if (!computedHmac.contentEquals(hmacBuffer)) {
            throw IllegalArgumentException("INVALID_HMAC")
        }

        outputStream.flush()
    }
}
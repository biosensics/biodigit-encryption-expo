package com.biosensics.biodigitencryptionexpo

import expo.modules.kotlin.modules.Module
import expo.modules.kotlin.modules.ModuleDefinition
import expo.modules.kotlin.Promise
import expo.modules.kotlin.exception.toCodedException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.util.UUID

class BioDigitEncryptionExpoModule : Module() {
  override fun definition() = ModuleDefinition {
    Name("BioDigitEncryptionExpo")


    AsyncFunction("encrypt") { session: String, path: String, promise: Promise ->
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // get aq byte array from the session
                val sessionByteArray = session.toByteArray()

                // make a new SymKey instance
                val key = SymKey(sessionByteArray)

                val fileName = path.substringAfterLast("/")

                // get the file path as a byte array
                val fileStream = File(path).readBytes()

                // encrypt the path
                val encrypted = encryptFile(fileStream, fileName, UUID.randomUUID().toString(), key)

                promise.resolve(encrypted)
            } catch (e: Exception) {
                promise.reject(e.toCodedException())
            }
        }
    }
  }
}

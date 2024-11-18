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


    AsyncFunction("encrypt") { path: String, keyB64: String, promise: Promise ->
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // make a new SymKey instance
                val key = SymKey.fromB64(keyB64)

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

package com.biosensics.biodigitencryptionexpo

import expo.modules.kotlin.modules.Module
import expo.modules.kotlin.modules.ModuleDefinition
import expo.modules.kotlin.Promise

class BioDigitEncryptionExpoModule : Module() {
  override fun definition() = ModuleDefinition {
    Name("BioDigitEncryptionExpo")

    AsyncFunction("encrypt") { session: String, path: String, promise: Promise ->
      promise.resolve("stub")
    }
  }
}

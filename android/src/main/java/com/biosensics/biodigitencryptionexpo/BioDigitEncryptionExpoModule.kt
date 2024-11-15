package com.biosensics.biodigitencryptionexpo

import expo.modules.kotlin.modules.Module
import expo.modules.kotlin.modules.ModuleDefinition

class BioDigitEncryptionExpoModule : Module() {
  override fun definition() = ModuleDefinition {
    Name("BioDigitEncryptionExpo")

    Function("getTheme") {
      return@Function "system"
    }
  }
}

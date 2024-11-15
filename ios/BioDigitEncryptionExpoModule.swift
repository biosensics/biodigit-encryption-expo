import ExpoModulesCore

public class BioDigitEncryptionExpoModule: Module {
  public func definition() -> ModuleDefinition {
    Name("BioDigitEncryptionExpo")

    Function("getTheme") { () -> String in
      "system"
    }
  }
}

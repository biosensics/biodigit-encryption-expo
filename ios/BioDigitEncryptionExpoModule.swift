import ExpoModulesCore

public class BioDigitEncryptionExpoModule: Module {
  public func definition() -> ModuleDefinition {
    Name("BioDigitEncryptionExpo")

    AsyncFunction("encrypt") { (session: String, path: String, promise: Promise) -> String in
      promise.resolve("stub")
    }
  }
}

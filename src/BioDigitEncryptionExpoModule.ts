import { NativeModule, requireNativeModule } from 'expo';

declare class BioDigitEncryptionExpoModule extends NativeModule {
}

// This call loads the native module object from the JSI.
export default requireNativeModule<BioDigitEncryptionExpoModule>('BioDigitEncryptionExpo');

import { NativeModule, requireNativeModule } from 'expo';

import { BioDigitEncryptionExpoModuleEvents } from './BioDigitEncryptionExpo.types';

declare class BioDigitEncryptionExpoModule extends NativeModule<BioDigitEncryptionExpoModuleEvents> {
  PI: number;
  hello(): string;
  setValueAsync(value: string): Promise<void>;
}

// This call loads the native module object from the JSI.
export default requireNativeModule<BioDigitEncryptionExpoModule>('BioDigitEncryptionExpo');

import { NativeModule, requireNativeModule } from 'expo';

import {encrypt} from './types';
declare class BioDigitEncryptionExpoModule extends NativeModule {
 encrypt: encrypt;
}

// This call loads the native module object from the JSI.
export default requireNativeModule<BioDigitEncryptionExpoModule>('BioDigitEncryptionExpo');

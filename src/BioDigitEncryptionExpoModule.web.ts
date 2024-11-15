import { registerWebModule, NativeModule } from 'expo';

import { BioDigitEncryptionExpoModuleEvents } from './BioDigitEncryptionExpo.types';

class BioDigitEncryptionExpoModule extends NativeModule<BioDigitEncryptionExpoModuleEvents> {
  PI = Math.PI;
  async setValueAsync(value: string): Promise<void> {
    this.emit('onChange', { value });
  }
  hello() {
    return 'Hello world! 👋';
  }
}

export default registerWebModule(BioDigitEncryptionExpoModule);

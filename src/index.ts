import BioDigitEncryptionExpoModule from './BioDigitEncryptionExpoModule';

export function encrypt(session: string, path: string): Promise<string> {
  return BioDigitEncryptionExpoModule.encrypt(session, path);
}
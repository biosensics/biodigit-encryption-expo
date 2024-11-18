import BioDigitEncryptionExpoModule from './BioDigitEncryptionExpoModule';

export function encrypt(path: string, session: {_sessionSymKey: {toB64: () => string}}): Promise<string> {
  return BioDigitEncryptionExpoModule.encrypt(path, session._sessionSymKey.toB64());
}

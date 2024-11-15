// Reexport the native module. On web, it will be resolved to BioDigitEncryptionExpoModule.web.ts
// and on native platforms to BioDigitEncryptionExpoModule.ts
export { default } from './BioDigitEncryptionExpoModule';
export { default as BioDigitEncryptionExpoView } from './BioDigitEncryptionExpoView';
export * from  './BioDigitEncryptionExpo.types';

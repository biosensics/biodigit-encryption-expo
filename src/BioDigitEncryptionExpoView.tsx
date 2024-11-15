import { requireNativeView } from 'expo';
import * as React from 'react';

import { BioDigitEncryptionExpoViewProps } from './BioDigitEncryptionExpo.types';

const NativeView: React.ComponentType<BioDigitEncryptionExpoViewProps> =
  requireNativeView('BioDigitEncryptionExpo');

export default function BioDigitEncryptionExpoView(props: BioDigitEncryptionExpoViewProps) {
  return <NativeView {...props} />;
}

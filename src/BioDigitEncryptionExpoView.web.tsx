import * as React from 'react';

import { BioDigitEncryptionExpoViewProps } from './BioDigitEncryptionExpo.types';

export default function BioDigitEncryptionExpoView(props: BioDigitEncryptionExpoViewProps) {
  return (
    <div>
      <iframe
        style={{ flex: 1 }}
        src={props.url}
        onLoad={() => props.onLoad({ nativeEvent: { url: props.url } })}
      />
    </div>
  );
}

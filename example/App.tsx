import { Buffer } from '@craftzdog/react-native-buffer';
global.Buffer = Buffer as any;

import {encrypt} from 'biodigit-encryption-expo';
import {AnonymousEncryptionSession} from '@seald-io/sdk/lib/anonymous.js';
import { useCallback, useMemo, useState } from 'react';
import { Button, View } from 'react-native';
import { getSealdSDKInstance } from './seald';

export default function App() {
  const encryptionToken = process.env.EXPO_PUBLIC_GET_KEYS_TOKEN
  const getKeysToken = process.env.EXPO_PUBLIC_GET_KEYS_TOKEN
  const sealdGroupId = process.env.EXPO_PUBLIC_SEALD_ID

  const sealdAnonSdk = useMemo(() => getSealdSDKInstance(), []);
  const [anonSession, setAnonSession] = useState<AnonymousEncryptionSession | null>(null);


  const encryptFile = useCallback(async () => {
    try {
    console.log('getting session');
    let currentSession = anonSession;
    if (!currentSession) {
      if (!encryptionToken || !getKeysToken || !sealdGroupId) {
        throw new Error('missing envs');
      }

      currentSession = await sealdAnonSdk.createEncryptionSession({
        encryptionToken,
        getKeysToken,
        recipients: {sealdIds: [sealdGroupId]},
      });

      console.log('session created', currentSession);
      setAnonSession(currentSession);
    }
    
    console.log('encrypting file');

    const encrypted = await encrypt('ile:///data/user/0/com.biosensics.biodigitencryptionexpo.example/files/test_file.txt', currentSession._sessionSymKey.key.toString('base64'));


    console.log('encrypted', encrypted);
    } catch (e: any) {
      console.error(e);
      console.error(e.message);
    }
  }, []);

  return (
    <View style={{ flex: 1, alignItems: 'center', justifyContent: 'center' }}>
      <Button title={'Encrypt'} onPress={encryptFile} />
    </View>
  );
}
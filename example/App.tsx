import {encrypt} from 'biodigit-encryption-expo';
import { useEffect, useState } from 'react';
import { Text, View } from 'react-native';

export default function App() {
  const [encryptResult, setEncryptResult] = useState<string | null>(null);

  useEffect(() => {
    (async () => {
      const result = await encrypt('stub', 'stub');
      setEncryptResult(result);
    })()
  } , []);

  return (
    <View style={{ flex: 1, alignItems: 'center', justifyContent: 'center' }}>
      <Text>Encrypt Result: {encryptResult}</Text>
    </View>
  );
}
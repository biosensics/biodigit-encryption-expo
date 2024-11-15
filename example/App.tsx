import * as BioDigitEncryption from 'biodigit-encryption-expo';
import { Text, View } from 'react-native';

export default function App() {
  return (
    <View style={{ flex: 1, alignItems: 'center', justifyContent: 'center' }}>
      <Text>Theme: {BioDigitEncryption.getTheme()}</Text>
    </View>
  );
}
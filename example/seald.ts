// https://docs.seald.io/en/sdk/guides/9-anonymous-encryption.html#react-native
import type {AnonymousSDK} from '@seald-io/sdk/lib/anonymous.js'; //type import
import AnonymousSDKBuilder from '@seald-io/sdk/react-native/anonymous-sdk-react-native.bundle.js'; // explicit import
import 'react-native-quick-crypto';
import type {SSCrypto} from 'sscrypto';
import * as sscrypto from 'sscrypto/node';

const sealdApiUrl = process.env.EXPO_PUBLIC_SEALD_API_URL as string;

// Validate that the Seald envs are set
if (!sealdApiUrl) {
  throw new Error('Seald envs not set');
}

let anonymousSDK: AnonymousSDK;

if (sealdApiUrl) {
  anonymousSDK = AnonymousSDKBuilder({
    apiURL: sealdApiUrl,
    sscrypto: sscrypto as SSCrypto,
  });
}

export const getSealdSDKInstance = (): AnonymousSDK => anonymousSDK;

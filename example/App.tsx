import { Buffer } from '@craftzdog/react-native-buffer'

global.Buffer = Buffer as any

import { encrypt } from 'biodigit-encryption-expo'
import { useCallback, } from 'react'
import { Button, View } from 'react-native'

import * as sscrypto from 'sscrypto/node'
import * as RNFS from 'react-native-fs'
import * as QuickB64 from 'react-native-quick-base64'

export default function App () {
  const encryptFile = useCallback(async () => {
    try {
      const key = sscrypto.utils.randomBytes(64).toString('base64')
      const fakeSession = {
        _sessionSymKey: {
          toB64 () {
            return key
          }
        }
      }

      console.log('Generating file data (1MB)...')
      const fileData = sscrypto.utils.randomBytes(1024 * 1024)
      const fileDataB64 = QuickB64.fromByteArray(fileData)

      console.log('Writing file data (10MB)...')
      await RNFS.unlink(RNFS.TemporaryDirectoryPath + '/test-data/').catch(() => {})
      await RNFS.mkdir(RNFS.TemporaryDirectoryPath + '/test-data/')
      for (let i = 0; i < 10; i++) {
        await RNFS.appendFile(RNFS.TemporaryDirectoryPath + '/test-data/file.bin', fileDataB64, 'base64')
      }

      console.log('encrypting file')
      const encrypted = await encrypt(RNFS.TemporaryDirectoryPath + '/test-data/file.bin', fakeSession)

      console.log('encrypted', encrypted.length)
    } catch (e: any) {
      console.error(e)
      console.error(e.message)
    }
  }, [])

  return (
    <View
      style={{ flex: 1, alignItems: 'center', justifyContent: 'center' }}>
      <Button
        title={'Encrypt'}
        onPress={encryptFile} />
    </View>
  )
}

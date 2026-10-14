import { useState } from 'react';
import {
  Alert,
  Button,
  Linking,
  Platform,
  StatusBar,
  StyleSheet,
  Text,
  View,
} from 'react-native';
import { SafeAreaProvider, SafeAreaView } from 'react-native-safe-area-context';

function App() {
  const [openingSettings, setOpeningSettings] = useState(false);

  async function openNotificationAccessSettings() {
    setOpeningSettings(true);
    try {
      await Linking.sendIntent(
        'android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS',
      );
    } catch {
      Alert.alert(
        '설정 화면을 열 수 없습니다',
        '휴대폰 설정에서 알림 접근 권한을 검색해 CardNotificationApp을 허용해 주세요.',
      );
    } finally {
      setOpeningSettings(false);
    }
  }

  return (
    <SafeAreaProvider>
      <StatusBar barStyle="dark-content" />
      <SafeAreaView style={styles.container}>
        <View style={styles.content}>
          <Text style={styles.title}>푸시 알림 수신 테스트</Text>
          <Text style={styles.instructions}>
            {
              '1. 아래 버튼으로 설정을 열고 CardNotificationApp의 알림 접근을 허용하세요.\n\n2. 다른 앱에서 새 알림을 발생시키세요.\n\n3. Android Studio Logcat에서 CardNotification 태그로 패키지명, 제목, 본문을 확인하세요.'
            }
          </Text>
          <Button
            title="알림 접근 권한 설정 열기"
            onPress={openNotificationAccessSettings}
            disabled={openingSettings || Platform.OS !== 'android'}
          />
          <Text style={styles.note}>
            금액이 포함된 알림은 금액과 나머지 내용을 분리해 Apps Script로
            전송합니다.
          </Text>
        </View>
      </SafeAreaView>
    </SafeAreaProvider>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: '#ffffff' },
  content: { flex: 1, justifyContent: 'center', padding: 24 },
  title: {
    fontSize: 24,
    fontWeight: '700',
    color: '#111827',
    marginBottom: 24,
  },
  instructions: {
    fontSize: 16,
    lineHeight: 24,
    color: '#374151',
    marginBottom: 24,
  },
  note: { fontSize: 13, lineHeight: 20, color: '#6b7280', marginTop: 20 },
});

export default App;

# 푸시 알림 테스트

앱에서 **알림 접근 권한 설정 열기** → **CardNotificationApp 허용**.

## 1. 테스트 알림 발생

PowerShell에서 실행:

```powershell
& "C:\Users\gnsdl\AppData\Local\Android\Sdk\platform-tools\adb.exe" shell cmd notification post -t NotificationTest card-test NotificationListenerTestBody
```

## 2. 로그 실시간 확인

별도 PowerShell 창에서 실행 (`Ctrl+C`로 종료):

```powershell
& "C:\Users\gnsdl\AppData\Local\Android\Sdk\platform-tools\adb.exe" logcat -s CardNotification:I "*:S"
```

출력 예시:

```text
packageName=com.android.shell / title=NotificationTest / text=NotificationListenerTestBody
```

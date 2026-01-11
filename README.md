# Firebase Order Notification System

A professional Android application that displays full-screen call-style notifications for new orders using Firebase Cloud Messaging (FCM). The app handles notifications in all states: foreground, background, and killed.

## Features

### 🔔 Full-Screen Notifications
- **Background/Killed State**: Shows full-screen activity with call-style UI
- **Foreground State**: Shows in-app dialog overlay
- **Lockscreen Support**: Notifications appear over lockscreen
- **Auto-dismiss**: Automatically dismisses after 60 seconds

### 🎨 Beautiful UI
- Modern Material Design 3
- Dark theme with elegant card design
- Accept (green) and Reject (red) action buttons
- Displays order details: ID, customer, items, amount, address, time

### 🔊 Sound & Vibration
- Custom ringtone (looping)
- Vibration pattern for attention
- Stops on Accept/Reject action

### 📱 Android Compatibility
- Supports Android 7.0 (API 24) and above
- Handles Android 12+ full-screen intent permissions
- Handles Android 13+ notification permissions
- Proper permission handling for all Android versions

## Architecture

```
FCM → OrderFirebaseMessagingService → App State Check
                                      ├─ Foreground → OrderDialog
                                      └─ Background/Killed → OrderNotificationActivity
                                                            ↓
                                                      OrderRepository → Backend API
```

## Setup Instructions

### 1. Firebase Configuration

1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Create a new project or select existing one
3. Add an Android app with package name: `com.prashantpizza.nofsdotaca`
4. Download `google-services.json`
5. Replace the dummy file at `app/google-services.json` with your actual file

### 2. Build & Run

```bash
# Build the project
./gradlew build

# Install on device
./gradlew installDebug

# Run the app
adb shell am start -n com.prashantpizza.nofsdotaca/.MainActivity
```

### 3. Grant Permissions

On first launch, the app will request:
- **Notification Permission** (Android 13+)
- **Full Screen Intent Permission** (Android 14+)

Grant these permissions for full functionality.

## Testing

### Method 1: Use Test Button in App

1. Open the app
2. Tap "Send Test Notification" button
3. Press home button or lock screen to test background/killed state
4. Keep app open to test foreground dialog

### Method 2: Send FCM Message from Backend

Use the FCM token displayed in the app to send test messages.

#### Using Firebase Console

1. Go to Firebase Console → Cloud Messaging
2. Click "Send your first message"
3. Enter notification details
4. Select your app
5. Send

#### Using curl (Command Line)

```bash
curl -X POST https://fcm.googleapis.com/fcm/send \
  -H "Authorization: Bearer YOUR_SERVER_KEY" \
  -H "Content-Type: application/json" \
  -d '{
    "to": "YOUR_FCM_TOKEN",
    "priority": "high",
    "data": {
      "type": "new_order",
      "title": "New Order",
      "body": "You have a new order!",
      "orderId": "ORD-12345",
      "customerName": "John Doe",
      "items": "2x Pizza Margherita, 1x Coca Cola",
      "amount": "$25.99",
      "address": "123 Main Street, Apt 4B"
    }
  }'
```

#### Using Postman

```json
POST https://fcm.googleapis.com/fcm/send
Headers:
  Authorization: Bearer YOUR_SERVER_KEY
  Content-Type: application/json

Body:
{
  "to": "YOUR_FCM_TOKEN",
  "priority": "high",
  "data": {
    "type": "new_order",
    "title": "New Order",
    "body": "You have a new order!",
    "orderId": "ORD-12345",
    "customerName": "John Doe",
    "items": "2x Pizza, 1x Coke",
    "amount": "$25.99",
    "address": "123 Main St"
  }
}
```

### Method 3: Test Different States

#### Test Background State
```bash
# Open app, then send to background
adb shell input keyevent KEYCODE_HOME

# Send notification (use method above)

# Result: Full-screen activity should appear
```

#### Test Killed State
```bash
# Force stop the app
adb shell am force-stop com.prashantpizza.nofsdotaca

# Send notification (use method above)

# Result: Full-screen activity should appear and wake screen
```

#### Test Foreground State
```bash
# Keep app open and visible

# Send notification (use method above)

# Result: Dialog should appear over the app
```

#### Test Lockscreen
```bash
# Lock the device
adb shell input keyevent KEYCODE_POWER

# Send notification (use method above)

# Result: Full-screen activity should appear over lockscreen
```

## Project Structure

```
app/src/main/java/com/prashantpizza/nofsdotaca/
├── MainActivity.kt                          # Main activity with permission handling
├── OrderApplication.kt                      # Application class for initialization
├── model/
│   └── OrderNotification.kt                 # Data model for orders
├── service/
│   └── OrderFirebaseMessagingService.kt     # FCM message receiver
├── notification/
│   └── NotificationHelper.kt                # Notification utilities
├── repository/
│   └── OrderRepository.kt                   # API calls for Accept/Reject
├── ui/
│   ├── OrderNotificationActivity.kt         # Full-screen notification activity
│   ├── OrderDialog.kt                       # Foreground dialog
│   └── theme/                               # Material Design theme
└── utils/
    └── AppStateTracker.kt                   # Track app foreground/background state
```

## Key Components

### OrderFirebaseMessagingService
- Receives FCM messages in all app states
- Routes to appropriate handler based on app state
- Handles notification data parsing

### OrderNotificationActivity
- Full-screen call-style UI
- Shows over lockscreen
- Plays ringtone and vibrates
- Auto-dismisses after 60 seconds
- Handles Accept/Reject actions

### OrderDialog
- In-app dialog for foreground notifications
- Same order details as full-screen
- Dismissible by user

### NotificationHelper
- Creates notification channels
- Builds high-priority notifications
- Manages notification permissions
- Handles Android version differences

### OrderRepository
- Handles Accept/Reject API calls
- Uses Retrofit for networking
- Currently uses dummy responses (ready for backend integration)

### AppStateTracker
- Tracks app foreground/background state
- Uses ActivityLifecycleCallbacks
- Helps route notifications correctly

## Backend Integration

### Sending FCM Messages

Your backend should send FCM messages with the following structure:

```json
{
  "to": "DEVICE_FCM_TOKEN",
  "priority": "high",
  "data": {
    "type": "new_order",
    "title": "New Order",
    "body": "You have a new order!",
    "orderId": "ORD-12345",
    "customerName": "John Doe",
    "items": "2x Pizza, 1x Coke",
    "amount": "$25.99",
    "address": "123 Main St"
  }
}
```

**Important**: Use `data` payload (not `notification`) for custom handling in all app states.

### Accept/Reject API Endpoints

Update `OrderRepository.kt` with your actual API endpoints:

```kotlin
private const val BASE_URL = "https://your-api-endpoint.com/"
```

Expected API format:

```
POST /api/orders/action
Content-Type: application/json

{
  "orderId": "ORD-12345",
  "action": "accept",  // or "reject"
  "timestamp": 1234567890
}

Response:
{
  "success": true,
  "message": "Order accepted successfully"
}
```

## Permissions Required

### Manifest Permissions
- `POST_NOTIFICATIONS` - Show notifications (Android 13+)
- `VIBRATE` - Vibrate device
- `WAKE_LOCK` - Wake screen
- `USE_FULL_SCREEN_INTENT` - Show full-screen notifications
- `SYSTEM_ALERT_WINDOW` - Draw over other apps
- `FOREGROUND_SERVICE` - Run foreground services
- `INTERNET` - Network access
- `ACCESS_NETWORK_STATE` - Check network state

### Runtime Permissions
- Notification permission (Android 13+)
- Full screen intent permission (Android 14+)

## Troubleshooting

### Notifications Not Appearing

1. **Check Permissions**: Ensure all permissions are granted
2. **Check FCM Token**: Verify token is correct in Firebase Console
3. **Check google-services.json**: Ensure file is properly configured
4. **Check Battery Optimization**: Disable battery optimization for the app
5. **Check Do Not Disturb**: Ensure DND is not blocking notifications

### Full-Screen Not Working

1. **Android 12+**: Check if full-screen intent permission is granted
2. **Settings**: Go to Settings → Apps → Your App → Full Screen Intent
3. **Battery Saver**: Disable battery saver mode

### No Sound/Vibration

1. **Volume**: Check notification volume is not muted
2. **DND**: Check Do Not Disturb settings
3. **Permissions**: Verify VIBRATE permission is granted

## Dependencies

- Firebase BOM 33.7.0
- Firebase Cloud Messaging
- Firebase Analytics
- Retrofit 2.11.0
- OkHttp 4.12.0
- Material Design 3
- Jetpack Compose
- LocalBroadcastManager

## License

This project is provided as-is for demonstration purposes.

## Support

For issues or questions, please check:
1. Firebase Console for FCM configuration
2. Android Studio Logcat for error messages
3. Ensure all permissions are granted
4. Test on physical device (emulator may have limitations)

---

**Built with ❤️ using Kotlin, Jetpack Compose, and Firebase**

# Quick Start Guide

Get your Firebase Order Notification System up and running in 5 minutes!

## 🚀 Prerequisites

- Android Studio installed
- Android device or emulator (API 24+)
- Firebase account
- 5 minutes of your time

---

## 📝 Step-by-Step Setup

### Step 1: Firebase Configuration (2 minutes)

1. **Go to Firebase Console**
   ```
   https://console.firebase.google.com/
   ```

2. **Create/Select Project**
   - Click "Add project" or select existing
   - Follow the wizard

3. **Add Android App**
   - Click "Add app" → Android icon
   - Package name: `com.prashantpizza.nofsdotaca`
   - Click "Register app"

4. **Download google-services.json**
   - Download the file
   - Replace `app/google-services.json` with downloaded file

5. **Done!** Firebase is configured ✅

---

### Step 2: Build & Install (2 minutes)

1. **Open Project**
   ```bash
   cd /Users/ajay/Downloads/my/nofsdotaca
   ```

2. **Build Project**
   ```bash
   ./gradlew clean build
   ```

3. **Connect Device**
   - Connect Android device via USB
   - Enable USB debugging
   - Or start Android emulator

4. **Install App**
   ```bash
   ./gradlew installDebug
   ```

5. **Done!** App is installed ✅

---

### Step 3: Test Notification (1 minute)

1. **Open App**
   - Launch app from device
   - Grant notification permission when prompted

2. **Get FCM Token**
   - Copy the FCM token shown in the app
   - It's a long alphanumeric string

3. **Send Test Notification**
   - Option A: Tap "Send Test Notification" button in app
   - Option B: Use Firebase Console (see below)

4. **Test Different States**
   - Press Home button → Send notification (tests background)
   - Force stop app → Send notification (tests killed state)
   - Keep app open → Send notification (tests foreground dialog)

5. **Done!** Notifications working ✅

---

## 🔥 Send Test from Firebase Console

1. **Go to Cloud Messaging**
   ```
   Firebase Console → Cloud Messaging → Send your first message
   ```

2. **Compose Notification**
   - Title: `New Order`
   - Text: `You have a new order!`

3. **Send Test Message**
   - Click "Send test message"
   - Paste your FCM token
   - Click "Test"

4. **Observe Result**
   - Full-screen activity should appear!

---

## 📱 What You Should See

### Background/Killed State
```
┌─────────────────────────┐
│    🛒 Shopping Cart     │
│                         │
│      New Order          │
│  You have a new order!  │
│                         │
│  ┌─────────────────┐   │
│  │ Order ID: ORD-X │   │
│  │ Customer: John  │   │
│  │ Items: 2x Pizza │   │
│  │ Amount: $25.99  │   │
│  │ Address: 123... │   │
│  └─────────────────┘   │
│                         │
│   ❌ Reject  ✅ Accept  │
└─────────────────────────┘
```

### Foreground State
```
┌─────────────────────────┐
│  Your App Content       │
│                         │
│  ┌───────────────────┐ │
│  │  🛒 New Order    X│ │
│  │                   │ │
│  │  Order Details    │ │
│  │  ┌─────────────┐  │ │
│  │  │ ORD-12345   │  │ │
│  │  │ John Doe    │  │ │
│  │  └─────────────┘  │ │
│  │                   │ │
│  │ [Reject] [Accept] │ │
│  └───────────────────┘ │
│                         │
└─────────────────────────┘
```

---

## 🧪 Quick Test Commands

### Test Background
```bash
adb shell input keyevent KEYCODE_HOME
# Now send notification
```

### Test Killed
```bash
adb shell am force-stop com.prashantpizza.nofsdotaca
# Now send notification
```

### Test Lockscreen
```bash
adb shell input keyevent KEYCODE_POWER
# Now send notification
```

### View Logs
```bash
adb logcat | grep "prashantpizza"
```

---

## 🔧 Quick Troubleshooting

### Problem: No notification appears
**Solution**: Check permissions
```bash
# Open app → Tap "Request Permissions"
# Grant all permissions
```

### Problem: No full-screen
**Solution**: Grant full-screen permission (Android 14+)
```bash
# Settings → Apps → Your App → Full Screen Intent → Allow
```

### Problem: No sound
**Solution**: Check volume
```bash
# Increase notification volume
# Disable Do Not Disturb
```

### Problem: FCM token not showing
**Solution**: Check Firebase setup
```bash
# Verify google-services.json is correct
# Rebuild: ./gradlew clean build
```

---

## 📚 Need More Help?

- **Full Documentation**: See `README.md`
- **Testing Guide**: See `TESTING_GUIDE.md`
- **Backend Integration**: See `BACKEND_INTEGRATION.md`
- **Implementation Details**: See `IMPLEMENTATION_SUMMARY.md`

---

## 🎯 Next Steps

### For Development
1. ✅ Complete Steps 1-3 above
2. ✅ Test all notification states
3. ✅ Customize UI colors/theme
4. ✅ Integrate with your backend

### For Production
1. ✅ Update `BASE_URL` in `OrderRepository.kt`
2. ✅ Implement backend API endpoints
3. ✅ Test on multiple devices
4. ✅ Test with real order data
5. ✅ Deploy to Play Store

---

## 💡 Pro Tips

1. **Test on Physical Device**: Emulators may not support all features
2. **Disable Battery Optimization**: For reliable notifications
3. **Use Data Payload**: Not notification payload in FCM
4. **Check Logs**: Always check logs for debugging
5. **Grant All Permissions**: Required for full functionality

---

## 📞 Quick Reference

### FCM Message Format
```json
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

### Key Files to Customize
- `OrderRepository.kt` - Update BASE_URL
- `ui/theme/Color.kt` - Change colors
- `OrderNotificationActivity.kt` - Modify timeout
- `strings.xml` - Update text

---

## ✅ Success Checklist

- [ ] Firebase configured
- [ ] App built and installed
- [ ] Permissions granted
- [ ] Test notification sent
- [ ] Background notification works
- [ ] Killed state notification works
- [ ] Foreground dialog works
- [ ] Accept button works
- [ ] Reject button works
- [ ] Sound plays
- [ ] Vibration works

**All checked?** 🎉 You're ready to go!

---

**Time to complete: 5 minutes**
**Difficulty: Easy**
**Result: Professional notification system** ✨

# 🚀 FCM Test Notification Sender

## Quick Start

### 1️⃣ Install Dependencies

```bash
cd /Users/ajay/Downloads/my/nofsdotaca
npm install
```

### 2️⃣ Send Test Notification

**Option A: Using npm script**
```bash
npm run send
```

**Option B: Direct node command**
```bash
node send-test-notification.js
```

**Option C: Automated test script (Mac/Linux)**
```bash
chmod +x TEST_FCM.sh
./TEST_FCM.sh
```

---

## 📋 What It Does

1. ✅ Reads your Firebase service account credentials
2. ✅ Gets OAuth2 access token
3. ✅ Sends FCM notification with **DATA payload** (not notification payload)
4. ✅ Uses FCM HTTP v1 API (latest)
5. ✅ Triggers full-screen activity on your device

---

## 📱 Before Testing

Make sure:
- ✅ App is installed on device
- ✅ Device is connected via ADB
- ✅ App is in **background** (press Home button)

```bash
# Send app to background
adb shell input keyevent KEYCODE_HOME

# Watch logs
adb logcat | grep -E "OrderFCMService|NotificationHelper|OrderNotificationActivity"
```

---

## 🎯 Expected Output

### From Script:
```
🚀 Firebase FCM Test Notification Sender
==========================================

🔑 Getting OAuth2 access token...
✅ Access token obtained

📤 Sending FCM notification...
📱 Target token: fOu_erylTm6eU8DKPOgQY...
📦 Order ID: ORD-1768122345678

📊 Response Status: 200
✅ Notification sent successfully!

🎉 SUCCESS! Check your device now!
📱 Full-screen activity should appear
```

### From Device Logs:
```
OrderFCMService: Message received from: 122219701660
OrderFCMService: Message data payload: {type=new_order, ...}
OrderFCMService: New order notification: ORD-1768122345678
OrderFCMService: Showing full-screen notification...
NotificationHelper: Launching full-screen activity directly...
NotificationHelper: ✅ Full-screen activity launched successfully
OrderNotificationActivity: Showing full screen notification
```

### On Device:
- 🎬 Full-screen activity appears
- 🔔 Ringtone plays
- 📳 Device vibrates
- ✅ Accept/Reject buttons visible

---

## 🔧 Customization

### Use Different FCM Token

```bash
node send-test-notification.js "YOUR_FCM_TOKEN_HERE"
```

### Modify Order Data

Edit `send-test-notification.js` line 35-44:
```javascript
data: {
  type: "new_order",
  title: "New Order",
  body: "You have a new order!",
  orderId: `ORD-${Date.now()}`,
  customerName: "Your Customer Name",  // ← Change this
  items: "Your Items",                  // ← Change this
  amount: "$99.99",                     // ← Change this
  address: "Your Address"               // ← Change this
}
```

---

## 🐛 Troubleshooting

### Error: "Cannot find module 'googleapis'"

```bash
npm install
```

### Error: "Cannot find module './firebase-service-account.json'"

Make sure `firebase-service-account.json` exists in project root.

### Error: "HTTP 401: Unauthorized"

Your service account key is invalid or expired. Generate a new one from Firebase Console.

### Error: "HTTP 404: Not Found"

Check that `project_id` in `firebase-service-account.json` matches your Firebase project.

### No Full-Screen Appearing

1. Check app is in background: `adb shell input keyevent KEYCODE_HOME`
2. Check logs: `adb logcat | grep OrderFCMService`
3. Verify notification permission is granted
4. Verify battery optimization is disabled

---

## 📊 Files Created

- `send-test-notification.js` - Main script
- `firebase-service-account.json` - Service account credentials
- `package.json` - Node.js dependencies
- `TEST_FCM.sh` - Automated test script
- `.gitignore` - Prevents committing credentials

---

## 🔒 Security Notes

1. ⚠️ **NEVER commit `firebase-service-account.json` to Git**
2. ⚠️ **NEVER share service account credentials publicly**
3. ✅ The `.gitignore` file is configured to exclude it
4. ✅ After testing, generate a new service account key

---

## 📝 About Web Push Certificates

**Q: Do I need Web Push certificates for Android?**

**A: NO!** ❌

Web Push certificates are **ONLY** for:
- 🌐 Web browsers (Chrome, Firefox, Safari)
- 💻 Progressive Web Apps (PWA)
- 🖥️ Desktop notifications

For **Android native apps**, you DON'T need them. You only need:
- ✅ Service account JSON (for backend)
- ✅ google-services.json (for Android app)
- ✅ FCM device token

---

## 🎉 Success!

If you see the full-screen activity on your device, congratulations! 🎊

Your Firebase push notification system is working perfectly!

---

## 📞 Next Steps

1. ✅ Integrate this into your backend server
2. ✅ Store FCM tokens in your database
3. ✅ Send notifications when real orders arrive
4. ✅ Handle Accept/Reject actions in your backend

---

**Happy Testing!** 🚀

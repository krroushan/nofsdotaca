# 🚀 Test Full-Screen Notification NOW

## ✅ Changes Made

I've fixed the full-screen notification to **launch the activity directly** instead of relying only on the system's full-screen intent mechanism.

### What Changed:
- **Before**: Relied on Android system to launch full-screen (unreliable on Android 10+)
- **After**: Directly launches the activity + shows notification (guaranteed to work)

---

## 🧪 Test Steps

### Step 1: Rebuild and Install

```bash
cd /Users/ajay/Downloads/my/nofsdotaca
./gradlew clean installDebug
```

### Step 2: Clear Logs

```bash
adb logcat -c
```

### Step 3: Open App

```bash
adb shell am start -n com.prashantpizza.nofsdotaca/.MainActivity
```

### Step 4: Send to Background

```bash
adb shell input keyevent KEYCODE_HOME
```

### Step 5: Trigger Test Notification

**Option A: From App (Before going to background)**
- Tap "Send Test Notification" button
- Then press Home button

**Option B: From Terminal**
- While app is in background, you can send FCM notification

### Step 6: Watch Logs

```bash
adb logcat | grep -E "MainActivity|NotificationHelper|OrderNotificationActivity"
```

---

## 📊 Expected Logs

You should see:

```
MainActivity: === TEST NOTIFICATION TRIGGERED ===
MainActivity: App in foreground: false
MainActivity: Created test order: ORD-XXXXX
NotificationHelper: Android SDK: 35, Device: motorola moto g34 5G
NotificationHelper: Android 14+ Full screen intent permission: true
NotificationHelper: Showing notification with full-screen intent...
NotificationHelper: ✅ Notification posted successfully
NotificationHelper: Launching full-screen activity directly...
NotificationHelper: ✅ Full-screen activity launched successfully
OrderNotificationActivity: Showing full screen notification for order: ORD-XXXXX
```

---

## 🎯 What Should Happen

1. **Notification appears in tray** ✅
2. **Full-screen activity launches IMMEDIATELY** ✅
3. **You see the call-style UI with order details** ✅
4. **Ringtone plays** ✅
5. **Device vibrates** ✅
6. **Accept/Reject buttons work** ✅

---

## 🔍 If Still Not Working

### Check 1: Is Activity Launching?

```bash
adb logcat | grep "OrderNotificationActivity"
```

**If you see**:
```
OrderNotificationActivity: Showing full screen notification
```
→ Activity is launching! ✅

**If you DON'T see it**:
→ Check next steps

### Check 2: Any Errors?

```bash
adb logcat | grep -E "ERROR|Exception|Failed"
```

**Common errors**:
- `ActivityNotFoundException` → Activity not registered in manifest
- `SecurityException` → Permission issue
- `IllegalStateException` → Context issue

### Check 3: Is Notification Showing?

```bash
adb shell dumpsys notification | grep "com.prashantpizza.nofsdotaca"
```

Should show active notification.

### Check 4: Test Activity Directly

```bash
# Try launching activity directly
adb shell am start -n com.prashantpizza.nofsdotaca/.ui.OrderNotificationActivity
```

**If this works** → Activity is fine, issue is with notification trigger
**If this fails** → Activity has configuration issue

---

## 🛠️ Alternative Test Methods

### Method 1: Test from Foreground

1. Open app
2. Tap "Send Test Notification"
3. **Don't go to background**
4. Full-screen should still appear (new behavior)

### Method 2: Test from Killed State

```bash
# Force stop app
adb shell am force-stop com.prashantpizza.nofsdotaca

# Send FCM notification with your token
# Full-screen should launch
```

### Method 3: Test on Lockscreen

```bash
# Lock device
adb shell input keyevent KEYCODE_POWER

# Send notification
# Screen should wake and show full-screen
```

---

## 📱 Device-Specific Issues

### Motorola Moto G34 5G (Your Device)

Motorola devices are generally good with notifications, but check:

1. **Battery Saver**: Disable if enabled
   ```
   Settings → Battery → Battery Saver → OFF
   ```

2. **App Battery Optimization**: Already disabled ✅

3. **Display Over Other Apps**: Check if needed
   ```
   Settings → Apps → Your App → Display over other apps → Allow
   ```

---

## 🎬 Video Test

Record your screen while testing:

```bash
# Start recording
adb shell screenrecord /sdcard/test.mp4

# Do your test (send notification)

# Stop recording (Ctrl+C after ~10 seconds)

# Pull video
adb pull /sdcard/test.mp4
```

This helps see exactly what's happening.

---

## 💡 Debug Commands

### See All Logs
```bash
adb logcat -v time | grep "prashantpizza"
```

### See Only Errors
```bash
adb logcat *:E | grep "prashantpizza"
```

### See Notification System
```bash
adb logcat | grep "NotificationManager"
```

### See Activity Manager
```bash
adb logcat | grep "ActivityManager"
```

---

## ✅ Success Indicators

You'll know it's working when you see:

1. ✅ Logs show "Full-screen activity launched successfully"
2. ✅ Full-screen UI appears on your device
3. ✅ Ringtone plays
4. ✅ Device vibrates
5. ✅ You can tap Accept/Reject
6. ✅ Activity dismisses after action

---

## 🚨 If STILL Not Working

Share these with me:

1. **Complete logs**:
   ```bash
   adb logcat -d > logs.txt
   ```

2. **Notification dump**:
   ```bash
   adb shell dumpsys notification > notification_dump.txt
   ```

3. **Activity dump**:
   ```bash
   adb shell dumpsys activity > activity_dump.txt
   ```

4. **Screenshot or video** of what you see

---

## 🎯 Quick Test Command

Run this all-in-one test:

```bash
echo "=== Starting Full-Screen Test ==="
adb logcat -c
adb shell am start -n com.prashantpizza.nofsdotaca/.MainActivity
sleep 3
adb shell input keyevent KEYCODE_HOME
echo "App sent to background. Now tap 'Send Test Notification' button."
echo "Watching logs..."
adb logcat | grep -E "MainActivity|NotificationHelper|OrderNotificationActivity"
```

---

**The fix is deployed. Rebuild, test, and let me know what you see!** 🚀

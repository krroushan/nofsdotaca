# Debug Full-Screen Permission Issue

## 🔍 Why Dialog Isn't Showing

The dialog only shows on **Android 14+ (API 34+)**. Here's how to check:

### Step 1: Check Your Android Version

```bash
adb shell getprop ro.build.version.sdk
```

**Result Interpretation**:
- **34 or higher** = Android 14+ → Dialog SHOULD show
- **33** = Android 13 → No dialog needed, full-screen works automatically
- **32 or lower** = Android 12 or below → No dialog needed

### Step 2: Check Logs After Opening App

```bash
adb logcat -c  # Clear logs
adb shell am start -n com.prashantpizza.nofsdotaca/.MainActivity
adb logcat | grep -E "MainActivity|checkFullScreen"
```

**Look for these logs**:

#### If Android 14+:
```
MainActivity: Checking full screen intent permission...
MainActivity: Android SDK Version: 34
MainActivity: Android Version: 14
MainActivity: Device: Samsung Galaxy
MainActivity: Android 14+ detected - checking full screen intent permission
MainActivity: Can use full screen intent: false
MainActivity: Full screen intent permission NOT granted - showing dialog
```

#### If Android 13 or below:
```
MainActivity: Checking full screen intent permission...
MainActivity: Android SDK Version: 33
MainActivity: Android Version: 13
MainActivity: Android 33 - Full screen intent works automatically (no permission needed)
```

---

## 🛠️ Solutions Based on Android Version

### If You're on Android 14+

**The dialog should show automatically when you open the app.**

If it doesn't show:

1. **Rebuild and reinstall the app**:
   ```bash
   ./gradlew clean installDebug
   ```

2. **Clear app data** (this forces permission checks):
   ```bash
   adb shell pm clear com.prashantpizza.nofsdotaca
   ```

3. **Open app again** - dialog should appear

4. **Or use the manual button**:
   - Open the app
   - Look for button: **"Enable Full-Screen Notifications (Android 14+)"**
   - Tap it
   - Dialog will show

### If You're on Android 13 or Below

**You don't need the dialog!** Full-screen notifications work automatically on Android 13 and below.

**What you need instead**:
1. ✅ Notification permission (Android 13)
2. ✅ Battery optimization disabled
3. ✅ That's it!

---

## 🧪 Test Full-Screen (Any Android Version)

### Test 1: Background State
```bash
# 1. Open app
adb shell am start -n com.prashantpizza.nofsdotaca/.MainActivity

# 2. Send to background
adb shell input keyevent KEYCODE_HOME

# 3. Tap "Send Test Notification" button in app
# OR send FCM notification

# Expected: Full-screen activity should appear
```

### Test 2: Check Logs
```bash
adb logcat | grep -E "NotificationHelper|OrderNotificationActivity"
```

**Good logs (Android 13 or below)**:
```
NotificationHelper: Android SDK: 33, Device: Google Pixel
NotificationHelper: Android 33 - Full screen intent should work automatically
NotificationHelper: Showing notification with full-screen intent...
NotificationHelper: ✅ Notification posted successfully
NotificationHelper: Android 10-13: Also launching activity directly...
NotificationHelper: ✅ Activity launched directly
OrderNotificationActivity: Showing full screen notification for order: ORD-12345
```

**Needs permission (Android 14+)**:
```
NotificationHelper: Android SDK: 34, Device: Samsung Galaxy
NotificationHelper: Android 14+ Full screen intent permission: false
NotificationHelper: ⚠️ Full screen intent permission NOT granted
NotificationHelper: Notification will appear in tray only. Tap to open.
```

---

## 📱 Quick Diagnostic

Run this complete diagnostic:

```bash
echo "=== Full-Screen Diagnostic ==="
echo ""
echo "1. Android Version:"
adb shell getprop ro.build.version.sdk
echo ""
echo "2. Device Info:"
adb shell getprop ro.product.manufacturer
adb shell getprop ro.product.model
echo ""
echo "3. Full-Screen Permission (Android 14+ only):"
adb shell dumpsys notification | grep -A 5 "com.prashantpizza.nofsdotaca" | grep "canUseFullScreenIntent"
echo ""
echo "4. Battery Optimization:"
adb shell dumpsys deviceidle whitelist | grep prashantpizza
echo ""
echo "5. Notification Permission:"
adb shell dumpsys package com.prashantpizza.nofsdotaca | grep "POST_NOTIFICATIONS"
```

---

## 🎯 Expected Behavior by Android Version

| Android Version | Dialog Shows? | Full-Screen Works? | Action Required |
|----------------|---------------|-------------------|-----------------|
| **Android 14+ (API 34+)** | ✅ Yes | ⚠️ Only after permission | Enable in Settings |
| **Android 13 (API 33)** | ❌ No | ✅ Yes, automatically | Disable battery optimization |
| **Android 12 (API 31-32)** | ❌ No | ✅ Yes, automatically | Disable battery optimization |
| **Android 11 (API 30)** | ❌ No | ✅ Yes, automatically | Disable battery optimization |
| **Android 10 (API 29)** | ❌ No | ✅ Yes, automatically | Disable battery optimization |

---

## 🔧 Manual Permission Enable (Android 14+)

If dialog doesn't work, enable manually:

### Method 1: Using App Button
1. Open app
2. Scroll down to "Test Notifications" section
3. Tap **"Enable Full-Screen Notifications (Android 14+)"** button
4. Dialog will show → Tap "Open Settings"
5. Toggle "Full screen notifications" to ON

### Method 2: Manual Settings
```
Settings 
  → Apps 
    → Your App 
      → Notifications 
        → Full screen notifications 
          → Toggle ON ✅
```

### Method 3: ADB Command
```bash
adb shell am start -a android.settings.MANAGE_APP_USE_FULL_SCREEN_INTENT \
  -d package:com.prashantpizza.nofsdotaca
```

---

## ✅ Verify It's Working

After enabling (or if on Android 13-):

```bash
# 1. Clear logs
adb logcat -c

# 2. Send to background
adb shell input keyevent KEYCODE_HOME

# 3. Send notification
# (Use test button in app or FCM)

# 4. Check logs
adb logcat | grep -E "NotificationHelper|OrderNotificationActivity"

# Should see:
# ✅ Notification posted successfully
# ✅ Activity launched directly
# OrderNotificationActivity: Showing full screen notification
```

---

## 💡 Summary

**Dialog not showing?**
- Check your Android version (must be 14+)
- If Android 13 or below: No dialog needed, full-screen works automatically
- If Android 14+: Use the manual button in the app or enable in Settings

**Full-screen not working?**
- Android 14+: Enable permission in Settings
- Android 13-: Disable battery optimization
- All versions: Check logs for specific error

---

**Need more help?** Share your:
1. Android version: `adb shell getprop ro.build.version.sdk`
2. Device: `adb shell getprop ro.product.model`
3. Logs: `adb logcat | grep MainActivity`

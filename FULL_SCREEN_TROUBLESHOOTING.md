# Full-Screen Intent Troubleshooting Guide

## 🔍 Issue: Notification Shows in Tray Instead of Full-Screen

If you're seeing a notification in the notification tray instead of a full-screen activity, follow this guide.

---

## 📱 Android Version-Specific Behavior

### Android 14+ (API 34+) - **REQUIRES USER PERMISSION**

**Problem**: Full-screen intents require explicit user permission.

**Solution**:
1. Open the app
2. A dialog will appear asking for full-screen notification permission
3. Tap "Allow"
4. In Settings, toggle "Full screen notifications" to ON
5. Return to app and test again

**Manual Steps**:
```
Settings → Apps → Your App → Notifications → 
Full screen notifications → Toggle ON
```

**ADB Command to Check**:
```bash
adb shell dumpsys notification | grep "fullScreenIntent"
```

### Android 10-13 (API 29-33) - **SHOULD WORK AUTOMATICALLY**

**Problem**: Full-screen should work automatically but may be blocked by:
- Battery optimization
- Do Not Disturb mode
- Manufacturer restrictions

**Solution**:
1. Disable battery optimization (see BATTERY_OPTIMIZATION_GUIDE.md)
2. Disable Do Not Disturb
3. Check manufacturer-specific settings

### Android 7-9 (API 24-28) - **WORKS AUTOMATICALLY**

Full-screen intents work without any special permissions.

---

## ✅ Quick Diagnostic Checklist

Run through this checklist to identify the issue:

### 1. Check Android Version
```bash
adb shell getprop ro.build.version.sdk

# If 34+ (Android 14+) → Need full-screen permission
# If 29-33 (Android 10-13) → Should work automatically
# If 24-28 (Android 7-9) → Should work automatically
```

### 2. Check Full-Screen Permission (Android 14+)
```bash
adb shell dumpsys notification | grep -A 5 "com.prashantpizza.nofsdotaca"

# Look for: "canUseFullScreenIntent: true"
# If false → User must grant permission
```

### 3. Check Notification Permission
```bash
adb shell dumpsys package com.prashantpizza.nofsdotaca | grep "POST_NOTIFICATIONS"

# Should show: granted=true
```

### 4. Check Battery Optimization
```bash
adb shell dumpsys deviceidle whitelist | grep prashantpizza

# If empty → Battery optimized (BAD)
# If shows package → Not optimized (GOOD)
```

### 5. Check Do Not Disturb
```bash
adb shell settings get global zen_mode

# 0 = DND off (GOOD)
# 1 = Priority only
# 2 = Total silence (BAD for notifications)
# 3 = Alarms only
```

---

## 🔧 Solutions by Android Version

### For Android 14+ Users

**Step 1: Grant Full-Screen Permission**
```
1. Open app
2. When dialog appears, tap "Allow"
3. In Settings, enable "Full screen notifications"
4. Return to app
```

**Step 2: Verify Permission**
```bash
# Check if permission granted
adb shell dumpsys notification | grep "canUseFullScreenIntent"

# Should show: canUseFullScreenIntent: true
```

**Step 3: Test**
```bash
# Send test notification
# Full-screen should now appear
```

### For Android 10-13 Users

**Step 1: Disable Battery Optimization**
```
1. Open app
2. Tap "Disable Battery Optimization" button
3. Select "Allow"
```

**Step 2: Check Manufacturer Settings**

**Xiaomi (MIUI)**:
```
Settings → Apps → Manage apps → Your App
- Autostart → Enable
- Battery saver → No restrictions
```

**Samsung (One UI)**:
```
Settings → Apps → Your App → Battery
- Optimize battery usage → Disable
- Sleeping apps → Remove if listed
```

**OnePlus/Oppo**:
```
Settings → Battery → Battery optimization
- Your App → Don't optimize
```

**Step 3: Test**
```bash
adb shell am force-stop com.prashantpizza.nofsdotaca
# Send notification → Should show full-screen
```

---

## 🧪 Testing Full-Screen Intent

### Test 1: Background State
```bash
# 1. Open app
adb shell am start -n com.prashantpizza.nofsdotaca/.MainActivity

# 2. Send to background
adb shell input keyevent KEYCODE_HOME

# 3. Send notification (use test button or FCM)

# Expected: Full-screen activity appears over home screen
```

### Test 2: Killed State
```bash
# 1. Force stop app
adb shell am force-stop com.prashantpizza.nofsdotaca

# 2. Send notification

# Expected: Full-screen activity launches automatically
```

### Test 3: Lockscreen
```bash
# 1. Lock device
adb shell input keyevent KEYCODE_POWER

# 2. Send notification

# Expected: Screen wakes, full-screen appears over lockscreen
```

### Test 4: Check Logs
```bash
adb logcat | grep -E "NotificationHelper|OrderNotificationActivity"

# Look for:
# ✅ "Notification posted successfully"
# ✅ "Activity launched directly"
# ⚠️ "Full screen intent permission NOT granted"
```

---

## 📊 Expected Log Output

### ✅ Good Logs (Full-Screen Working)
```
NotificationHelper: Android SDK: 33, Device: Google Pixel
NotificationHelper: Android 33 - Full screen intent should work automatically
NotificationHelper: Showing notification with full-screen intent...
NotificationHelper: ✅ Notification posted successfully
NotificationHelper: Android 10-13: Also launching activity directly...
NotificationHelper: ✅ Activity launched directly
OrderNotificationActivity: Showing full screen notification for order: ORD-12345
```

### ❌ Bad Logs (Permission Issue - Android 14+)
```
NotificationHelper: Android SDK: 34, Device: Samsung Galaxy
NotificationHelper: Android 14+ Full screen intent permission: false
NotificationHelper: ⚠️ Full screen intent permission NOT granted
NotificationHelper: Notification will appear in tray only. Tap to open.
NotificationHelper: ✅ Notification posted successfully
```

### ❌ Bad Logs (Notification Permission Issue)
```
NotificationHelper: ❌ Notification permission NOT granted!
```

---

## 🔍 Device-Specific Issues

### Samsung Devices
**Issue**: "Deep Sleeping Apps" feature kills background apps

**Solution**:
```
Settings → Battery → Background usage limits
→ Sleeping apps → Remove your app
→ Deep sleeping apps → Remove your app
```

### Xiaomi Devices
**Issue**: MIUI aggressively kills background apps

**Solution**:
```
1. Settings → Apps → Manage apps → Your App
2. Enable "Autostart"
3. Battery saver → "No restrictions"
4. Other permissions → "Display pop-up windows" → Enable
```

### Huawei Devices
**Issue**: App Launch restrictions

**Solution**:
```
Settings → Battery → App launch → Your App
→ Manage manually
→ Enable: Auto-launch, Secondary launch, Run in background
```

### OnePlus Devices
**Issue**: Battery optimization very aggressive

**Solution**:
```
Settings → Battery → Battery optimization
→ Your App → Don't optimize

Settings → Battery → App auto-launch
→ Your App → Enable
```

---

## 🛠️ Advanced Debugging

### Enable Verbose Logging
```bash
# Enable all logs
adb logcat -c  # Clear logs
adb logcat | grep -E "prashantpizza|NotificationHelper|OrderNotificationActivity|OrderFCMService"
```

### Check Notification Channel
```bash
adb shell dumpsys notification | grep -A 20 "order_notifications"

# Look for:
# - importance: HIGH (should be 4)
# - canShowBadge: true
# - canBypassDnd: true
```

### Check Activity Launch
```bash
adb shell dumpsys activity | grep "OrderNotificationActivity"

# Should show activity in stack when full-screen is shown
```

### Simulate Full-Screen Launch
```bash
# Manually launch the activity to test if it works
adb shell am start -n com.prashantpizza.nofsdotaca/.ui.OrderNotificationActivity

# If this works but notification doesn't → Permission issue
# If this doesn't work → Activity configuration issue
```

---

## 📝 Common Fixes

### Fix 1: Reset Notification Settings
```bash
# Clear app data (will reset all permissions)
adb shell pm clear com.prashantpizza.nofsdotaca

# Reinstall app
./gradlew installDebug

# Grant permissions again
```

### Fix 2: Force Grant Full-Screen Permission (Android 14+)
```bash
# This only works on rooted devices or emulators
adb shell settings put secure notification_bubbles 1
```

### Fix 3: Disable Battery Optimization via ADB
```bash
# Add to whitelist
adb shell dumpsys deviceidle whitelist +com.prashantpizza.nofsdotaca

# Verify
adb shell dumpsys deviceidle whitelist | grep prashantpizza
```

### Fix 4: Test with Screen On
```bash
# Some devices only show full-screen when screen is on
# Wake screen first
adb shell input keyevent KEYCODE_WAKEUP

# Then send notification
```

---

## 🎯 Final Checklist

Before reporting an issue, verify:

- [ ] Android version checked
- [ ] Full-screen permission granted (Android 14+)
- [ ] Notification permission granted
- [ ] Battery optimization disabled
- [ ] Do Not Disturb disabled
- [ ] Manufacturer-specific settings configured
- [ ] Tested in background state
- [ ] Tested in killed state
- [ ] Tested on lockscreen
- [ ] Logs checked for errors
- [ ] Tested on physical device (not just emulator)

---

## 📞 Still Not Working?

If full-screen still doesn't work after following this guide:

1. **Check your Android version**:
   ```bash
   adb shell getprop ro.build.version.sdk
   ```

2. **Share these logs**:
   ```bash
   adb logcat | grep -E "NotificationHelper|OrderNotificationActivity" > logs.txt
   ```

3. **Share device info**:
   ```bash
   adb shell getprop | grep "ro.product"
   ```

4. **Check if it's a known issue** with your device manufacturer

---

## 💡 Pro Tips

1. **Always test on physical devices** - Emulators may not accurately simulate full-screen behavior

2. **Test on multiple manufacturers** - Samsung, Xiaomi, OnePlus all behave differently

3. **Android 14+ requires user action** - There's no way around this, users MUST grant permission

4. **Battery optimization is critical** - Even with full-screen permission, battery optimization can block it

5. **Manufacturer settings matter** - Stock Android works best, custom ROMs vary

---

**Remember**: Full-screen intents are designed for time-sensitive notifications like incoming calls and alarms. Your order notification system qualifies for this use case! 📱✨

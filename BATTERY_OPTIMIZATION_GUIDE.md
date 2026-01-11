# Battery Optimization Guide

## 🔋 Why Battery Optimization Matters

Battery optimization is **THE MOST CRITICAL** factor for reliable push notifications on Android, especially for:
- Background notifications
- Killed state notifications
- Real-time order alerts

### The Problem

Android manufacturers implement aggressive battery-saving measures that can:
- ❌ Kill your app in the background
- ❌ Prevent FCM messages from being received
- ❌ Delay notifications by hours
- ❌ Stop your app from waking up

### Manufacturers with Aggressive Battery Management

**Most Aggressive** (Require special handling):
1. **Xiaomi** (MIUI) - Very aggressive, multiple settings
2. **Huawei** (EMUI) - Extremely aggressive
3. **OnePlus** (OxygenOS) - Aggressive background restrictions
4. **Oppo** (ColorOS) - Similar to OnePlus
5. **Vivo** (FuntouchOS) - Very restrictive
6. **Samsung** (One UI) - Moderate but has "Deep Sleep" mode
7. **Realme** (Realme UI) - Similar to Oppo

**Less Aggressive**:
- Google Pixel (Stock Android)
- Motorola
- Nokia (Android One)

---

## ✅ What We Implemented

### 1. Permission Added
```xml
<uses-permission android:name="android.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS" />
```

### 2. Battery Status Check
The app now automatically checks if battery optimization is enabled:

```kotlin
private fun checkBatteryOptimization() {
    val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
    isBatteryOptimized = !powerManager.isIgnoringBatteryOptimizations(packageName)
}
```

### 3. Visual Warning
If battery optimization is enabled, the app shows a **red warning card**:

```
┌─────────────────────────────────────┐
│ ⚠️ Battery Optimization Enabled    │
│                                     │
│ Notifications may be delayed or     │
│ missed when app is in background.   │
│                                     │
│ [Disable Battery Optimization]      │
└─────────────────────────────────────┘
```

### 4. One-Tap Fix
Tapping the button opens Android settings where user can disable optimization.

---

## 📱 How It Works

### When App Opens
1. ✅ Checks battery optimization status
2. ✅ Shows warning if enabled
3. ✅ Provides one-tap solution

### User Flow
```
User opens app
    ↓
App checks battery status
    ↓
If optimized → Shows red warning
    ↓
User taps "Disable Battery Optimization"
    ↓
Android settings opens
    ↓
User allows exemption
    ↓
Returns to app → Green checkmark shows ✅
```

---

## 🧪 Testing Battery Optimization

### Test 1: Check Current Status
```bash
# Check if app is battery optimized
adb shell dumpsys deviceidle whitelist | grep prashantpizza

# If empty = app IS optimized (bad)
# If shows package name = app is NOT optimized (good)
```

### Test 2: Simulate Battery Optimization
```bash
# Enable battery optimization (for testing)
adb shell dumpsys deviceidle whitelist -prashantpizza

# Test notification delivery
# Send FCM → May be delayed or missed

# Disable battery optimization
adb shell dumpsys deviceidle whitelist +prashantpizza

# Test notification delivery again
# Send FCM → Should arrive immediately
```

### Test 3: Force Doze Mode
```bash
# Put device in Doze mode (simulates deep sleep)
adb shell dumpsys battery unplug
adb shell dumpsys deviceidle force-idle

# Send FCM notification
# If battery optimized: Won't receive
# If exempted: Will receive immediately

# Exit Doze mode
adb shell dumpsys deviceidle unforce
adb shell dumpsys battery reset
```

---

## 🔧 Manufacturer-Specific Settings

### Xiaomi (MIUI)
Users need to configure **multiple settings**:

1. **Battery Optimization**
   - Settings → Apps → Manage apps → Your App
   - Battery saver → No restrictions

2. **Autostart**
   - Settings → Apps → Manage apps → Your App
   - Autostart → Enable

3. **Battery Saver**
   - Settings → Battery & performance
   - Battery saver → Your App → No restrictions

4. **App Lock**
   - Security → App lock → Your App → Disable

### Huawei (EMUI)
1. **Battery Optimization**
   - Settings → Apps → Your App
   - Battery → App launch → Manage manually
   - Enable: Auto-launch, Secondary launch, Run in background

2. **Protected Apps**
   - Settings → Battery → App launch
   - Find your app → Toggle ON

### OnePlus/Oppo/Realme
1. **Battery Optimization**
   - Settings → Battery → Battery optimization
   - Your App → Don't optimize

2. **App Auto-Launch**
   - Settings → Battery → App auto-launch
   - Your App → Enable

### Samsung
1. **Battery Optimization**
   - Settings → Apps → Your App
   - Battery → Optimize battery usage → All apps
   - Your App → Disable

2. **Sleeping Apps**
   - Settings → Battery → Background usage limits
   - Sleeping apps → Remove your app if listed
   - Deep sleeping apps → Remove your app if listed

---

## 📊 Impact on Notification Delivery

### With Battery Optimization (❌ Bad)
```
Notification sent at 10:00 AM
    ↓
Device in Doze mode
    ↓
Notification delayed until next maintenance window
    ↓
User receives at 10:30 AM (30 min delay!)
```

### Without Battery Optimization (✅ Good)
```
Notification sent at 10:00 AM
    ↓
Device in Doze mode
    ↓
App exempted from Doze
    ↓
Notification received immediately at 10:00 AM
```

---

## 🎯 Best Practices

### 1. Request at Right Time
✅ **Good**: Request after user sees value (after first order)
❌ **Bad**: Request immediately on first launch

### 2. Explain Why
Always explain the benefit:
```
"Disable battery optimization to receive order notifications 
immediately, even when your phone is in deep sleep mode."
```

### 3. Show Impact
Show statistics:
```
"Users with battery optimization disabled receive orders 
30 seconds faster on average."
```

### 4. Provide Alternatives
If user denies:
```
"You can still receive notifications, but they may be delayed 
by up to 30 minutes when your phone is in deep sleep."
```

---

## 🚨 Important Notes

### Google Play Policy
- ✅ **Allowed**: Request battery optimization exemption for messaging/order apps
- ❌ **Not Allowed**: Request for apps that don't need real-time notifications
- ✅ **Your App**: Qualifies as it's for time-sensitive order notifications

### User Experience
- Don't force users to disable optimization
- Explain the benefits clearly
- Show visual feedback (green checkmark when disabled)
- Recheck status when app resumes

### Testing
- Always test on physical devices
- Test on different manufacturers (Xiaomi, Samsung, OnePlus)
- Test in Doze mode
- Test with app killed

---

## 📱 User Instructions

### For End Users

**To ensure you receive order notifications immediately:**

1. **Open the app**
2. **Look for the red warning card** at the top
3. **Tap "Disable Battery Optimization"**
4. **Select "Allow"** in the system dialog
5. **Return to app** - you should see a green checkmark ✅

**Additional steps for Xiaomi users:**
1. Go to Settings → Apps → Manage apps
2. Find "Order Notification App"
3. Enable "Autostart"
4. Set Battery saver to "No restrictions"

**Additional steps for Huawei users:**
1. Go to Settings → Battery → App launch
2. Find "Order Notification App"
3. Toggle to "Manage manually"
4. Enable all three options

---

## 🔍 Debugging Battery Issues

### Check if Battery Optimization is the Problem

```bash
# 1. Check current status
adb shell dumpsys deviceidle whitelist | grep prashantpizza

# 2. Disable battery optimization
adb shell dumpsys deviceidle whitelist +com.prashantpizza.nofsdotaca

# 3. Send test notification
# Use Firebase Console or curl

# 4. Check if notification arrives immediately
# If yes → Battery optimization was the issue
# If no → Check other factors (FCM token, permissions, etc.)
```

### Monitor Battery Restrictions

```bash
# Check app standby bucket
adb shell am get-standby-bucket com.prashantpizza.nofsdotaca

# Buckets:
# 5 = Active (best)
# 10 = Working set
# 20 = Frequent
# 30 = Rare
# 40 = Restricted (worst)

# Force to active bucket (for testing)
adb shell am set-standby-bucket com.prashantpizza.nofsdotaca active
```

---

## 📈 Analytics to Track

Consider tracking these metrics:

1. **Battery Optimization Status**
   - % of users with optimization disabled
   - Conversion rate of the "Disable" button

2. **Notification Delivery Time**
   - Average time from send to receive
   - Compare optimized vs non-optimized users

3. **Notification Success Rate**
   - % of notifications successfully delivered
   - Compare by manufacturer

4. **User Actions**
   - How many users disable optimization
   - How many users re-enable it later

---

## ✅ Summary

### What Battery Optimization Affects
- ✅ FCM message delivery
- ✅ Background app wake-up
- ✅ Notification display timing
- ✅ App responsiveness when killed

### What We Implemented
- ✅ Permission to request exemption
- ✅ Automatic status checking
- ✅ Visual warning when enabled
- ✅ One-tap solution
- ✅ Green checkmark when disabled
- ✅ Recheck on app resume

### User Impact
- ✅ Immediate notification delivery
- ✅ No delays in Doze mode
- ✅ Reliable background operation
- ✅ Better user experience

---

**Battery optimization exemption is CRITICAL for your order notification system. Without it, notifications may be delayed by 15-30 minutes or more!** ⚠️

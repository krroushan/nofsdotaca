# Testing Guide for Order Notification System

This guide provides detailed instructions for testing the Firebase push notification system in all scenarios.

## Prerequisites

1. ✅ App installed on device
2. ✅ All permissions granted
3. ✅ Firebase configured with valid `google-services.json`
4. ✅ FCM token obtained from the app

## Test Scenarios

### 1. Test Foreground Notifications (Dialog)

**Objective**: Verify that notifications show as dialog when app is open

**Steps**:
1. Open the app
2. Keep it in foreground (visible on screen)
3. Send FCM notification using one of these methods:
   - Tap "Send Test Notification" button in the app
   - Send FCM message from backend/Firebase Console
4. Observe the dialog appearing over the app

**Expected Result**:
- ✅ Dialog appears immediately
- ✅ Shows order details (ID, customer, items, amount, address, time)
- ✅ Accept and Reject buttons are visible
- ✅ Can dismiss by tapping X or outside
- ✅ No full-screen activity launches

**ADB Command to Send Test**:
```bash
# App must be open
adb shell am start -n com.prashantpizza.nofsdotaca/.MainActivity
```

---

### 2. Test Background Notifications (Full-Screen)

**Objective**: Verify full-screen notification when app is in background

**Steps**:
1. Open the app
2. Press Home button to send app to background
   ```bash
   adb shell input keyevent KEYCODE_HOME
   ```
3. Send FCM notification
4. Observe full-screen activity appearing

**Expected Result**:
- ✅ Full-screen activity appears immediately
- ✅ Shows over current app
- ✅ Displays order details in call-style UI
- ✅ Ringtone plays (looping)
- ✅ Device vibrates with pattern
- ✅ Accept (green) and Reject (red) buttons visible
- ✅ Auto-dismisses after 60 seconds if no action taken

---

### 3. Test Killed State Notifications (Full-Screen)

**Objective**: Verify notification works when app is force-stopped

**Steps**:
1. Force stop the app
   ```bash
   adb shell am force-stop com.prashantpizza.nofsdotaca
   ```
2. Verify app is not running in recent apps
3. Send FCM notification
4. Observe full-screen activity appearing

**Expected Result**:
- ✅ Full-screen activity appears immediately
- ✅ App launches automatically
- ✅ Screen wakes up if device is sleeping
- ✅ Ringtone plays
- ✅ Device vibrates
- ✅ All UI elements visible and functional

---

### 4. Test Lockscreen Notifications

**Objective**: Verify notification appears over lockscreen

**Steps**:
1. Lock the device
   ```bash
   adb shell input keyevent KEYCODE_POWER
   ```
2. Verify screen is off/locked
3. Send FCM notification
4. Observe screen waking up and showing notification

**Expected Result**:
- ✅ Screen turns on automatically
- ✅ Full-screen activity appears over lockscreen
- ✅ Can interact with Accept/Reject without unlocking
- ✅ Ringtone plays
- ✅ Device vibrates

---

### 5. Test Accept Action

**Objective**: Verify Accept button functionality

**Steps**:
1. Trigger any notification (foreground or background)
2. Tap "Accept" button
3. Check logs for API call

**Expected Result**:
- ✅ Notification/dialog dismisses immediately
- ✅ Ringtone and vibration stop
- ✅ Toast message: "Order accepted"
- ✅ Log shows: "Order accepted successfully"
- ✅ API call made to backend (check logs)

**Check Logs**:
```bash
adb logcat | grep -E "OrderRepository|OrderNotificationActivity"
```

---

### 6. Test Reject Action

**Objective**: Verify Reject button functionality

**Steps**:
1. Trigger any notification (foreground or background)
2. Tap "Reject" button
3. Check logs for API call

**Expected Result**:
- ✅ Notification/dialog dismisses immediately
- ✅ Ringtone and vibration stop
- ✅ Toast message: "Order rejected"
- ✅ Log shows: "Order rejected successfully"
- ✅ API call made to backend (check logs)

---

### 7. Test Auto-Dismiss

**Objective**: Verify notification auto-dismisses after timeout

**Steps**:
1. Trigger background notification (full-screen)
2. Do NOT tap any button
3. Wait 60 seconds
4. Observe notification dismissing automatically

**Expected Result**:
- ✅ Notification dismisses after exactly 60 seconds
- ✅ Ringtone stops
- ✅ Vibration stops
- ✅ Returns to previous screen
- ✅ Log shows: "Auto-dismissing notification after timeout"

---

### 8. Test Multiple Notifications

**Objective**: Verify handling of multiple rapid notifications

**Steps**:
1. Send first notification
2. Immediately send second notification (within 5 seconds)
3. Observe behavior

**Expected Result**:
- ✅ Second notification replaces first
- ✅ No duplicate activities
- ✅ Latest order details shown
- ✅ Previous notification dismissed

---

### 9. Test Permissions

**Objective**: Verify permission handling

**Steps**:
1. Fresh install or clear app data
   ```bash
   adb shell pm clear com.prashantpizza.nofsdotaca
   ```
2. Launch app
3. Observe permission requests

**Expected Result**:
- ✅ Notification permission requested (Android 13+)
- ✅ Full-screen intent permission requested (Android 14+)
- ✅ Can grant/deny permissions
- ✅ App handles denial gracefully
- ✅ "Request Permissions" button works

---

### 10. Test FCM Token

**Objective**: Verify FCM token generation and display

**Steps**:
1. Open app
2. Check FCM token in the UI
3. Copy token
4. Verify in Firebase Console

**Expected Result**:
- ✅ Token displays in app (not "Loading...")
- ✅ Token is valid format (long alphanumeric string)
- ✅ Can copy token
- ✅ Token works for sending notifications

**Check Token in Logs**:
```bash
adb logcat | grep "FCM Token"
```

---

## Sending Test Notifications

### Method 1: In-App Test Button

Easiest method for quick testing:

1. Open app
2. Tap "Send Test Notification"
3. Press Home or lock screen to test background behavior

### Method 2: Firebase Console

1. Go to Firebase Console → Cloud Messaging
2. Click "Send your first message"
3. Fill in:
   - **Notification title**: New Order
   - **Notification text**: You have a new order!
4. Click "Send test message"
5. Paste FCM token from app
6. Click "Test"

### Method 3: curl Command

```bash
# Replace YOUR_SERVER_KEY and YOUR_FCM_TOKEN

curl -X POST https://fcm.googleapis.com/fcm/send \
  -H "Authorization: Bearer YOUR_SERVER_KEY" \
  -H "Content-Type: application/json" \
  -d '{
    "to": "YOUR_FCM_TOKEN",
    "priority": "high",
    "data": {
      "type": "new_order",
      "title": "New Order #123",
      "body": "You have a new order from John Doe",
      "orderId": "ORD-12345",
      "customerName": "John Doe",
      "items": "2x Pizza Margherita, 1x Coca Cola",
      "amount": "$25.99",
      "address": "123 Main Street, Apt 4B"
    }
  }'
```

### Method 4: Postman

```
POST https://fcm.googleapis.com/fcm/send

Headers:
  Authorization: Bearer YOUR_SERVER_KEY
  Content-Type: application/json

Body (raw JSON):
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

---

## Debugging

### Check Logs

```bash
# All app logs
adb logcat | grep "prashantpizza"

# FCM service logs
adb logcat | grep "OrderFCMService"

# Notification activity logs
adb logcat | grep "OrderNotificationActivity"

# Repository logs
adb logcat | grep "OrderRepository"

# App state logs
adb logcat | grep "AppStateTracker"
```

### Common Issues

#### 1. Notification Not Appearing

**Check**:
```bash
# Check if notification permission granted
adb shell dumpsys notification_listener

# Check if app can use full screen intent
adb shell dumpsys notification
```

**Solutions**:
- Grant notification permission
- Disable battery optimization
- Check Do Not Disturb settings

#### 2. No Sound/Vibration

**Check**:
```bash
# Check notification volume
adb shell media volume --show

# Check DND status
adb shell settings get global zen_mode
```

**Solutions**:
- Increase notification volume
- Disable Do Not Disturb
- Check app notification settings

#### 3. Full-Screen Not Working

**Check**:
```bash
# Check full screen intent permission (Android 14+)
adb shell dumpsys notification | grep "fullScreenIntent"
```

**Solutions**:
- Go to Settings → Apps → Your App → Full Screen Intent → Allow
- Disable battery saver
- Test on physical device (not emulator)

#### 4. FCM Token Not Generating

**Check**:
```bash
# Check Firebase initialization
adb logcat | grep "Firebase"
```

**Solutions**:
- Verify `google-services.json` is correct
- Check internet connection
- Rebuild project: `./gradlew clean build`

---

## Test Checklist

Use this checklist to ensure all features are working:

- [ ] Foreground notification shows dialog
- [ ] Background notification shows full-screen
- [ ] Killed state notification shows full-screen
- [ ] Lockscreen notification works
- [ ] Accept button works
- [ ] Reject button works
- [ ] Auto-dismiss after 60 seconds
- [ ] Ringtone plays and loops
- [ ] Vibration works
- [ ] Sound stops on action
- [ ] Vibration stops on action
- [ ] FCM token displays correctly
- [ ] Test button works
- [ ] Permissions requested properly
- [ ] Order details display correctly
- [ ] Toast messages appear
- [ ] API calls logged
- [ ] Multiple notifications handled
- [ ] Screen wakes on notification
- [ ] Works over other apps

---

## Performance Testing

### Battery Impact
```bash
# Monitor battery usage
adb shell dumpsys batterystats | grep prashantpizza
```

### Memory Usage
```bash
# Monitor memory
adb shell dumpsys meminfo com.prashantpizza.nofsdotaca
```

### Network Usage
```bash
# Monitor network
adb shell dumpsys netstats | grep prashantpizza
```

---

## Automated Testing Script

Save this as `test_notifications.sh`:

```bash
#!/bin/bash

echo "=== Order Notification Testing Script ==="
echo ""

# Get FCM token
echo "1. Getting FCM token..."
adb logcat -d | grep "FCM Token" | tail -1

echo ""
echo "2. Testing Background State..."
adb shell am force-stop com.prashantpizza.nofsdotaca
sleep 2
adb shell am start -n com.prashantpizza.nofsdotaca/.MainActivity
sleep 3
adb shell input keyevent KEYCODE_HOME
echo "   App sent to background. Send FCM notification now."
echo "   Press Enter when done..."
read

echo ""
echo "3. Testing Killed State..."
adb shell am force-stop com.prashantpizza.nofsdotaca
echo "   App killed. Send FCM notification now."
echo "   Press Enter when done..."
read

echo ""
echo "4. Testing Foreground State..."
adb shell am start -n com.prashantpizza.nofsdotaca/.MainActivity
sleep 2
echo "   App in foreground. Send FCM notification now."
echo "   Press Enter when done..."
read

echo ""
echo "=== Testing Complete ==="
echo "Check device for results."
```

Run with:
```bash
chmod +x test_notifications.sh
./test_notifications.sh
```

---

## Conclusion

This comprehensive testing guide ensures all notification features work correctly across all Android versions and app states. Follow each test scenario and check off the test checklist to verify complete functionality.

For any issues, refer to the Debugging section and check logs for detailed error messages.

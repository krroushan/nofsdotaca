# 🔔 Custom Ringtone Guide

## How to Add Your Own Ringtone

### Step 1: Prepare Your Ringtone File

**Supported Formats:**
- `.mp3` (Recommended)
- `.ogg`
- `.wav`
- `.m4a`

**File Requirements:**
- File name must be **lowercase**
- No spaces or special characters
- Use underscores instead of spaces
- Example: `neworder.mp3`

**Recommended Settings:**
- Duration: 3-10 seconds
- Bitrate: 128-192 kbps
- Sample Rate: 44.1 kHz
- File Size: < 500 KB (for faster loading)

---

### Step 2: Add File to Project

1. Place your ringtone file in:
   ```
   app/src/main/res/raw/neworder.mp3
   ```

2. The file MUST be named exactly: `neworder.mp3` (or `.ogg`, `.wav`, `.m4a`)

3. If the `raw` folder doesn't exist, create it:
   ```
   app/src/main/res/raw/
   ```

---

### Step 3: Rebuild and Test

The code is already configured to:
1. ✅ Look for `neworder` in `res/raw/`
2. ✅ Use custom ringtone if found
3. ✅ Fallback to system default if not found

**No code changes needed!** Just add the file and rebuild.

---

## 📁 Project Structure

```
app/
├── src/
│   └── main/
│       ├── res/
│       │   └── raw/
│       │       └── neworder.mp3  ← Add your file here
│       └── java/
│           └── com/prashantpizza/nofsdotaca/
```

---

## 🎵 Where to Find Free Ringtones

1. **Zedge** - https://www.zedge.net/ringtones
2. **Notification Sounds** - https://notificationsounds.com/
3. **Free Sound Effects** - https://www.freesoundeffects.com/
4. **YouTube Audio Library** - https://www.youtube.com/audiolibrary

---

## 🧪 Testing

### Test Foreground (App Open):
```bash
cd backend
npm run send
```
- Dialog should appear with your custom ringtone

### Test Background (App Closed):
```bash
adb shell input keyevent KEYCODE_HOME
cd backend
npm run send
```
- Full-screen activity should appear with your custom ringtone

---

## 🔍 Verify Custom Ringtone is Used

Check the logs:
```bash
adb logcat | grep "Ringtone started"
```

You should see:
- `Ringtone started (custom: true)` ← Using your custom ringtone
- `Ringtone started (custom: false)` ← Using system default

---

## 🎨 Multiple Ringtones (Advanced)

If you want different ringtones for different scenarios:

### Option 1: Different ringtones for foreground vs background

Add two files:
- `res/raw/order_ringtone_foreground.mp3`
- `res/raw/order_ringtone_background.mp3`

Update code to check for these specific files.

### Option 2: Different ringtones per order type

Add multiple files:
- `res/raw/order_ringtone_urgent.mp3`
- `res/raw/order_ringtone_normal.mp3`
- `res/raw/order_ringtone_vip.mp3`

Pass the ringtone type in the FCM data payload.

---

## ❓ Troubleshooting

### Ringtone not playing?

1. **Check file name:**
   - Must be exactly `order_ringtone.mp3` (lowercase, underscore)
   - No spaces or special characters

2. **Check file location:**
   ```
   app/src/main/res/raw/neworder.mp3
   ```

3. **Check file format:**
   - Use `.mp3` or `.ogg` (most compatible)
   - Avoid `.m4a` on older devices

4. **Rebuild the app:**
   - Clean build: `./gradlew clean`
   - Rebuild: `./gradlew assembleDebug`
   - Reinstall: `./gradlew installDebug`

5. **Check logs:**
   ```bash
   adb logcat | grep -E "Ringtone|MediaPlayer"
   ```

### Still using system ringtone?

If the custom ringtone file is not found, the app will automatically fallback to the system default ringtone. This is by design for reliability.

---

## 📝 Example Ringtone Files

You can use these free notification sounds:

1. **Swiggy-style** - Search for "order notification sound"
2. **Zomato-style** - Search for "food delivery notification"
3. **Uber-style** - Search for "ride notification sound"
4. **Custom** - Create your own using audio editing software

---

## ✅ Summary

1. Add `order_ringtone.mp3` to `app/src/main/res/raw/`
2. Rebuild the app
3. Test - it will automatically use your custom ringtone!

**That's it!** 🎉

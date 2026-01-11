#!/bin/bash

echo "🚀 FCM Full-Screen Notification Test"
echo "====================================="
echo ""

# Check if Node.js is installed
if ! command -v node &> /dev/null; then
    echo "❌ Node.js is not installed!"
    echo "Please install Node.js from: https://nodejs.org/"
    exit 1
fi

echo "✅ Node.js found: $(node --version)"
echo ""

# Check if dependencies are installed
if [ ! -d "node_modules" ]; then
    echo "📦 Installing dependencies..."
    npm install
    echo ""
fi

# Check if device is connected
if ! command -v adb &> /dev/null; then
    echo "⚠️  ADB not found. Install Android SDK Platform Tools."
else
    echo "📱 Checking connected devices..."
    adb devices
    echo ""
    
    # Send app to background
    echo "📲 Sending app to background..."
    adb shell input keyevent KEYCODE_HOME
    sleep 1
    echo ""
    
    # Clear and start watching logs
    echo "📋 Starting log monitoring..."
    adb logcat -c
    adb logcat | grep -E "OrderFCMService|NotificationHelper|OrderNotificationActivity" &
    LOGCAT_PID=$!
    echo ""
fi

# Send notification
echo "🔔 Sending FCM notification..."
echo ""
node send-test-notification.js

# Wait a bit for logs
sleep 3

# Kill logcat
if [ ! -z "$LOGCAT_PID" ]; then
    kill $LOGCAT_PID 2>/dev/null
fi

echo ""
echo "✅ Test completed!"
echo "📱 Check your device for the full-screen notification"

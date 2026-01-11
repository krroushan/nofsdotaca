# Backend Integration Guide

This guide explains how to integrate your backend with the Order Notification System.

## Overview

The app requires two main integrations:
1. **Sending FCM Push Notifications** - To trigger order notifications
2. **Accept/Reject API Endpoints** - To handle order actions

---

## 1. Sending FCM Push Notifications

### Prerequisites

- Firebase project set up
- Server key from Firebase Console (Settings → Cloud Messaging → Server Key)
- Device FCM token (obtained from the app)

### FCM Message Format

**Important**: Use `data` payload (NOT `notification` payload) for custom handling in all app states.

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
    "items": "2x Pizza Margherita, 1x Coca Cola",
    "amount": "$25.99",
    "address": "123 Main Street, Apt 4B",
    "timestamp": "1704067200000"
  }
}
```

### Field Descriptions

| Field | Type | Required | Description | Example |
|-------|------|----------|-------------|---------|
| `type` | String | Yes | Must be "new_order" | "new_order" |
| `title` | String | Yes | Notification title | "New Order" |
| `body` | String | Yes | Notification body | "You have a new order!" |
| `orderId` | String | Yes | Unique order identifier | "ORD-12345" |
| `customerName` | String | Yes | Customer's name | "John Doe" |
| `items` | String | Yes | Order items description | "2x Pizza, 1x Coke" |
| `amount` | String | Yes | Total amount with currency | "$25.99" |
| `address` | String | Yes | Delivery address | "123 Main St" |
| `timestamp` | String | No | Unix timestamp in milliseconds | "1704067200000" |

---

## 2. Implementation Examples

### Node.js (Express + Firebase Admin SDK)

```javascript
const admin = require('firebase-admin');
const express = require('express');

// Initialize Firebase Admin
admin.initializeApp({
  credential: admin.credential.cert('./serviceAccountKey.json')
});

const app = express();
app.use(express.json());

// Send order notification
app.post('/api/orders/notify', async (req, res) => {
  const { fcmToken, orderId, customerName, items, amount, address } = req.body;
  
  const message = {
    token: fcmToken,
    data: {
      type: 'new_order',
      title: 'New Order',
      body: 'You have a new order!',
      orderId: orderId,
      customerName: customerName,
      items: items,
      amount: amount,
      address: address,
      timestamp: Date.now().toString()
    },
    android: {
      priority: 'high'
    }
  };
  
  try {
    const response = await admin.messaging().send(message);
    console.log('Successfully sent message:', response);
    res.json({ success: true, messageId: response });
  } catch (error) {
    console.error('Error sending message:', error);
    res.status(500).json({ success: false, error: error.message });
  }
});

app.listen(3000, () => {
  console.log('Server running on port 3000');
});
```

### Python (Flask + Firebase Admin SDK)

```python
from flask import Flask, request, jsonify
import firebase_admin
from firebase_admin import credentials, messaging
import time

# Initialize Firebase Admin
cred = credentials.Certificate('./serviceAccountKey.json')
firebase_admin.initialize_app(cred)

app = Flask(__name__)

@app.route('/api/orders/notify', methods=['POST'])
def send_order_notification():
    data = request.json
    
    message = messaging.Message(
        data={
            'type': 'new_order',
            'title': 'New Order',
            'body': 'You have a new order!',
            'orderId': data['orderId'],
            'customerName': data['customerName'],
            'items': data['items'],
            'amount': data['amount'],
            'address': data['address'],
            'timestamp': str(int(time.time() * 1000))
        },
        token=data['fcmToken'],
        android=messaging.AndroidConfig(
            priority='high'
        )
    )
    
    try:
        response = messaging.send(message)
        print('Successfully sent message:', response)
        return jsonify({'success': True, 'messageId': response})
    except Exception as e:
        print('Error sending message:', str(e))
        return jsonify({'success': False, 'error': str(e)}), 500

if __name__ == '__main__':
    app.run(port=3000)
```

### PHP (Laravel)

```php
<?php

namespace App\Http\Controllers;

use Illuminate\Http\Request;
use Kreait\Firebase\Factory;
use Kreait\Firebase\Messaging\CloudMessage;

class OrderNotificationController extends Controller
{
    public function sendOrderNotification(Request $request)
    {
        $factory = (new Factory)->withServiceAccount('./serviceAccountKey.json');
        $messaging = $factory->createMessaging();
        
        $message = CloudMessage::withTarget('token', $request->fcmToken)
            ->withData([
                'type' => 'new_order',
                'title' => 'New Order',
                'body' => 'You have a new order!',
                'orderId' => $request->orderId,
                'customerName' => $request->customerName,
                'items' => $request->items,
                'amount' => $request->amount,
                'address' => $request->address,
                'timestamp' => (string)(time() * 1000)
            ])
            ->withAndroidConfig([
                'priority' => 'high'
            ]);
        
        try {
            $response = $messaging->send($message);
            return response()->json(['success' => true, 'messageId' => $response]);
        } catch (\Exception $e) {
            return response()->json(['success' => false, 'error' => $e->getMessage()], 500);
        }
    }
}
```

### Java (Spring Boot)

```java
import com.google.firebase.messaging.*;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
public class OrderNotificationController {
    
    @PostMapping("/notify")
    public ResponseEntity<?> sendOrderNotification(@RequestBody OrderNotificationRequest request) {
        Map<String, String> data = new HashMap<>();
        data.put("type", "new_order");
        data.put("title", "New Order");
        data.put("body", "You have a new order!");
        data.put("orderId", request.getOrderId());
        data.put("customerName", request.getCustomerName());
        data.put("items", request.getItems());
        data.put("amount", request.getAmount());
        data.put("address", request.getAddress());
        data.put("timestamp", String.valueOf(System.currentTimeMillis()));
        
        Message message = Message.builder()
            .setToken(request.getFcmToken())
            .putAllData(data)
            .setAndroidConfig(AndroidConfig.builder()
                .setPriority(AndroidConfig.Priority.HIGH)
                .build())
            .build();
        
        try {
            String response = FirebaseMessaging.getInstance().send(message);
            return ResponseEntity.ok(Map.of("success", true, "messageId", response));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                .body(Map.of("success", false, "error", e.getMessage()));
        }
    }
}
```

---

## 3. Accept/Reject API Endpoints

The app will call your backend when the user accepts or rejects an order.

### Expected Endpoint

```
POST /api/orders/action
Content-Type: application/json
Authorization: Bearer <USER_TOKEN>  (if using authentication)
```

### Request Body

```json
{
  "orderId": "ORD-12345",
  "action": "accept",
  "timestamp": 1704067200000
}
```

| Field | Type | Description |
|-------|------|-------------|
| `orderId` | String | The order ID |
| `action` | String | Either "accept" or "reject" |
| `timestamp` | Long | Unix timestamp in milliseconds |

### Response Format

```json
{
  "success": true,
  "message": "Order accepted successfully"
}
```

### Implementation Example (Node.js)

```javascript
app.post('/api/orders/action', async (req, res) => {
  const { orderId, action, timestamp } = req.body;
  
  // Validate request
  if (!orderId || !action || !['accept', 'reject'].includes(action)) {
    return res.status(400).json({
      success: false,
      message: 'Invalid request parameters'
    });
  }
  
  try {
    // Update order in database
    await db.collection('orders').doc(orderId).update({
      status: action === 'accept' ? 'accepted' : 'rejected',
      actionTimestamp: timestamp,
      updatedAt: admin.firestore.FieldValue.serverTimestamp()
    });
    
    // Send confirmation notification to customer (optional)
    if (action === 'accept') {
      await sendCustomerNotification(orderId, 'Your order has been accepted!');
    }
    
    res.json({
      success: true,
      message: `Order ${action}ed successfully`
    });
  } catch (error) {
    console.error('Error updating order:', error);
    res.status(500).json({
      success: false,
      message: 'Failed to update order'
    });
  }
});
```

---

## 4. Updating the Android App

To connect the app to your backend, update `OrderRepository.kt`:

```kotlin
// File: app/src/main/java/com/prashantpizza/nofsdotaca/repository/OrderRepository.kt

companion object {
    private const val BASE_URL = "https://your-api-endpoint.com/"  // Update this
}

// If using authentication, add token to requests
private val apiService: OrderApiService by lazy {
    val client = OkHttpClient.Builder()
        .addInterceptor { chain ->
            val request = chain.request().newBuilder()
                .addHeader("Authorization", "Bearer YOUR_AUTH_TOKEN")
                .build()
            chain.proceed(request)
        }
        .build()
    
    // ... rest of the code
}
```

---

## 5. Testing the Integration

### Test FCM Notification

```bash
# Using curl
curl -X POST https://your-backend.com/api/orders/notify \
  -H "Content-Type: application/json" \
  -d '{
    "fcmToken": "DEVICE_FCM_TOKEN_FROM_APP",
    "orderId": "ORD-TEST-001",
    "customerName": "Test Customer",
    "items": "1x Test Item",
    "amount": "$10.00",
    "address": "Test Address"
  }'
```

### Test Accept/Reject Endpoint

```bash
# Test Accept
curl -X POST https://your-backend.com/api/orders/action \
  -H "Content-Type: application/json" \
  -d '{
    "orderId": "ORD-TEST-001",
    "action": "accept",
    "timestamp": 1704067200000
  }'

# Test Reject
curl -X POST https://your-backend.com/api/orders/action \
  -H "Content-Type: application/json" \
  -d '{
    "orderId": "ORD-TEST-001",
    "action": "reject",
    "timestamp": 1704067200000
  }'
```

---

## 6. Best Practices

### FCM Notifications

1. **Use Data Payload**: Always use `data` (not `notification`) for custom handling
2. **Set High Priority**: Use `priority: "high"` for immediate delivery
3. **Include All Fields**: Ensure all required fields are present
4. **Handle Errors**: Implement retry logic for failed sends
5. **Log Everything**: Log all FCM sends for debugging

### API Endpoints

1. **Validate Input**: Always validate orderId and action
2. **Use Authentication**: Secure endpoints with proper auth
3. **Return Proper Status Codes**: 200 for success, 400 for bad request, 500 for errors
4. **Log Actions**: Log all accept/reject actions for audit trail
5. **Send Confirmations**: Optionally notify customers of status changes

### Security

1. **Protect Server Key**: Never expose Firebase server key in client code
2. **Validate Tokens**: Verify FCM tokens before sending
3. **Rate Limiting**: Implement rate limiting on endpoints
4. **Authentication**: Use JWT or similar for API authentication
5. **HTTPS Only**: Always use HTTPS for API calls

---

## 7. Monitoring & Analytics

### Track FCM Delivery

```javascript
// Log FCM sends
console.log('FCM Notification Sent:', {
  orderId: orderId,
  fcmToken: fcmToken.substring(0, 20) + '...',
  timestamp: new Date().toISOString(),
  success: true
});

// Track delivery status
admin.messaging().send(message)
  .then(response => {
    // Log success
    db.collection('fcm_logs').add({
      orderId: orderId,
      messageId: response,
      status: 'sent',
      timestamp: admin.firestore.FieldValue.serverTimestamp()
    });
  })
  .catch(error => {
    // Log error
    db.collection('fcm_logs').add({
      orderId: orderId,
      error: error.message,
      status: 'failed',
      timestamp: admin.firestore.FieldValue.serverTimestamp()
    });
  });
```

### Track Order Actions

```javascript
// Log accept/reject actions
await db.collection('order_actions').add({
  orderId: orderId,
  action: action,
  timestamp: timestamp,
  source: 'mobile_app',
  createdAt: admin.firestore.FieldValue.serverTimestamp()
});
```

---

## 8. Troubleshooting

### FCM Not Delivered

**Possible Causes**:
- Invalid FCM token
- Token expired/refreshed
- Device offline
- App uninstalled

**Solutions**:
- Validate token before sending
- Implement token refresh mechanism
- Handle FCM error codes properly
- Remove invalid tokens from database

### API Calls Failing

**Possible Causes**:
- Network issues
- Authentication failure
- Invalid request format
- Server errors

**Solutions**:
- Check network connectivity
- Verify auth tokens
- Validate request body
- Check server logs

---

## 9. Sample Postman Collection

Import this collection for easy testing:

```json
{
  "info": {
    "name": "Order Notification API",
    "schema": "https://schema.getpostman.com/json/collection/v2.1.0/collection.json"
  },
  "item": [
    {
      "name": "Send Order Notification",
      "request": {
        "method": "POST",
        "header": [
          {
            "key": "Content-Type",
            "value": "application/json"
          }
        ],
        "body": {
          "mode": "raw",
          "raw": "{\n  \"fcmToken\": \"YOUR_FCM_TOKEN\",\n  \"orderId\": \"ORD-12345\",\n  \"customerName\": \"John Doe\",\n  \"items\": \"2x Pizza, 1x Coke\",\n  \"amount\": \"$25.99\",\n  \"address\": \"123 Main St\"\n}"
        },
        "url": {
          "raw": "https://your-backend.com/api/orders/notify",
          "protocol": "https",
          "host": ["your-backend", "com"],
          "path": ["api", "orders", "notify"]
        }
      }
    },
    {
      "name": "Accept Order",
      "request": {
        "method": "POST",
        "header": [
          {
            "key": "Content-Type",
            "value": "application/json"
          }
        ],
        "body": {
          "mode": "raw",
          "raw": "{\n  \"orderId\": \"ORD-12345\",\n  \"action\": \"accept\",\n  \"timestamp\": 1704067200000\n}"
        },
        "url": {
          "raw": "https://your-backend.com/api/orders/action",
          "protocol": "https",
          "host": ["your-backend", "com"],
          "path": ["api", "orders", "action"]
        }
      }
    }
  ]
}
```

---

## 10. Support

For integration issues:

1. Check Firebase Console for FCM delivery status
2. Review backend logs for API errors
3. Test with curl/Postman before mobile app
4. Verify all required fields are present
5. Check Android app logs: `adb logcat | grep OrderRepository`

---

**Ready to integrate? Start with the Node.js example and test with curl before connecting the mobile app.**

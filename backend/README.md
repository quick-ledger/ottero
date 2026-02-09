# Exception handling
 See GlobalExceptionHandler.java. Just throw exceptions in controller and this will catch and for frontend.

# Stripe Testing
### ✅ Successful Payment
- **Card:** `4242 4242 4242 4242`
### ❌ Payment Declined (Test Grace Period)
- **Card:** `4000 0000 0000 0341`
- **Use for:** Testing `past_due` status and grace period logic

### ⚠️ Requires Authentication (3D Secure)
- **Card:** `4000 0025 0000 3155`

### 💳 Insufficient Funds
- **Card:** `4000 0000 0000 9995`

### 🔄 Charge Succeeds, Card Declined on Subscription
- **Card:** `4000 0000 0000 0341`
- **Use for:** Testing subscription payment failures

**Full list:** https://stripe.com/docs/testing#cards




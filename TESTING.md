# Testing Guide

This guide explains all the ways to test the Android Account Connector.

## 1. Quick API Test (No Android Required)

Test the mock API independently using curl:

```bash
# Start the API server (in terminal 1)
./gradlew :mock-api:run

# Run the test script (in terminal 2)
./test-api.sh
```

### Manual curl tests:

```bash
# Test server health
curl http://localhost:8080/

# Login
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"demo","password":"password"}'

# Get user info (replace TOKEN with access token from login)
curl http://localhost:8080/auth/user \
  -H "Authorization: Bearer TOKEN"

# Refresh token (replace TOKEN with refresh token from login)
curl -X POST http://localhost:8080/auth/token/refresh \
  -H "Content-Type: application/json" \
  -d '{"refreshToken":"TOKEN"}'

# Logout
curl -X POST http://localhost:8080/auth/logout \
  -H "Content-Type: application/json" \
  -d '{"refreshToken":"TOKEN"}'
```

## 2. Unit Tests

Run unit tests for individual components:

```bash
# Run all unit tests
./gradlew test

# Run specific test class
./gradlew test --tests AccountAuthenticatorTest
./gradlew test --tests ApiServiceTest

# Generate test report
./gradlew test
# Open: app/build/reports/tests/testDebugUnitTest/index.html
```

### What's tested:
- `AccountAuthenticatorTest`: AccountAuthenticator methods
- `ApiServiceTest`: Network data classes

## 3. Instrumentation Tests (Android)

Run instrumentation tests on an emulator or device:

```bash
# Start an emulator first, then:
./gradlew connectedAndroidTest

# Run specific test
./gradlew connectedAndroidTest \
  -Pandroid.testInstrumentationRunnerArguments.class=\
  com.example.accountconnector.AccountManagerTest
```

### What's tested:
- `AuthenticatorActivityTest`: Login UI interactions
- `AccountManagerTest`: Android AccountManager integration

**Prerequisites:**
- Running Android emulator or connected device
- Mock API server running on host machine

## 4. Manual Android Testing

### Setup:

1. **Start Mock API Server:**
   ```bash
   ./gradlew :mock-api:run
   ```

2. **Run Android App:**
   - Open in Android Studio
   - Run on emulator (Ctrl+R)
   - Or build: `./gradlew assembleDebug`

### Test Scenarios:

#### A. Add Account via Settings

1. Open **Settings** → **Accounts** on emulator
2. Tap **Add account**
3. Select **Account Connector**
4. Enter credentials:
   - Username: `demo`
   - Password: `password`
5. Tap **Sign In**

**Expected Result:**
- Account appears in Settings → Accounts
- Account visible in Account Connector app

#### B. Add Account via App

1. Open Account Connector app
2. Follow on-screen instructions
3. Navigate to Settings → Accounts → Add account
4. Complete login flow

#### C. Test Token Refresh

1. Add an account
2. Use `adb` to check stored tokens:
   ```bash
   adb shell dumpsys account
   ```
3. Wait 1+ hours (or manually invalidate token)
4. Request token again - should refresh automatically

#### D. Remove Account

1. Open Account Connector app
2. Tap **Remove** button on an account
   OR
3. Go to Settings → Accounts → Account Connector → Remove account

**Expected Result:**
- Account removed from system
- No longer visible in app or settings

#### E. Test Invalid Credentials

1. Go to Settings → Add account → Account Connector
2. Enter wrong credentials:
   - Username: `wrong`
   - Password: `wrong`
3. Tap Sign In

**Expected Result:**
- Error message displayed
- Account not created

#### F. Test Network Error

1. Stop the mock API server
2. Try to add an account

**Expected Result:**
- Network error message
- Graceful failure

## 5. Debugging with Android Studio

### Enable Logging:

1. Open Logcat in Android Studio
2. Filter by package: `com.example.accountconnector`
3. Look for:
   - Network requests/responses (OkHttp logs)
   - Authentication flow logs
   - Error messages

### Debug Breakpoints:

Set breakpoints in:
- `AccountAuthenticator.getAuthToken()` - Token retrieval logic
- `AuthenticatorActivity.finishLogin()` - Account creation
- `ApiClient` - Network layer

### Inspect Account Data:

```bash
# View all accounts
adb shell dumpsys account

# View specific account type
adb shell dumpsys account | grep -A 20 "com.example.accountconnector"
```

## 6. Testing Checklist

Before considering the implementation complete, test:

- [ ] API server starts without errors
- [ ] All API endpoints respond correctly
- [ ] Login with valid credentials succeeds
- [ ] Login with invalid credentials fails gracefully
- [ ] Account appears in Android Settings
- [ ] Account appears in app's MainActivity
- [ ] Access token is stored and retrieved
- [ ] Refresh token is stored securely
- [ ] Token refresh works automatically
- [ ] Account removal works from app
- [ ] Account removal works from Settings
- [ ] Network errors are handled gracefully
- [ ] Unit tests pass
- [ ] Instrumentation tests pass
- [ ] App doesn't crash on rotation
- [ ] Multiple accounts can be added

## 7. Common Testing Issues

### Issue: Can't connect to API from emulator

**Solution:**
- Ensure API is running: `curl http://localhost:8080`
- Emulator uses `10.0.2.2` to access host machine
- Check `ApiClient.kt` has correct BASE_URL

### Issue: Account not appearing in Settings

**Solution:**
- Verify app is installed
- Check AndroidManifest.xml permissions
- Restart emulator
- Run: `adb shell pm clear android.providers.settings`

### Issue: Tests fail with "No tests found"

**Solution:**
```bash
./gradlew clean
./gradlew test --rerun-tasks
```

### Issue: Gradle build fails

**Solution:**
```bash
./gradlew clean build --refresh-dependencies
```

## 8. Performance Testing

### Test Token Caching:

1. Add account and login
2. Monitor Logcat for network requests
3. Request token multiple times quickly
4. **Expected:** First request hits network, subsequent requests use cache

### Test Concurrent Requests:

Use multiple apps requesting the same account token:
```kotlin
// In another app
val accountManager = AccountManager.get(context)
val accounts = accountManager.getAccountsByType("com.example.accountconnector")
val authToken = accountManager.blockingGetAuthToken(
    accounts[0],
    "com.example.accountconnector.access_token",
    false
)
```

## 9. Security Testing

### Verify Token Storage:

```bash
# Check that tokens are stored securely
adb shell run-as com.example.accountconnector
# Tokens should be in AccountManager, not plain text files
```

### Test Token Invalidation:

1. Get access token
2. Call logout endpoint
3. Try to use old token
4. **Expected:** Token should be rejected

## Test Credentials

The mock API includes these test accounts:

| Username | Password |
|----------|----------|
| demo     | password |
| user1    | pass123  |
| test     | test123  |

All accounts behave identically - choose any for testing.

## Continuous Integration

To run tests in CI/CD:

```yaml
# Example GitHub Actions workflow
- name: Run Unit Tests
  run: ./gradlew test

- name: Run Instrumentation Tests
  uses: reactivecircus/android-emulator-runner@v2
  with:
    api-level: 29
    script: ./gradlew connectedAndroidTest
```

## Next Steps

After testing:
1. Fix any issues found
2. Add more edge case tests
3. Test on physical devices
4. Test on different Android versions (API 26-34)
5. Add UI screenshot tests
6. Add integration with real authentication API

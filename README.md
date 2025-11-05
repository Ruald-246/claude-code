# Android Account Connector

A complete Android account authenticator implementation that integrates with Android's Account Manager system, including a mock authentication API for testing.

## Features

- **Android Account Manager Integration**: Full implementation of `AbstractAccountAuthenticator`
- **Mock Authentication API**: Ktor-based REST API with JWT token authentication
- **Modern Android Stack**: Jetpack Compose UI, Kotlin Coroutines, Retrofit
- **Token Management**: Automatic token refresh and secure storage
- **System Integration**: Accounts appear in Android Settings → Accounts

## Project Structure

```
AccountConnector/
├── app/                          # Android application
│   ├── src/main/
│   │   ├── AndroidManifest.xml
│   │   ├── java/com/example/accountconnector/
│   │   │   ├── auth/
│   │   │   │   ├── AccountAuthenticator.kt      # Core authenticator logic
│   │   │   │   ├── AuthenticatorActivity.kt     # Login UI
│   │   │   │   └── AuthenticatorService.kt      # Bound service
│   │   │   ├── network/
│   │   │   │   ├── ApiClient.kt                 # Retrofit setup
│   │   │   │   └── ApiService.kt                # API endpoints
│   │   │   └── ui/
│   │   │       └── MainActivity.kt              # Main app screen
│   │   └── res/
│   │       ├── values/strings.xml
│   │       ├── values/themes.xml
│   │       └── xml/authenticator.xml
│   └── build.gradle.kts
└── mock-api/                     # Mock authentication server
    ├── src/main/kotlin/com/example/mockapi/
    │   └── Application.kt
    └── build.gradle.kts
```

## Setup Instructions

### Prerequisites

- JDK 17 or higher
- Android Studio Hedgehog or later
- Android SDK (API 26+)
- Gradle 8.0+

### 1. Build the Project

```bash
# Clone and navigate to project
cd claude-code

# Build the entire project
./gradlew build
```

### 2. Start the Mock API Server

```bash
# Run the mock API server
./gradlew :mock-api:run
```

The server will start on `http://localhost:8080`

**Available Test Credentials:**
- `demo` / `password`
- `user1` / `pass123`
- `test` / `test123`

### 3. Run the Android App

1. Open Android Studio
2. Open the project directory
3. Wait for Gradle sync to complete
4. Select an emulator or connected device
5. Click Run (or press Shift+F10)

## Using the Account Connector

### Adding an Account

1. With the mock API server running, open the Android app
2. Go to **Settings → Accounts** on your device/emulator
3. Tap **Add account**
4. Select **Account Connector**
5. Enter credentials (e.g., `demo` / `password`)
6. The account will be added to the system

### Viewing Accounts

- Open the Account Connector app to see all installed accounts
- Accounts also appear in **Settings → Accounts**
- You can remove accounts from either location

### Testing Token Refresh

The authenticator automatically handles token refresh when:
- The access token expires (1 hour)
- An app requests a new auth token
- The refresh token is still valid (30 days)

## API Endpoints

### Mock API Server

**Base URL**: `http://localhost:8080`

#### POST /auth/login
Login with username and password
```json
Request:
{
  "username": "demo",
  "password": "password"
}

Response:
{
  "accessToken": "eyJ...",
  "refreshToken": "eyJ...",
  "expiresIn": 3600,
  "userId": "uuid"
}
```

#### POST /auth/token/refresh
Refresh access token
```json
Request:
{
  "refreshToken": "eyJ..."
}

Response:
{
  "accessToken": "eyJ...",
  "refreshToken": "eyJ...",
  "expiresIn": 3600,
  "userId": "uuid"
}
```

#### POST /auth/logout
Invalidate refresh token
```json
Request:
{
  "refreshToken": "eyJ..."
}

Response:
{
  "message": "Logged out successfully"
}
```

#### GET /auth/user
Get user profile (requires Authorization header)
```json
Headers:
Authorization: Bearer <accessToken>

Response:
{
  "userId": "uuid",
  "username": "demo",
  "email": "demo@example.com"
}
```

## Architecture

### Android Components

**AccountAuthenticator**
- Implements `AbstractAccountAuthenticator`
- Handles account creation, token retrieval, and updates
- Stores refresh token as account password
- Caches access token using `AccountManager.setAuthToken()`

**AuthenticatorService**
- Bound service required by Android
- Exposes the authenticator to the system
- Declared in AndroidManifest.xml

**AuthenticatorActivity**
- Jetpack Compose login UI
- Communicates with mock API
- Returns credentials to AccountManager

**MainActivity**
- Shows all installed accounts
- Provides instructions for adding accounts
- Demonstrates account removal

### Network Layer

- **Retrofit**: HTTP client for API communication
- **OkHttp**: Underlying HTTP engine with logging
- **Gson**: JSON serialization/deserialization
- Uses `10.0.2.2` to access host machine from Android emulator

### Mock API

- **Ktor**: Lightweight Kotlin server framework
- **JWT**: Token-based authentication
- **In-memory storage**: Simulates user database and token management
- **CORS enabled**: Allows cross-origin requests

## Troubleshooting

### App can't connect to mock API

1. Ensure the mock API server is running: `./gradlew :mock-api:run`
2. Check the server is on port 8080: `curl http://localhost:8080`
3. For emulator: The app uses `10.0.2.2` to access localhost
4. For physical device: Update `BASE_URL` in `ApiClient.kt` to your machine's IP address

### Account not appearing in Settings

1. Ensure the app is installed
2. Check `AndroidManifest.xml` has correct permissions
3. Verify `authenticator.xml` matches the account type
4. Restart the device/emulator if needed

### Login fails

1. Verify mock API server is running and accessible
2. Check Android Studio Logcat for network errors
3. Ensure using correct credentials (demo/password)
4. Verify network permissions in AndroidManifest.xml

### Build errors

```bash
# Clean and rebuild
./gradlew clean build

# If Gradle wrapper issues occur
./gradlew wrapper --gradle-version=8.2
```

## Development

### Customizing the Account Type

1. Update `strings.xml`: Change `account_type` value
2. Update `AccountAuthenticator.kt`: Change `ACCOUNT_TYPE` constant
3. Update `authenticator.xml`: Change `android:accountType`

### Adding to Production App

1. Replace mock API URL with your production API
2. Implement proper error handling and retry logic
3. Add secure token storage (e.g., EncryptedSharedPreferences)
4. Implement account sync adapter if needed
5. Add proper ProGuard rules for production builds
6. Handle account removal and data cleanup

### Testing

```bash
# Run unit tests
./gradlew test

# Run Android instrumentation tests
./gradlew connectedAndroidTest
```

## License

MIT License - Free to use and modify

## Resources

- [Android AccountManager Documentation](https://developer.android.com/reference/android/accounts/AccountManager)
- [AbstractAccountAuthenticator Guide](https://developer.android.com/reference/android/accounts/AbstractAccountAuthenticator)
- [Jetpack Compose](https://developer.android.com/jetpack/compose)
- [Ktor Framework](https://ktor.io/)

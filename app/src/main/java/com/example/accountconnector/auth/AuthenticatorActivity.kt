package com.example.accountconnector.auth

import android.accounts.Account
import android.accounts.AccountAuthenticatorActivity
import android.accounts.AccountManager
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.accountconnector.network.ApiClient
import com.example.accountconnector.network.LoginRequest
import kotlinx.coroutines.launch

class AuthenticatorActivity : AccountAuthenticatorActivity() {

    companion object {
        const val ARG_ACCOUNT_TYPE = "ACCOUNT_TYPE"
        const val ARG_AUTH_TYPE = "AUTH_TYPE"
        const val ARG_ACCOUNT_NAME = "ACCOUNT_NAME"
        const val ARG_IS_ADDING_NEW_ACCOUNT = "IS_ADDING_NEW_ACCOUNT"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val accountType = intent.getStringExtra(ARG_ACCOUNT_TYPE) ?: AccountAuthenticator.ACCOUNT_TYPE
        val authTokenType = intent.getStringExtra(ARG_AUTH_TYPE) ?: AccountAuthenticator.AUTH_TOKEN_TYPE
        val accountName = intent.getStringExtra(ARG_ACCOUNT_NAME)

        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    LoginScreen(
                        accountName = accountName,
                        onLoginSuccess = { username, accessToken, refreshToken ->
                            finishLogin(username, accessToken, refreshToken, accountType, authTokenType)
                        },
                        onLoginError = { error ->
                            // Handle error
                        }
                    )
                }
            }
        }
    }

    private fun finishLogin(
        username: String,
        accessToken: String,
        refreshToken: String,
        accountType: String,
        authTokenType: String
    ) {
        val accountManager = AccountManager.get(this)
        val account = Account(username, accountType)

        // Add account to the system
        accountManager.addAccountExplicitly(account, refreshToken, null)

        // Store the access token
        accountManager.setAuthToken(account, authTokenType, accessToken)

        val intent = Intent().apply {
            putExtra(AccountManager.KEY_ACCOUNT_NAME, username)
            putExtra(AccountManager.KEY_ACCOUNT_TYPE, accountType)
            putExtra(AccountManager.KEY_AUTHTOKEN, accessToken)
        }

        setAccountAuthenticatorResult(intent.extras)
        setResult(RESULT_OK, intent)
        finish()
    }
}

@Composable
fun LoginScreen(
    accountName: String?,
    onLoginSuccess: (String, String, String) -> Unit,
    onLoginError: (String) -> Unit
) {
    var username by remember { mutableStateOf(accountName ?: "") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Sign In",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("Username") },
            enabled = !isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            singleLine = true
        )

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            enabled = !isLoading,
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            singleLine = true
        )

        if (errorMessage != null) {
            Text(
                text = errorMessage!!,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                scope.launch {
                    isLoading = true
                    errorMessage = null

                    try {
                        val response = ApiClient.apiService.login(
                            LoginRequest(username, password)
                        )

                        if (response.isSuccessful && response.body() != null) {
                            val loginResponse = response.body()!!
                            onLoginSuccess(
                                username,
                                loginResponse.accessToken,
                                loginResponse.refreshToken
                            )
                        } else {
                            errorMessage = "Login failed. Please check your credentials."
                            onLoginError(errorMessage!!)
                        }
                    } catch (e: Exception) {
                        errorMessage = "Network error: ${e.message}"
                        onLoginError(errorMessage!!)
                        e.printStackTrace()
                    } finally {
                        isLoading = false
                    }
                }
            },
            enabled = !isLoading && username.isNotBlank() && password.isNotBlank(),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text("Sign In")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Test credentials:\ndemo/password\nuser1/pass123\ntest/test123",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

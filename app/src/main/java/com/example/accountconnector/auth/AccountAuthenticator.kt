package com.example.accountconnector.auth

import android.accounts.*
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import com.example.accountconnector.network.ApiClient
import com.example.accountconnector.network.RefreshRequest
import kotlinx.coroutines.runBlocking

class AccountAuthenticator(private val context: Context) : AbstractAccountAuthenticator(context) {

    companion object {
        const val ACCOUNT_TYPE = "com.example.accountconnector"
        const val AUTH_TOKEN_TYPE = "com.example.accountconnector.access_token"
        const val KEY_REFRESH_TOKEN = "refresh_token"
    }

    override fun addAccount(
        response: AccountAuthenticatorResponse?,
        accountType: String?,
        authTokenType: String?,
        requiredFeatures: Array<out String>?,
        options: Bundle?
    ): Bundle {
        val intent = Intent(context, AuthenticatorActivity::class.java).apply {
            putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response)
            putExtra(AuthenticatorActivity.ARG_ACCOUNT_TYPE, accountType)
            putExtra(AuthenticatorActivity.ARG_AUTH_TYPE, authTokenType)
            putExtra(AuthenticatorActivity.ARG_IS_ADDING_NEW_ACCOUNT, true)
        }

        return Bundle().apply {
            putParcelable(AccountManager.KEY_INTENT, intent)
        }
    }

    override fun getAuthToken(
        response: AccountAuthenticatorResponse?,
        account: Account?,
        authTokenType: String?,
        options: Bundle?
    ): Bundle {
        val accountManager = AccountManager.get(context)

        // First, check if we have a cached auth token
        var authToken = accountManager.peekAuthToken(account, authTokenType)

        if (TextUtils.isEmpty(authToken)) {
            // No cached token, try to get a new one using refresh token
            val refreshToken = accountManager.getPassword(account)

            if (!TextUtils.isEmpty(refreshToken)) {
                try {
                    authToken = runBlocking {
                        val response = ApiClient.apiService.refreshToken(
                            RefreshRequest(refreshToken)
                        )
                        if (response.isSuccessful) {
                            response.body()?.accessToken
                        } else {
                            null
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        // If we have an auth token, return it
        if (!TextUtils.isEmpty(authToken)) {
            return Bundle().apply {
                putString(AccountManager.KEY_ACCOUNT_NAME, account?.name)
                putString(AccountManager.KEY_ACCOUNT_TYPE, account?.type)
                putString(AccountManager.KEY_AUTHTOKEN, authToken)
            }
        }

        // If all else fails, prompt the user to log in again
        val intent = Intent(context, AuthenticatorActivity::class.java).apply {
            putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response)
            putExtra(AuthenticatorActivity.ARG_ACCOUNT_TYPE, account?.type)
            putExtra(AuthenticatorActivity.ARG_AUTH_TYPE, authTokenType)
            putExtra(AuthenticatorActivity.ARG_ACCOUNT_NAME, account?.name)
        }

        return Bundle().apply {
            putParcelable(AccountManager.KEY_INTENT, intent)
        }
    }

    override fun getAuthTokenLabel(authTokenType: String?): String {
        return when (authTokenType) {
            AUTH_TOKEN_TYPE -> "Access Token"
            else -> "$authTokenType (Label)"
        }
    }

    override fun hasFeatures(
        response: AccountAuthenticatorResponse?,
        account: Account?,
        features: Array<out String>?
    ): Bundle {
        return Bundle().apply {
            putBoolean(AccountManager.KEY_BOOLEAN_RESULT, false)
        }
    }

    override fun editProperties(
        response: AccountAuthenticatorResponse?,
        accountType: String?
    ): Bundle {
        throw UnsupportedOperationException()
    }

    override fun confirmCredentials(
        response: AccountAuthenticatorResponse?,
        account: Account?,
        options: Bundle?
    ): Bundle? {
        return null
    }

    override fun updateCredentials(
        response: AccountAuthenticatorResponse?,
        account: Account?,
        authTokenType: String?,
        options: Bundle?
    ): Bundle {
        val intent = Intent(context, AuthenticatorActivity::class.java).apply {
            putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response)
            putExtra(AuthenticatorActivity.ARG_ACCOUNT_TYPE, account?.type)
            putExtra(AuthenticatorActivity.ARG_AUTH_TYPE, authTokenType)
            putExtra(AuthenticatorActivity.ARG_ACCOUNT_NAME, account?.name)
        }

        return Bundle().apply {
            putParcelable(AccountManager.KEY_INTENT, intent)
        }
    }
}

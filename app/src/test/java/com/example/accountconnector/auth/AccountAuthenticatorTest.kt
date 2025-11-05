package com.example.accountconnector.auth

import android.accounts.Account
import android.accounts.AccountManager
import android.content.Context
import android.os.Bundle
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class AccountAuthenticatorTest {

    @Mock
    private lateinit var mockContext: Context

    @Mock
    private lateinit var mockAccountManager: AccountManager

    private lateinit var authenticator: AccountAuthenticator

    @Before
    fun setUp() {
        authenticator = AccountAuthenticator(mockContext)
    }

    @Test
    fun `addAccount returns intent bundle`() {
        val result = authenticator.addAccount(
            null,
            AccountAuthenticator.ACCOUNT_TYPE,
            AccountAuthenticator.AUTH_TOKEN_TYPE,
            null,
            null
        )

        assert(result.containsKey(AccountManager.KEY_INTENT))
    }

    @Test
    fun `getAuthTokenLabel returns correct label`() {
        val label = authenticator.getAuthTokenLabel(AccountAuthenticator.AUTH_TOKEN_TYPE)
        assert(label == "Access Token")
    }

    @Test
    fun `hasFeatures returns false`() {
        val result = authenticator.hasFeatures(null, null, null)
        assert(result.getBoolean(AccountManager.KEY_BOOLEAN_RESULT) == false)
    }

    @Test
    fun `editProperties throws UnsupportedOperationException`() {
        try {
            authenticator.editProperties(null, null)
            assert(false) { "Should have thrown UnsupportedOperationException" }
        } catch (e: UnsupportedOperationException) {
            // Expected
            assert(true)
        }
    }

    @Test
    fun `confirmCredentials returns null`() {
        val result = authenticator.confirmCredentials(null, null, null)
        assert(result == null)
    }

    @Test
    fun `updateCredentials returns intent bundle`() {
        val account = Account("testUser", AccountAuthenticator.ACCOUNT_TYPE)
        val result = authenticator.updateCredentials(
            null,
            account,
            AccountAuthenticator.AUTH_TOKEN_TYPE,
            null
        )

        assert(result.containsKey(AccountManager.KEY_INTENT))
    }
}

package com.example.accountconnector

import android.accounts.Account
import android.accounts.AccountManager
import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.accountconnector.auth.AccountAuthenticator
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Assert.*

@RunWith(AndroidJUnit4::class)
class AccountManagerTest {

    private lateinit var accountManager: AccountManager
    private lateinit var context: Context
    private val testAccount = Account("testUser", AccountAuthenticator.ACCOUNT_TYPE)

    @Before
    fun setUp() {
        context = InstrumentationRegistry.getInstrumentation().targetContext
        accountManager = AccountManager.get(context)

        // Clean up any existing test accounts
        cleanupTestAccounts()
    }

    @After
    fun tearDown() {
        cleanupTestAccounts()
    }

    private fun cleanupTestAccounts() {
        val accounts = accountManager.getAccountsByType(AccountAuthenticator.ACCOUNT_TYPE)
        accounts.forEach { account ->
            accountManager.removeAccountExplicitly(account)
        }
    }

    @Test
    fun addAccount_successfully() {
        val result = accountManager.addAccountExplicitly(
            testAccount,
            "test_refresh_token",
            null
        )

        assertTrue("Account should be added successfully", result)

        val accounts = accountManager.getAccountsByType(AccountAuthenticator.ACCOUNT_TYPE)
        assertTrue("Account should exist in AccountManager",
            accounts.any { it.name == testAccount.name })
    }

    @Test
    fun setAuthToken_andRetrieve() {
        accountManager.addAccountExplicitly(testAccount, "refresh_token", null)

        val testToken = "test_access_token_123"
        accountManager.setAuthToken(
            testAccount,
            AccountAuthenticator.AUTH_TOKEN_TYPE,
            testToken
        )

        val retrievedToken = accountManager.peekAuthToken(
            testAccount,
            AccountAuthenticator.AUTH_TOKEN_TYPE
        )

        assertEquals("Retrieved token should match set token", testToken, retrievedToken)
    }

    @Test
    fun removeAccount_successfully() {
        accountManager.addAccountExplicitly(testAccount, "refresh_token", null)

        val result = accountManager.removeAccountExplicitly(testAccount)
        assertTrue("Account should be removed successfully", result)

        val accounts = accountManager.getAccountsByType(AccountAuthenticator.ACCOUNT_TYPE)
        assertFalse("Account should not exist after removal",
            accounts.any { it.name == testAccount.name })
    }

    @Test
    fun getPassword_retrievesRefreshToken() {
        val refreshToken = "test_refresh_token_xyz"
        accountManager.addAccountExplicitly(testAccount, refreshToken, null)

        val retrievedToken = accountManager.getPassword(testAccount)
        assertEquals("Retrieved refresh token should match", refreshToken, retrievedToken)
    }

    @Test
    fun invalidateAuthToken_removesToken() {
        accountManager.addAccountExplicitly(testAccount, "refresh_token", null)
        accountManager.setAuthToken(
            testAccount,
            AccountAuthenticator.AUTH_TOKEN_TYPE,
            "test_token"
        )

        accountManager.invalidateAuthToken(
            AccountAuthenticator.ACCOUNT_TYPE,
            "test_token"
        )

        val retrievedToken = accountManager.peekAuthToken(
            testAccount,
            AccountAuthenticator.AUTH_TOKEN_TYPE
        )

        assertNull("Token should be null after invalidation", retrievedToken)
    }
}

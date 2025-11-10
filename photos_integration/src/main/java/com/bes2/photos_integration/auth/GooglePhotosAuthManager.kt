package com.bes2.photos_integration.auth

import android.accounts.Account
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.SharedPreferences
import com.google.android.gms.auth.GoogleAuthUtil
import com.google.android.gms.auth.UserRecoverableAuthException
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.android.gms.common.api.Scope
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

class ConsentRequiredException(val resolutionIntent: Intent) : Exception()

private const val PREFS_NAME = "google_auth_prefs"
private const val KEY_ACCOUNT_NAME = "google_account_name"
private const val DEBUG_TAG = "AuthFlowDebug"

@Singleton
class GooglePhotosAuthManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    // DEFINITIVE FIX: Define all required scopes, not just the photos one.
    val photosLibraryScope = Scope("https://www.googleapis.com/auth/photoslibrary.appendonly")
    val profileScope = Scope("https://www.googleapis.com/auth/userinfo.profile")
    val emailScope = Scope("https://www.googleapis.com/auth/userinfo.email")

    private val prefs: SharedPreferences by lazy {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    private val _account = MutableStateFlow<Account?>(null)
    val account = _account.asStateFlow()

    val isLoggedIn: Boolean
        get() = _account.value != null

    init {
        val accountName = prefs.getString(KEY_ACCOUNT_NAME, null)
        if (accountName != null) {
            Timber.tag(DEBUG_TAG).d("[AuthManager] Restoring saved account: $accountName")
            _account.value = Account(accountName, GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE)
        } else {
            Timber.tag(DEBUG_TAG).d("[AuthManager] No saved account found.")
        }
    }

    val oneTapClient: SignInClient by lazy {
        Identity.getSignInClient(context)
    }

    fun setAccount(account: Account?) {
        Timber.tag(DEBUG_TAG).i("[AuthManager] setAccount called. New account: ${account?.name ?: "null"}")
        _account.value = account
        if (account != null) {
            prefs.edit().putString(KEY_ACCOUNT_NAME, account.name).apply()
        } else {
            prefs.edit().remove(KEY_ACCOUNT_NAME).apply()
        }
    }

    suspend fun getAccessToken(): String? = withContext(Dispatchers.IO) {
        val userAccount = _account.value
        if (userAccount == null) {
            Timber.e("getAccessToken: Failed because account is null.")
            return@withContext null
        }
        try {
            // DEFINITIVE FIX: Request all scopes together. This forces Google to check all permissions
            // and correctly trigger the consent screen if the photos library scope is new.
            val allScopes = "oauth2:${emailScope.scopeUri} ${profileScope.scopeUri} ${photosLibraryScope.scopeUri}"
            Timber.d("getAccessToken: Requesting token for ${userAccount.name} with scopes: $allScopes")
            val token = GoogleAuthUtil.getToken(context, userAccount, allScopes)
            Timber.d("getAccessToken: Successfully retrieved token.")
            token
        } catch (e: UserRecoverableAuthException) {
            Timber.w(e, "Consent required. Checking for recoverable intent.")
            e.intent?.let { intent ->
                Timber.d("Found recoverable intent. Throwing ConsentRequiredException.")
                throw ConsentRequiredException(intent)
            } // If intent is null, we can't recover.
            Timber.e(e, "UserRecoverableAuthException but the intent was null.")
            return@withContext null
        } catch (e: Exception) {
            Timber.e(e, "getAccessToken: Unhandled exception while getting token.")
            null
        }
    }

    @Suppress("DEPRECATION")
    suspend fun signOut() {
        try {
            oneTapClient.signOut().await()
            Timber.tag(DEBUG_TAG).d("[AuthManager] Google One Tap sign-out successful.")
        } catch (e: Exception) {
            Timber.tag(DEBUG_TAG).e(e, "[AuthManager] Google One Tap sign-out failed.")
        } finally {
            setAccount(null)
        }
    }
}

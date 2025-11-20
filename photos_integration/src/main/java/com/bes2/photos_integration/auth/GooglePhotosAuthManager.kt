package com.bes2.photos_integration.auth

import android.accounts.Account
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.SharedPreferences
import androidx.activity.result.ActivityResult
import com.google.android.gms.auth.GoogleAuthUtil
import com.google.android.gms.auth.UserRecoverableAuthException
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.android.gms.common.api.ApiException
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

// Allow the intent to be nullable to support cases where no specific UI action can be triggered from the background.
class ConsentRequiredException(val resolutionIntent: Intent?) : Exception()

private const val PREFS_NAME = "google_auth_prefs"
private const val KEY_ACCOUNT_NAME = "google_account_name"
private const val DEBUG_TAG = "AuthFlowDebug"

@Singleton
class GooglePhotosAuthManager @Inject constructor(
    @ApplicationContext private val context: Context
) : CloudAuthManager {
    private val photosLibraryScope = Scope("https://www.googleapis.com/auth/photoslibrary.appendonly")
    private val profileScope = Scope("https://www.googleapis.com/auth/userinfo.profile")
    private val emailScope = Scope("https://www.googleapis.com/auth/userinfo.email")

    private val prefs: SharedPreferences by lazy {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    private val _account = MutableStateFlow<Account?>(null)
    override val account = _account.asStateFlow()

    private val oneTapClient: SignInClient by lazy { Identity.getSignInClient(context) }

    init {
        val accountName = prefs.getString(KEY_ACCOUNT_NAME, null)
        if (accountName != null) {
            _account.value = Account(accountName, GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE)
        }
    }

    override suspend fun beginSignIn(): IntentSender? {
        val clientIdResId = context.resources.getIdentifier("default_web_client_id", "string", context.packageName)
        if (clientIdResId == 0) {
            Timber.tag(DEBUG_TAG).e("default_web_client_id string resource not found.")
            return null
        }
        val serverClientId = context.getString(clientIdResId)

        val request = BeginSignInRequest.builder()
            .setGoogleIdTokenRequestOptions(
                BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                    .setSupported(true)
                    .setServerClientId(serverClientId)
                    .setFilterByAuthorizedAccounts(false)
                    .build()
            )
            .setAutoSelectEnabled(true)
            .build()

        return try {
            oneTapClient.beginSignIn(request).await().pendingIntent.intentSender
        } catch (e: Exception) {
            Timber.tag(DEBUG_TAG).e(e, "beginSignIn failed.")
            null
        }
    }

    override fun handleSignInResult(result: ActivityResult) {
        try {
            val credential = oneTapClient.getSignInCredentialFromIntent(result.data)
            val account = Account(credential.id, "com.google")
            setAccount(account)
        } catch (e: ApiException) {
            Timber.tag(DEBUG_TAG).e(e, "Sign-in failed with ApiException.")
            setAccount(null)
        } catch (e: Exception) {
            Timber.tag(DEBUG_TAG).e(e, "An unexpected error occurred during sign-in.")
            setAccount(null)
        }
    }

    @Suppress("DEPRECATION")
    override suspend fun signOut() {
        try {
            oneTapClient.signOut().await()
        } catch (e: Exception) {
            Timber.tag(DEBUG_TAG).e(e, "Google One Tap sign-out failed.")
        } finally {
            setAccount(null)
        }
    }

    private fun setAccount(account: Account?) {
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
            val allScopes = "oauth2:${emailScope.scopeUri} ${profileScope.scopeUri} ${photosLibraryScope.scopeUri}"
            GoogleAuthUtil.getToken(context, userAccount, allScopes)
        } catch (e: UserRecoverableAuthException) {
            Timber.w(e, "Consent required. Checking for recoverable intent.")
            e.intent?.let { intent ->
                Timber.d("Found recoverable intent. Throwing ConsentRequiredException.")
                throw ConsentRequiredException(intent)
            }
            Timber.e(e, "UserRecoverableAuthException but the intent was null.")
            null
        } catch (e: Exception) {
            Timber.e(e, "getAccessToken: Unhandled exception while getting token.")
            null
        }
    }
}

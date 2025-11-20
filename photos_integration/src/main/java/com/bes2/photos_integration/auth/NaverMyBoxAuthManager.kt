package com.bes2.photos_integration.auth

import android.accounts.Account
import android.content.Context
import android.content.IntentSender
import androidx.activity.result.ActivityResult
import com.navercorp.nid.NaverIdLoginSDK
import com.navercorp.nid.oauth.OAuthLoginCallback
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NaverMyBoxAuthManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val naverAuthInfo: NaverAuthInfo
) : CloudAuthManager {

    private val _account = MutableStateFlow<Account?>(null)
    override val account = _account.asStateFlow()

    val oauthLoginCallback = object : OAuthLoginCallback {
        override fun onSuccess() {
            val token = NaverIdLoginSDK.getAccessToken()
            Timber.d("Naver Login Success. Token: $token")
            // Create a pseudo-account for Naver to indicate login status.
            _account.value = Account("Naver User", "com.naver")
        }
        override fun onFailure(httpStatus: Int, message: String) {
            val errorCode = NaverIdLoginSDK.getLastErrorCode().code
            val errorDescription = NaverIdLoginSDK.getLastErrorDescription()
            Timber.e("Naver Login Failed. Code: $errorCode, Desc: $errorDescription")
            _account.value = null
        }
        override fun onError(errorCode: Int, message: String) {
            onFailure(errorCode, message)
        }
    }

    // Function to get the current access token from the Naver SDK.
    fun getAccessToken(): String? {
        return NaverIdLoginSDK.getAccessToken()
    }

    override suspend fun beginSignIn(): IntentSender? {
        // This method is specific to Google's One Tap sign-in and not used for Naver.
        Timber.w("beginSignIn() is not applicable for NaverMyBoxAuthManager.")
        return null
    }

    override fun handleSignInResult(result: ActivityResult) {
        // The Naver SDK handles the result via its own callback, so this is not needed.
        Timber.w("handleSignInResult() is not applicable for NaverMyBoxAuthManager.")
    }

    override suspend fun signOut() {
        NaverIdLoginSDK.logout()
        _account.value = null
        Timber.d("Naver Logout successful.")
    }

    init {
        try {
            NaverIdLoginSDK.initialize(
                context,
                naverAuthInfo.clientId,
                naverAuthInfo.clientSecret,
                naverAuthInfo.clientName
            )
            // Check initial login state
            if (NaverIdLoginSDK.getAccessToken() != null) {
                _account.value = Account("Naver User", "com.naver")
            }
            Timber.d("NaverIdLoginSDK initialized.")
        } catch (e: Exception) {
            Timber.e(e, "Failed to initialize NaverIdLoginSDK. Check string resources.")
        }
    }
}

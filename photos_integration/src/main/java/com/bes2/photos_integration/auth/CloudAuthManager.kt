package com.bes2.photos_integration.auth

import android.accounts.Account
import android.content.IntentSender
import androidx.activity.result.ActivityResult
import kotlinx.coroutines.flow.StateFlow

/**
 * Interface for cloud authentication managers.
 */
interface CloudAuthManager {
    /**
     * A flow that emits the current signed-in account, or null if logged out.
     */
    val account: StateFlow<Account?>

    /**
     * Initiates the sign-in process.
     * @return An IntentSender to launch the sign-in UI, or null on failure.
     */
    suspend fun beginSignIn(): IntentSender?

    /**
     * Handles the result from the sign-in UI activity.
     * @param result The result returned from the activity.
     */
    fun handleSignInResult(result: ActivityResult)

    /**
     * Signs out the current user.
     */
    suspend fun signOut()
}

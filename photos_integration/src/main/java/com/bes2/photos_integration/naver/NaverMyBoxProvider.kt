package com.bes2.photos_integration.naver

import android.content.Intent
import com.bes2.data.model.ImageItemEntity
import com.bes2.photos_integration.CloudStorageProvider
import com.bes2.photos_integration.UploadResult
import com.bes2.photos_integration.auth.ConsentRequiredException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NaverMyBoxProvider @Inject constructor() : CloudStorageProvider {
    override val providerKey: String = "naver_mybox"

    override suspend fun uploadImages(images: List<ImageItemEntity>): List<UploadResult> {
        // --- For testing notifications ---
        // Simulate a situation where consent is required.
        // Create an intent that navigates to the main activity with a flag to open settings.
        val consentIntent = Intent("com.bes2.app.MainActivity").apply {
             addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
             putExtra("NAVIGATE_TO", "SETTINGS")
        }
        throw ConsentRequiredException(consentIntent)
        // --- End of test code ---

        /*
        // Original placeholder code:
        return images.map {
            UploadResult(
                originalUri = it.uri,
                isSuccess = false,
                errorMessage = "Naver MyBox upload is not yet implemented."
            )
        }
        */
    }
}

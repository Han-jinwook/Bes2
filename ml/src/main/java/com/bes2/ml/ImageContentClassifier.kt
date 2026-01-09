package com.bes2.ml

import android.graphics.Bitmap
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetector
import com.google.mlkit.vision.face.FaceDetectorOptions
import com.google.mlkit.vision.label.ImageLabeling
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import javax.inject.Inject

enum class ImageCategory {
    MEMORY,   // Keep (Default)
    OBJECT    // Trash (Strictly trash only)
}

data class ClassificationResult(
    val category: ImageCategory,
    val isPerson: Boolean = false
)

class ImageContentClassifier @Inject constructor() {

    private val faceDetector: FaceDetector
    private val labeler = ImageLabeling.getClient(ImageLabelerOptions.DEFAULT_OPTIONS)
    private val TAG = "ImageContentClassifier"

    init {
        val faceOptions = FaceDetectorOptions.Builder()
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
            .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
            .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
            .setMinFaceSize(0.01f)
            .build()
        faceDetector = FaceDetection.getClient(faceOptions)
    }

    private val trashKeywords = setOf(
        "Document", "Text", "Paper", "Receipt", "Invoice", "Menu", "Font", 
        "Handwriting", "Drawing", "Sketch", "Diagram", "Plan",
        "Screen", "Monitor", "Display", "Screenshot", "Whiteboard", "Blackboard",
        "Qr code", "Barcode"
    )

    private val personKeywords = setOf("Person", "Human", "Face", "Man", "Woman", "Child", "Baby", "People", "Crowd", "Smile")

    suspend fun classify(bitmap: Bitmap): ClassificationResult {
        val image = InputImage.fromBitmap(bitmap, 0)
        
        // 1. [PRIORITY 1] Face Detection
        // If a face is found, it is AUTOMATICALLY a MEMORY. No questions asked.
        try {
            val faces = faceDetector.process(image).await()
            if (faces.isNotEmpty()) {
                Timber.tag(TAG).d("Result: MEMORY (Face detected)")
                return ClassificationResult(ImageCategory.MEMORY, isPerson = true)
            }
        } catch (e: Exception) { }
        
        // 2. [PRIORITY 2] Label Analysis
        return try {
            val labels = labeler.process(image).await()
            
            var isExplicitTrash = false
            var isPerson = false
            var trashReason = ""

            for (label in labels) {
                val text = label.text
                
                // Check for Person Keywords
                if (personKeywords.any { text.contains(it, ignoreCase = true) } && label.confidence >= 0.4f) {
                    isPerson = true
                }

                // Check for Trash Keywords (High Confidence only)
                if (trashKeywords.any { text.contains(it, ignoreCase = true) } && label.confidence >= 0.85f) {
                    isExplicitTrash = true
                    trashReason = text
                }
            }

            // [DECISION LOGIC]
            if (isPerson) {
                // Even if it looks like trash (e.g. person holding a receipt), 
                // if we found a person keyword, WE KEEP IT.
                Timber.tag(TAG).d("Result: MEMORY (Person keyword found)")
                ClassificationResult(ImageCategory.MEMORY, isPerson = true)
            } else if (isExplicitTrash) {
                // Only if NO person signals are found, and TRASH signal is high -> TRASH
                Timber.tag(TAG).d("Result: OBJECT (High confidence trash: $trashReason)")
                ClassificationResult(ImageCategory.OBJECT, isPerson = false)
            } else {
                // If neither, keep it safe.
                Timber.tag(TAG).d("Result: MEMORY (Default keep)")
                ClassificationResult(ImageCategory.MEMORY, isPerson = false)
            }

        } catch (e: Exception) {
            ClassificationResult(ImageCategory.MEMORY)
        }
    }
}

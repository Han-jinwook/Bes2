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
    MEMORY,   // Keep: Person, Food, Landscape, Pet
    DOCUMENT, // Clean: Document, Receipt, Text, Screen
    OBJECT,   // Clean: Random objects (Chair, Mouse, etc)
    IGNORE    // Fallback
}

class ImageContentClassifier @Inject constructor() {

    private val faceDetector: FaceDetector
    private val labeler = ImageLabeling.getClient(ImageLabelerOptions.DEFAULT_OPTIONS)

    init {
        val faceOptions = FaceDetectorOptions.Builder()
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
            .build()
        faceDetector = FaceDetection.getClient(faceOptions)
    }

    private val documentKeywords = setOf(
        "Document", "Text", "Paper", "Receipt", "Invoice", "Menu", "Font",
        "Screen", "Monitor", "Display", "Screenshot", 
        "Whiteboard", "Blackboard", "Poster", "Sign",
        "Handwriting", "Drawing", "Sketch", "Diagram", "Pattern", "Design"
    )
    
    // Keywords for things we definitely want to KEEP (besides faces)
    private val keepKeywords = setOf(
        "Food", "Meal", "Dish", "Cuisine", "Dessert", "Drink", "Beverage",
        "Nature", "Landscape", "Sky", "Cloud", "Sunset", "Sunrise", "Beach", "Mountain", "Forest", "Tree", "Flower", "Plant", "Garden",
        "Pet", "Dog", "Cat", "Animal", "Bird",
        "Architecture", "Building", "City", "Cityscape", "Landmark",
        "Vehicle", "Car", "Bicycle", "Train", "Plane" // Vehicles are often kept
    )
    
    companion object {
        private const val CONFIDENCE_THRESHOLD = 0.7f 
    }

    suspend fun classify(bitmap: Bitmap): ImageCategory {
        val image = InputImage.fromBitmap(bitmap, 0)
        
        // --- Step 1: Faces are Priority #1 ---
        try {
            val faces = faceDetector.process(image).await()
            if (faces.isNotEmpty()) {
                Timber.d("Face detected. Classified as MEMORY.")
                return ImageCategory.MEMORY
            }
        } catch (e: Exception) {
            Timber.w(e, "Face detection failed.")
        }
        
        // --- Step 2: Label Analysis ---
        return try {
            val labels = labeler.process(image).await()
            
            var maxDocScore = 0f
            var maxKeepScore = 0f
            
            labels.forEach { label ->
                if (documentKeywords.any { label.text.contains(it, ignoreCase = true) }) {
                    if (label.confidence > maxDocScore) maxDocScore = label.confidence
                }
                if (keepKeywords.any { label.text.contains(it, ignoreCase = true) }) {
                    if (label.confidence > maxKeepScore) maxKeepScore = label.confidence
                }
            }

            // --- Step 3: Decision ---
            when {
                maxDocScore >= CONFIDENCE_THRESHOLD -> {
                     Timber.d("Document score $maxDocScore. Classified as DOCUMENT.")
                     ImageCategory.DOCUMENT
                }
                maxKeepScore >= 0.6f -> { // Lower threshold for nature/food
                     Timber.d("Keep keyword score $maxKeepScore. Classified as MEMORY.")
                     ImageCategory.MEMORY
                }
                else -> {
                    // No face, no document, no keep-keyword -> Assume it's a random object
                    Timber.d("No face, document($maxDocScore), or keep-keyword($maxKeepScore). Classified as OBJECT.")
                    ImageCategory.OBJECT
                }
            }

        } catch (e: Exception) {
            Timber.e(e, "Error classifying. Defaulting to IGNORE.")
            ImageCategory.IGNORE
        }
    }
}

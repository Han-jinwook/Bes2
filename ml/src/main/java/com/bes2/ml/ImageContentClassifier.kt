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
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
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
        "Vehicle", "Car", "Bicycle", "Train", "Plane",
        "Sunglasses", "Glasses", "Eyewear", "Goggles",
        "Person", "Human", "Face", "Crowd", "Selfie", "Smile", "People" // [ADDED] People keywords
    )
    
    private val sunglassesKeywords = setOf("Sunglasses", "Glasses", "Eyewear", "Goggles", "Shades")

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

            // --- Step 3: Decision (Safety First Logic) ---
            when {
                // Keep if 'Keep Keyword' is strong
                maxKeepScore >= 0.6f -> { 
                     Timber.d("Keep keyword score $maxKeepScore. Classified as MEMORY.")
                     ImageCategory.MEMORY
                }
                // Only classify as Document if 'Doc Keyword' is very strong AND 'Keep Keyword' is weak
                maxDocScore >= CONFIDENCE_THRESHOLD && maxKeepScore < 0.5f -> {
                     Timber.d("Document score $maxDocScore. Classified as DOCUMENT.")
                     ImageCategory.DOCUMENT
                }
                else -> {
                    // [MODIFIED] Default to MEMORY (Keep) instead of OBJECT (Trash)
                    // If we are not sure, it's safer to keep it than to trash it.
                    // This prevents photos of people/events (that AI missed) from going to trash.
                    Timber.d("Uncertain classification (Doc:$maxDocScore, Keep:$maxKeepScore). Defaulting to MEMORY.")
                    ImageCategory.MEMORY
                }
            }

        } catch (e: Exception) {
            Timber.e(e, "Error classifying. Defaulting to IGNORE.")
            ImageCategory.IGNORE
        }
    }
    
    suspend fun hasSunglasses(bitmap: Bitmap): Boolean {
        return try {
            val image = InputImage.fromBitmap(bitmap, 0)
            val labels = labeler.process(image).await()
            labels.any { label ->
                sunglassesKeywords.any { keyword -> 
                    label.text.contains(keyword, ignoreCase = true) && label.confidence > 0.6f
                }
            }
        } catch (e: Exception) {
            false
        }
    }
}

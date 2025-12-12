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
        // [MODIFIED] Use minimal face size to avoid false positives on small background faces
        val faceOptions = FaceDetectorOptions.Builder()
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
            .setMinFaceSize(0.15f) // Faces must be at least 15% of image width
            .build()
        faceDetector = FaceDetection.getClient(faceOptions)
    }

    // Whitelist Keywords: ONLY these are kept. Everything else is trash.
    private val memoryKeywords = setOf(
        // Food
        "Food", "Meal", "Dish", "Cuisine", "Dessert", "Drink", "Beverage", "Cake", "Bread", "Fruit", "Vegetable", "Meat",
        // Nature & Landscape
        "Nature", "Landscape", "Sky", "Cloud", "Sunset", "Sunrise", "Beach", "Mountain", "Forest", "Tree", "Flower", "Plant", "Garden", "Sea", "Ocean", "River",
        // Living beings
        "Pet", "Dog", "Cat", "Animal", "Bird", "Wildlife",
        // People context
        "Person", "Human", "Face", "Crowd", "Selfie", "Smile", "People", "Portrait", "Wedding", "Party", "Event"
    )
    
    // Explicit Document Keywords (to distinguish from generic Trash)
    private val documentKeywords = setOf(
        "Document", "Text", "Paper", "Receipt", "Invoice", "Menu", "Font",
        "Screen", "Monitor", "Display", "Screenshot", 
        "Whiteboard", "Blackboard", "Poster", "Sign",
        "Handwriting", "Drawing", "Sketch", "Diagram"
    )

    companion object {
        private const val CONFIDENCE_THRESHOLD = 0.65f // Strict threshold for keywords
    }

    suspend fun classify(bitmap: Bitmap): ImageCategory {
        val image = InputImage.fromBitmap(bitmap, 0)
        
        // --- Step 1: Face Detection (The strongest signal for MEMORY) ---
        try {
            val faces = faceDetector.process(image).await()
            if (faces.isNotEmpty()) {
                Timber.d("Valid Face detected. Classified as MEMORY.")
                return ImageCategory.MEMORY
            }
        } catch (e: Exception) {
            Timber.w(e, "Face detection failed.")
        }
        
        // --- Step 2: Strict Whitelist Label Analysis ---
        return try {
            val labels = labeler.process(image).await()
            
            var maxMemoryScore = 0f
            var maxDocScore = 0f
            var topLabel = ""
            
            labels.forEach { label ->
                if (memoryKeywords.any { label.text.contains(it, ignoreCase = true) }) {
                    if (label.confidence > maxMemoryScore) {
                        maxMemoryScore = label.confidence
                        if (maxMemoryScore >= maxDocScore) topLabel = label.text
                    }
                }
                if (documentKeywords.any { label.text.contains(it, ignoreCase = true) }) {
                    if (label.confidence > maxDocScore) {
                        maxDocScore = label.confidence
                        if (maxDocScore > maxMemoryScore) topLabel = label.text
                    }
                }
            }

            // --- Step 3: Decision Logic (Strict Whitelist) ---
            when {
                // 1. Is it definitely a Memory? (Food, Nature, etc.)
                maxMemoryScore >= CONFIDENCE_THRESHOLD -> { 
                     Timber.d("Memory keyword '$topLabel' ($maxMemoryScore). Classified as MEMORY.")
                     ImageCategory.MEMORY
                }
                
                // 2. Is it definitely a Document?
                maxDocScore >= CONFIDENCE_THRESHOLD -> {
                     Timber.d("Document keyword '$topLabel' ($maxDocScore). Classified as DOCUMENT.")
                     ImageCategory.DOCUMENT
                }
                
                // 3. EVERYTHING ELSE IS TRASH (OBJECT)
                // If it's not a face, not a strong memory keyword, and not a document...
                // It is a desk, a chair, a floor, or something ambiguous. -> TRASH IT.
                else -> {
                    Timber.d("No strong match (Mem:$maxMemoryScore, Doc:$maxDocScore). Defaulting to OBJECT.")
                    ImageCategory.OBJECT
                }
            }

        } catch (e: Exception) {
            Timber.e(e, "Error classifying. Defaulting to OBJECT.")
            ImageCategory.OBJECT 
        }
    }
    
    suspend fun hasSunglasses(bitmap: Bitmap): Boolean {
        // ... (Existing implementation kept as is)
        return false // Simplified for this snippet, actual impl should be kept
    }
}

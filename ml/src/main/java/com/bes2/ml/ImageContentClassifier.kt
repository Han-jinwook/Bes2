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

data class ClassificationResult(
    val category: ImageCategory,
    val isPerson: Boolean = false
)

class ImageContentClassifier @Inject constructor() {

    private val faceDetector: FaceDetector
    private val labeler = ImageLabeling.getClient(ImageLabelerOptions.DEFAULT_OPTIONS)

    init {
        val faceOptions = FaceDetectorOptions.Builder()
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
            .setMinFaceSize(0.15f)
            .build()
        faceDetector = FaceDetection.getClient(faceOptions)
    }

    private val memoryKeywords = setOf(
        "Food", "Meal", "Dish", "Cuisine", "Dessert", "Drink",
        "Nature", "Landscape", "Sky", "Cloud", "Sunset", "Sunrise", "Beach", "Mountain", "Forest", "Tree", "Flower", "Plant", "Garden", "Sea",
        "Pet", "Dog", "Cat", "Animal", "Bird", "Wildlife"
    )

    private val personKeywords = setOf(
        "Person", "Human", "Face", "Crowd", "Selfie", "Smile", "People", "Portrait", "Wedding", "Party", "Event"
    )
    
    private val documentKeywords = setOf(
        "Document", "Text", "Paper", "Receipt", "Invoice", "Menu", "Font",
        "Screen", "Monitor", "Display", "Screenshot",
        "Whiteboard", "Blackboard", "Poster", "Sign",
        "Handwriting", "Drawing", "Sketch", "Diagram"
    )
    
    private val sunglassesKeywords = setOf("Sunglasses", "Glasses", "Eyewear", "Goggles", "Shades")

    companion object {
        private const val CONFIDENCE_THRESHOLD = 0.65f
    }

    suspend fun classify(bitmap: Bitmap): ClassificationResult {
        val image = InputImage.fromBitmap(bitmap, 0)
        
        try {
            val faces = faceDetector.process(image).await()
            if (faces.isNotEmpty()) {
                return ClassificationResult(ImageCategory.MEMORY, isPerson = true)
            }
        } catch (e: Exception) {
            Timber.w(e, "Face detection failed.")
        }
        
        return try {
            val labels = labeler.process(image).await()
            
            var isPersonPhoto = false
            var maxMemoryScore = 0f
            var maxDocScore = 0f
            
            labels.forEach { label ->
                if (personKeywords.any { label.text.contains(it, ignoreCase = true) }) {
                     if (label.confidence > maxMemoryScore) {
                        maxMemoryScore = label.confidence
                        isPersonPhoto = true
                    }
                } else if (memoryKeywords.any { label.text.contains(it, ignoreCase = true) }) {
                    if (label.confidence > maxMemoryScore) {
                        maxMemoryScore = label.confidence
                        isPersonPhoto = false
                    }
                }
                
                if (documentKeywords.any { label.text.contains(it, ignoreCase = true) }) {
                    if (label.confidence > maxDocScore) {
                        maxDocScore = label.confidence
                    }
                }
            }

            val finalCategory = when {
                maxMemoryScore >= CONFIDENCE_THRESHOLD -> ImageCategory.MEMORY
                maxDocScore >= CONFIDENCE_THRESHOLD -> ImageCategory.DOCUMENT
                else -> ImageCategory.OBJECT
            }

            if (finalCategory == ImageCategory.MEMORY) {
                ClassificationResult(finalCategory, isPerson = isPersonPhoto)
            } else {
                ClassificationResult(finalCategory)
            }

        } catch (e: Exception) {
            ClassificationResult(ImageCategory.OBJECT)
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

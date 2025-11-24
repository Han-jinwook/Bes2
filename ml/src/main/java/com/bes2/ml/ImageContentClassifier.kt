package com.bes2.ml

import android.graphics.Bitmap
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.label.ImageLabeling
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import javax.inject.Inject

enum class ImageCategory {
    MEMORY,   // Keep: Person, Food, Landscape, Pet
    DOCUMENT  // Clean: Document, Receipt, Text, Screen, Objects
}

class ImageContentClassifier @Inject constructor() {

    private val labeler = ImageLabeling.getClient(ImageLabelerOptions.DEFAULT_OPTIONS)

    // Keywords that indicate this image is a "Memory" to be reviewed
    private val memoryKeywords = setOf(
        "Food", "Cuisine", "Dish", "Meal", "Bakery", "Dessert", "Drink",
        "Person", "Face", "Human", "People", "Selfie", "Smile",
        "Nature", "Landscape", "Sky", "Cloud", "Sunset", "Sunrise", "Beach", "Mountain", "Tree", "Plant", "Flower",
        "Animal", "Pet", "Dog", "Cat", "Bird"
    )

    // Keywords that indicate this image is a "Document" or "Object" to be cleaned
    private val documentKeywords = setOf(
        "Document", "Text", "Paper", "Receipt", "Invoice",
        "Screen", "Monitor", "Display", "Screenshot", 
        "Whiteboard", "Blackboard", "Poster",
        "Handwriting", "Drawing", "Sketch", "Diagram", "Pattern", "Design" // Added handwriting keywords
    )

    suspend fun classify(bitmap: Bitmap): ImageCategory {
        val image = InputImage.fromBitmap(bitmap, 0)
        
        return try {
            val labels = labeler.process(image).await()
            
            val labelTexts = labels.map { "${it.text} (${String.format("%.2f", it.confidence)})" }
            Timber.d("Image Labels Detected: $labelTexts")

            // Calculate Max Confidence Score for each category
            var maxMemoryScore = 0f
            var maxDocumentScore = 0f

            labels.forEach { label ->
                val text = label.text
                val score = label.confidence

                // Check Memory
                if (memoryKeywords.any { text.contains(it, ignoreCase = true) }) {
                    if (score > maxMemoryScore) maxMemoryScore = score
                }

                // Check Document
                if (documentKeywords.any { text.contains(it, ignoreCase = true) }) {
                    if (score > maxDocumentScore) maxDocumentScore = score
                }
            }

            Timber.d("Score -> Memory: $maxMemoryScore vs Document: $maxDocumentScore")

            // Classification Logic: Compare Scores
            // If Memory score is significantly high OR higher than document score, keep it.
            // But if Document score is very high and Memory is low/medium, treat as Document.
            
            // Heuristic:
            // 1. If Memory > Document -> MEMORY
            // 2. If Document > Memory -> DOCUMENT
            // 3. If both are 0 (No labels matched) -> DOCUMENT (Default to clean)
            
            if (maxMemoryScore > 0 && maxMemoryScore >= maxDocumentScore) {
                Timber.d("Classified as MEMORY")
                ImageCategory.MEMORY
            } else {
                Timber.d("Classified as DOCUMENT")
                ImageCategory.DOCUMENT
            }

        } catch (e: Exception) {
            Timber.e(e, "Error classifying image content. Defaulting to DOCUMENT.")
            ImageCategory.DOCUMENT
        }
    }
}

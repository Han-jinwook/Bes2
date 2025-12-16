package com.bes2.background.util

import com.bes2.data.model.ImageItemEntity
import com.bes2.ml.FaceEmbedder
import com.bes2.ml.ImagePhashGenerator
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.abs

@Singleton
class ImageClusteringHelper @Inject constructor(
    private val faceEmbedder: FaceEmbedder
) {

    companion object {
        private const val HAMMING_DISTANCE_THRESHOLD = 12
        private const val TIME_THRESHOLD_MS = 3 * 60 * 1000 // 3 Minutes
        private const val FACE_SIMILARITY_THRESHOLD = 0.85f
        private const val TAG = "Bes2Clustering"
        private const val ORPHAN_MERGE_TIME_THRESHOLD_MS = 3 * 60 * 1000
    }
    
    init {
        Timber.tag(TAG).i("ImageClusteringHelper initialized.")
    }

    data class Cluster(val images: MutableList<ImageItemEntity>)

    fun clusterImages(images: List<ImageItemEntity>): List<Cluster> {
        if (images.isEmpty()) {
            Timber.tag(TAG).w("clusterImages called with empty list.")
            return emptyList()
        }
        Timber.tag(TAG).i("Starting clustering for ${images.size} images.")

        val sortedImages = images.sortedBy { it.timestamp }
        val clusters = mutableListOf<Cluster>()

        var currentCluster = Cluster(mutableListOf(sortedImages.first()))
        clusters.add(currentCluster)

        for (i in 1 until sortedImages.size) {
            val prevImage = sortedImages[i - 1]
            val currentImage = sortedImages[i]

            val timeDiff = abs(prevImage.timestamp - currentImage.timestamp)

            if (timeDiff <= TIME_THRESHOLD_MS && areVisuallySimilar(prevImage, currentImage)) {
                currentCluster.images.add(currentImage)
            } else {
                Timber.tag(TAG).d("New cluster created. Previous cluster size: ${currentCluster.images.size}")
                currentCluster = Cluster(mutableListOf(currentImage))
                clusters.add(currentCluster)
            }
        }
        Timber.tag(TAG).i("Initial clustering finished. ${clusters.size} clusters found.")
        
        val merged = mergeOrphanClusters(clusters.toMutableList())
        Timber.tag(TAG).i("Orphan merging finished. ${merged.size} final clusters.")
        return merged
    }

    private fun mergeOrphanClusters(originalClusters: MutableList<Cluster>): List<Cluster> {
        if (originalClusters.size <= 1) return originalClusters
        Timber.tag(TAG).d("Merging ${originalClusters.size} small/orphan clusters...")

        val mergedClusters = mutableListOf<Cluster>()
        originalClusters.sortBy { it.images.first().timestamp }

        var currentBase = originalClusters[0]

        for (i in 1 until originalClusters.size) {
            val nextCluster = originalClusters[i]

            val isCurrentSmall = currentBase.images.size < 3
            val isNextSmall = nextCluster.images.size < 3
            
            val timeGap = abs(currentBase.images.last().timestamp - nextCluster.images.first().timestamp)

            if ((isCurrentSmall || isNextSmall) && timeGap <= ORPHAN_MERGE_TIME_THRESHOLD_MS) {
                Timber.tag(TAG).v("Merging cluster due to time gap ($timeGap ms).")
                currentBase.images.addAll(nextCluster.images)
                currentBase.images.sortBy { it.timestamp }
            } else {
                mergedClusters.add(currentBase)
                currentBase = nextCluster
            }
        }
        mergedClusters.add(currentBase)

        return mergedClusters
    }

    private fun areVisuallySimilar(image1: ImageItemEntity, image2: ImageItemEntity): Boolean {
        val hasFace1 = image1.faceEmbedding != null
        val hasFace2 = image2.faceEmbedding != null
        
        if (hasFace1 != hasFace2) {
            Timber.tag(TAG).d("Content Mismatch: Face vs No-Face. [${image1.id}] vs [${image2.id}] -> Not similar")
            return false
        }

        if (hasFace1 && hasFace2) {
            val faceSimilarity = areFacesSimilar(image1, image2)
            if (faceSimilarity) {
                Timber.tag(TAG).i("Similar by Face: [${image1.id}] vs [${image2.id}]")
                return true
            }
            Timber.tag(TAG).d("Different Faces: [${image1.id}] vs [${image2.id}] -> Not similar")
            return false
        }

        val pHashSimilarity = arePhashSimilar(image1, image2)
        if (pHashSimilarity) {
            Timber.tag(TAG).i("Similar by pHash: [${image1.id}] vs [${image2.id}]")
            return true
        }
        
        Timber.tag(TAG).d("Not similar (pHash): [${image1.id}] vs [${image2.id}]")
        return false
    }

    private fun arePhashSimilar(image1: ImageItemEntity, image2: ImageItemEntity): Boolean {
        val pHash1 = image1.pHash
        val pHash2 = image2.pHash

        if (pHash1 == null || pHash2 == null || pHash1.length != pHash2.length || pHash1.isEmpty()) {
             Timber.tag(TAG).w("Cannot compare pHash: one or both are null/empty.")
            return false
        }

        val distance = ImagePhashGenerator.calculateHammingDistance(pHash1, pHash2)
        val isSimilar = distance <= HAMMING_DISTANCE_THRESHOLD
        
        Timber.tag(TAG).d("pHash comparison: distance=$distance (Threshold<=$HAMMING_DISTANCE_THRESHOLD) -> $isSimilar")
        return isSimilar
    }

    private fun areFacesSimilar(image1: ImageItemEntity, image2: ImageItemEntity): Boolean {
        val embedding1 = image1.faceEmbedding!!
        val embedding2 = image2.faceEmbedding!!

        val buffer1 = java.nio.ByteBuffer.wrap(embedding1)
        val face1 = FloatArray(embedding1.size / 4)
        buffer1.asFloatBuffer().get(face1)

        val buffer2 = java.nio.ByteBuffer.wrap(embedding2)
        val face2 = FloatArray(embedding2.size / 4)
        buffer2.asFloatBuffer().get(face2)

        val similarity = FaceEmbedder.calculateCosineSimilarity(face1, face2)
        val isSimilar = similarity >= FACE_SIMILARITY_THRESHOLD
        
        Timber.tag(TAG).d("Face comparison: similarity=$similarity (Threshold>=$FACE_SIMILARITY_THRESHOLD) -> $isSimilar")
        return isSimilar
    }
}

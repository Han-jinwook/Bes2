package com.bes2.background.util

import com.bes2.data.model.ImageItemEntity
import com.bes2.ml.FaceEmbedder
import com.bes2.ml.ImagePhashGenerator
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.abs

@Singleton
class ImageClusteringHelper @Inject constructor(
    private val faceEmbedder: FaceEmbedder
) {

    companion object {
        private const val HAMMING_DISTANCE_THRESHOLD = 15
        private const val TIME_THRESHOLD_MS = 3 * 60 * 1000 // 3 Minutes (Standard)
        private const val FACE_SIMILARITY_THRESHOLD = 0.85f 
        
        // [MODIFIED] Reduced orphan merge threshold to 3 mins (Natural merging)
        private const val ORPHAN_MERGE_TIME_THRESHOLD_MS = 3 * 60 * 1000 
    }

    data class Cluster(val images: MutableList<ImageItemEntity>)

    fun clusterImages(images: List<ImageItemEntity>): List<Cluster> {
        val sortedImages = images.sortedBy { it.timestamp }.toMutableList()
        val clusters = mutableListOf<Cluster>()

        // 1. Initial Clustering
        while (sortedImages.isNotEmpty()) {
            val currentImage = sortedImages.removeAt(0)
            val newCluster = Cluster(mutableListOf(currentImage))
            val iterator = sortedImages.iterator()

            while (iterator.hasNext()) {
                val otherImage = iterator.next()
                
                val timeDiff = abs(currentImage.timestamp - otherImage.timestamp)
                
                if (timeDiff <= TIME_THRESHOLD_MS) {
                    if (areVisuallySimilar(currentImage, otherImage)) {
                        newCluster.images.add(otherImage)
                        iterator.remove()
                    }
                }
            }
            clusters.add(newCluster)
        }
        
        // 2. Merge Orphan Clusters
        return mergeOrphanClusters(clusters)
    }
    
    private fun mergeOrphanClusters(originalClusters: MutableList<Cluster>): List<Cluster> {
        if (originalClusters.size <= 1) return originalClusters
        
        val mergedClusters = mutableListOf<Cluster>()
        // Sort clusters by representative time
        originalClusters.sortBy { it.images.first().timestamp }
        
        var currentBase = originalClusters[0]
        
        for (i in 1 until originalClusters.size) {
            val nextCluster = originalClusters[i]
            
            val isCurrentSmall = currentBase.images.size < 3
            val isNextSmall = nextCluster.images.size < 3
            
            // Check time gap (Strict 3 mins)
            val timeGap = abs(currentBase.images.last().timestamp - nextCluster.images.first().timestamp)
            
            if ((isCurrentSmall || isNextSmall) && timeGap <= ORPHAN_MERGE_TIME_THRESHOLD_MS) {
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
        if (arePhashSimilar(image1, image2)) {
            return true
        }
        
        if (areFacesSimilar(image1, image2)) {
            return true
        }
        
        return false
    }

    private fun arePhashSimilar(image1: ImageItemEntity, image2: ImageItemEntity): Boolean {
        val pHash1 = image1.pHash
        val pHash2 = image2.pHash
        
        if (pHash1 == null || pHash2 == null || pHash1.length != pHash2.length) {
            return false
        }
        
        val distance = ImagePhashGenerator.calculateHammingDistance(pHash1, pHash2)
        return distance <= HAMMING_DISTANCE_THRESHOLD
    }

    private fun areFacesSimilar(image1: ImageItemEntity, image2: ImageItemEntity): Boolean {
        val embedding1 = image1.faceEmbedding
        val embedding2 = image2.faceEmbedding

        if (embedding1 == null || embedding2 == null) {
            return false
        }
        
        val buffer1 = java.nio.ByteBuffer.wrap(embedding1)
        val face1 = FloatArray(embedding1.size / 4)
        buffer1.asFloatBuffer().get(face1)

        val buffer2 = java.nio.ByteBuffer.wrap(embedding2)
        val face2 = FloatArray(embedding2.size / 4)
        buffer2.asFloatBuffer().get(face2)

        val similarity = FaceEmbedder.calculateCosineSimilarity(face1, face2)
        return similarity >= FACE_SIMILARITY_THRESHOLD
    }
}

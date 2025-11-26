package com.bes2.ml.util

import android.content.Context
import timber.log.Timber
import java.io.BufferedReader
import java.io.InputStreamReader
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SimpleTokenizer @Inject constructor(
    private val context: Context
) {
    private val vocab = HashMap<String, Int>()
    private val vocabFile = "vocab.txt" // Standard CLIP vocabulary file name
    private var isInitialized = false

    // CLIP standard special tokens
    private val START_TOKEN = 49406
    private val END_TOKEN = 49407
    private val MAX_LENGTH = 77

    init {
        loadVocab()
    }

    private fun loadVocab() {
        try {
            val assetManager = context.assets
            // Check if file exists roughly
            val assets = assetManager.list("")
            if (assets?.contains(vocabFile) == true) {
                assetManager.open(vocabFile).use { inputStream ->
                    BufferedReader(InputStreamReader(inputStream)).use { reader ->
                        reader.forEachLine { line ->
                            // Assuming vocab file format: "word index" or just "word" (line number = index)
                            // Usually CLIP vocab is just a list of tokens line by line.
                            val parts = line.split(" ")
                            if (parts.size >= 1) {
                                val token = parts[0]
                                // If index is not provided, use size as index
                                vocab[token] = vocab.size 
                            }
                        }
                    }
                }
                isInitialized = true
                Timber.d("SimpleTokenizer: Loaded ${vocab.size} tokens.")
            } else {
                Timber.w("SimpleTokenizer: vocab.txt not found in assets. Tokenizer will not work correctly.")
            }
        } catch (e: Exception) {
            Timber.e(e, "SimpleTokenizer: Failed to load vocabulary.")
        }
    }

    fun tokenize(text: String): IntArray {
        if (!isInitialized) {
            Timber.w("SimpleTokenizer: Returning empty tokens (Not Initialized).")
            return IntArray(MAX_LENGTH)
        }

        // 1. Basic cleaning
        val cleaned = text.lowercase().trim()
        
        // 2. Split (Naive implementation - ideally needs BPE logic)
        // For standard English, splitting by space is okay for a start.
        // For Korean, character-based or subword splitting is needed.
        // This is a placeholder for the actual BPE algorithm.
        val words = cleaned.split(Regex("\\s+"))

        val tokens = ArrayList<Int>()
        tokens.add(START_TOKEN)

        for (word in words) {
            if (tokens.size >= MAX_LENGTH - 1) break // Reserve space for END_TOKEN

            // Naive lookup: Exact match
            if (vocab.containsKey(word)) {
                vocab[word]?.let { tokens.add(it) }
            } else {
                // If unknown, simple fallback: try char by char or skip
                // For Korean support, we need a robust BPE library.
                // Here we just skip unknowns to prevent crashes.
            }
        }

        tokens.add(END_TOKEN)

        // Padding
        val result = IntArray(MAX_LENGTH) { 0 }
        for (i in tokens.indices) {
            result[i] = tokens[i]
        }
        
        return result
    }
}

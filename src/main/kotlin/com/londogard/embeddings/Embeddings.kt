package com.londogard.embeddings

import com.londogard.embeddings.utils.SimpleDistances
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.math.sqrt
import kotlin.streams.asSequence

abstract class Embeddings {
    abstract val dimensions: Int
    abstract val delimiter: Char
    abstract val normalized: Boolean
    abstract val filename: String

    internal abstract val embeddings: Map<String, Array<Float>>

    /** Vocabulary of the embeddings */
    val vocabulary by lazy { embeddings.keys }

    /** Check if the word is present in the vocab map.
     * @param word Word to be checked.
     * @return True if the word is in the vocab map.
     */
    fun contains(word: String): Boolean = embeddings.contains(word)

    /** Get the vector representation for the word.
     * @param word Word to retrieve vector for.
     * @return The vector representation of the word.
     */
    fun vector(word: String): Array<Float>? = embeddings[word]

    /** Compute the Euclidean distance between the vector representations of the words.
     * @param w1 The first word.
     * @param w2 The other word.
     * @return The Euclidean distance between the vector representations of the words.
     */
    fun euclidean(w1: String, w2: String): Double? = traverseVectors(listOf(w1, w2))?.let { vectors ->
        if (vectors.size == 2) SimpleDistances.euclidean(vectors.first(), vectors.last())
        else null
    }

    /** Compute the cosine similarity score between the vector representations of the words.
     * @param w1 The first word.
     * @param w2 The other word.
     * @return The cosine similarity score between the vector representations of the words.
     */
    fun cosine(w1: String, w2: String): Double? = traverseVectors(listOf(w1, w2))?.let { vectors ->
        if (vectors.size == 2) SimpleDistances.cosine(vectors.first(), vectors.last())
        else null
    }

    internal fun traverseVectors(words: List<String>): List<Array<Float>>? = words
        .fold(listOf<Array<Float>>() as List<Array<Float>>?) { agg, word ->
            vector(word)?.let { vector -> (agg ?: emptyList()) + listOf(vector) }
        }

    internal fun loadEmbeddingsFromFile(inFilter: Set<String> = emptySet(),
                                        maxWordCount: Int = Int.MAX_VALUE): Map<String, Array<Float>> = Files
        .newBufferedReader(Paths.get(filename))
        .use { reader ->
            reader
                .lines()
                .filter { line -> inFilter.isEmpty() || inFilter.contains(line.takeWhile { it != delimiter }) }
                .asSequence()
                .mapNotNull { line ->
                    line
                        .split(delimiter)
                        .filterNot { it.isEmpty() || it.isBlank() }
                        .takeIf { it.size > dimensions }
                        ?.let { elems ->
                            val key = elems.first()
                            val value = Array(dimensions) { i -> elems[i + 1].toFloat() }
                                .let { if (normalized) it.normalize() else it }

                            key to value
                        }
                }
                .take(maxWordCount)
                .toMap()
        }
}
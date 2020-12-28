package com.londogard.embeddings.utils

import com.londogard.embeddings.`--`
import com.londogard.embeddings.dot
import kotlin.math.sqrt

object SimpleDistances {
    /** Compute the cosine similarity score between two vectors.
     * 1.0 means equal, 0 = 90* & -1 is when they're opposite
     * @param v1 The first vector.
     * @param v2 The other vector.
     * @return The cosine similarity score of the two vectors.
     */
    fun cosine(v1: Array<Float>, v2: Array<Float>): Double {
        if (v1.size != v2.size) throw ArithmeticException("Vectors must be same size (v1: ${v1.size} != v2: ${v2.size}")

        return v1.dot(v2) / (sqrt(v1.dot(v1)) * sqrt(v2.dot(v2)))
    }

    /** Compute the Euclidean distance between two vectors.
     * @param v1 The first vector.
     * @param v2 The other vector.
     * @return The Euclidean distance between the two vectors.
     */
    fun euclidean(v1: Array<Float>, v2: Array<Float>): Double =
        (v1 `--` v2).let { vector -> sqrt(vector.dot(vector)) }
}
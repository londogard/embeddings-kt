package com.londogard.embeddings

import smile.math.matrix.FloatMatrix
import kotlin.math.pow

// Implementation based on: https://github.com/kawine/usif/blob/master/usif.py
class USifSentenceEmbeddings(
    private val embeddings: Embeddings,
    private val wordProb: Array<Float>,
    private val wordIndices: Map<String, Int>,
    randomWalkLength: Int, // = n
    private val numCommonDiscourseVector: Int = 5 // = m, 0 should work
) : SentenceEmbeddings {
    private val vocabSize = wordProb.size
    private val threshold = 1 - (1 - 1 / vocabSize.toFloat()).pow(randomWalkLength)
    private val alpha = wordProb.count { prob -> prob > threshold } / vocabSize.toFloat()
    private val Z = vocabSize / 2.0
    private val a = (1 - alpha) / (alpha * Z)
    private val punctRegex = ".*[A-Za-z0-9].*".toRegex()
    private val charsToRemove: Set<Char> = setOf(';', '.', ':', '(', ')')

    init {
        if (randomWalkLength < 0) throw IllegalArgumentException("randomWalkLength must be greater than 0 (was: $randomWalkLength)")
    }

    private fun weight(word: String): Double = a / (0.5 * a + (wordIndices[word]?.let(wordProb::get) ?: 0f))

    private fun preprocess(token: String): List<String> =
        token
            .toLowerCase()
            .filterNot(charsToRemove::contains)
            .replace("n't", "not")
            .split('-')

    override fun getSentenceEmbeddings(listOfTokens: List<List<String>>): List<Array<Float>> {
        val vectors = listOfTokens.map(this::getSentenceEmbedding)

        return if (numCommonDiscourseVector == 0) vectors
        else {
            val svd = FloatMatrix(vectors.map(Array<Float>::toFloatArray).toTypedArray())
                .svd(true, true)
            val singularValueSum = svd.s.sumOf { it.pow(2).toDouble() }.toFloat()

            (0 until numCommonDiscourseVector)
                .fold(vectors) { acc, i ->
                    val lambdaI = svd.s[i].pow(2) / singularValueSum
                    val pc = svd.V.row(i).map(Double::toFloat).toTypedArray()
                    acc.map { element -> (element `--` (project(element, pc)).mMul(lambdaI)) }
                }
        }
    }

    override fun getSentenceEmbedding(tokens: List<String>): Array<Float> =
        tokens
            .filterNot(punctRegex::matches)
            .flatMap(this::preprocess)
            .filter(embeddings.vocabulary::contains)
            .let { processedTokens ->
                if (processedTokens.isEmpty()) Array(embeddings.dimensions) { a.toFloat() }
                else {
                    processedTokens
                        .mapNotNull(embeddings::vector)
                        .map(Array<Float>::normalize)
                        .mapIndexed { i, array -> array.mMul(weight(tokens[i]).toFloat()) }
                        .mean()

                }
            }
}
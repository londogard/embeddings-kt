package com.londogard.embeddings

import smile.math.matrix.Matrix
import smile.nlp.bag
import smile.nlp.tfidf
import smile.nlp.vectorize
import smile.nlp.words
import smile.projection.PCA

class SifSentenceEmbeddings(val embeddings: Embeddings) : SentenceEmbeddings {
    private lateinit var tfidfMap: Map<String, Float>
    private lateinit var pca: PCA
    private val default: Float = 0.0f

    fun train(sentences: List<String>, threshold: Double = 0.0, k: Int = Int.MAX_VALUE) {
        val corpus = sentences.map { it.bag(stemmer = null) }
        val words = corpus.flatMap { bag -> bag.keys }.distinct()
        val bags = corpus.map { vectorize(words.toTypedArray(), it) }
        val vectors = tfidf(bags)

        val vector = Matrix(embeddings.dimensions, vectors.size, vectors.toTypedArray()).colSums()
        val vecMax = vector.max() ?: 1.0
        tfidfMap = vector
            .map { it / vecMax }
            .mapIndexedNotNull { idx, rel -> if (rel > threshold) words[idx] to rel.toFloat() else null }
            .let { relMap -> if (k < Int.MAX_VALUE) relMap.sortedByDescending { it.second }.take(k) else relMap }
            .toMap()
        val weightedArrays = sentences.map { sentence -> sentence
            .words()
            .mapNotNull { word -> embeddings.vector(word)?.mMul(tfidfMap.getOrDefault(word, default)) }
            .sumByColumns()
            .normalize()
            .map(Float::toDouble)
            .toDoubleArray()
        }.toTypedArray()

        pca = PCA.fit(weightedArrays).setProjection(1)
    }

    fun loadPretrained(pretrained: Map<String, Float>, pretrainedPca: PCA) {
        tfidfMap = pretrained
        pca = pretrainedPca
    }

    override fun getSentenceEmbedding(tokens: List<String>): Array<Float> {
        if (!this::tfidfMap.isInitialized) throw Error("Not trained!")
        val weightedArray = tokens  // TODO make default = lowest val
            .mapNotNull { word -> embeddings.vector(word)?.mMul(tfidfMap.getOrDefault(word, default)) }
            .sumByColumns()
            .normalize()
            .map(Float::toDouble).toDoubleArray()

        val m = Matrix(weightedArray)

        return m
            .sub(m.mul(pca.projection.transpose()).mul(pca.projection)) // TODO perhaps remove rest of PCA?
            .toArray()
            .first()
            .map(Double::toFloat)
            .toTypedArray()
    }
}
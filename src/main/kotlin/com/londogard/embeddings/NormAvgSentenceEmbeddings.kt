package com.londogard.embeddings

class NormAvgSentenceEmbeddings(val embeddings: Embeddings) : SentenceEmbeddings {
    override fun getSentenceEmbedding(tokens: List<String>): Array<Float> = tokens
        .mapNotNull(embeddings::vector)
        .sumByColumns()
        .normalize()
}
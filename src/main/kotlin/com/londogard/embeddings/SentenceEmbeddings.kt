package com.londogard.embeddings

interface SentenceEmbeddings {
    fun getSentenceEmbedding(tokens: List<String>): Array<Float>
}
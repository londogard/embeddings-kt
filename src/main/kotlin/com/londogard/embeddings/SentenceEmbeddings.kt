package com.londogard.embeddings

interface SentenceEmbeddings {
    fun getSentenceEmbedding(tokens: List<String>): Array<Float>
    fun getSentenceEmbeddings(listOfTokens: List<List<String>>): List<Array<Float>> =
        listOfTokens.map(this::getSentenceEmbedding)
}
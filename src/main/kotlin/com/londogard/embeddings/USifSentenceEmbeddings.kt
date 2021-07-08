package com.londogard.embeddings

import com.londogard.embeddings.utils.SimpleDistances
import com.londogard.embeddings.utils.SimpleWordTokenizer
import com.londogard.nlp.stopwords.Stopwords
import com.londogard.nlp.utils.LanguageSupport
import com.londogard.nlp.wordfreq.WordFrequencies
import smile.math.matrix.FloatMatrix
import smile.nlp.dictionary.StopWords
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.math.pow

// Implementation based on: https://github.com/kawine/usif/blob/master/usif.py
class USifSentenceEmbeddings(
    private val embeddings: Embeddings,
    private val wordProb: Map<String, Float>,
    randomWalkLength: Int, // = n, ~11
    private val numCommonDiscourseVector: Int = 5 // = m, 0 should work. In practise max 5.
) : SentenceEmbeddings {
    private val vocabSize = wordProb.size.toFloat()
    private val threshold = 1 - (1 - 1 / vocabSize).pow(randomWalkLength)
    private val alpha = wordProb.count { (_, prob) -> prob > threshold } / vocabSize
    private val Z = vocabSize / 2
    private val a = (1 - alpha) / (alpha * Z)
    private val stopWords = setOf("jag", "hur", "som")

    init {
        if (randomWalkLength < 0) throw IllegalArgumentException("randomWalkLength must be greater than 0 (was: $randomWalkLength)")
    }

    private fun weight(word: String): Double = a / (0.5 * a + wordProb.getOrDefault(word, 0f))

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
            .filter(embeddings.vocabulary::contains)
            .let { processedTokens ->
                if (processedTokens.isEmpty()){
                    println("Empty array")
                    Array(embeddings.dimensions) { a }
                }
                else {
                    processedTokens
                        .mapNotNull(embeddings::vector)
                        .map(Array<Float>::normalize)
                        .mapIndexed { i, array -> array.mMul(weight(processedTokens[i]).toFloat()) }
                        .mean()
                }
            }
}

object test {
    @JvmStatic
    fun main(args: Array<String>) {
        val wordProb = WordFrequencies.getAllWordFrequenciesOrNull()
        val embedd = WordEmbeddings(filename="/home/londogard/summarize-embeddings/cc.sv.50.vec")
        val usif = USifSentenceEmbeddings(embedd, wordProb, 11, 0)

        val covidFaq = javaClass
            .getResourceAsStream("/covid_faq.tsv")
            .bufferedReader().readLines().asSequence()
            .drop(1)
            .filterNot(String::isEmpty)
            .map { line -> line.split('\t') }
            .filter { line -> line.size == 2 }
            .map { line -> line[0] to line[1] }
            .toMap()

        val stopWords = Stopwords.stopwords(LanguageSupport.sv)

        val tokenizer = SimpleWordTokenizer(true)
        val covidTitleEmbeddings = covidFaq
            .map { (question, ans) -> question to tokenizer.split("$question $ans".toLowerCase()).filterNot(stopWords::contains) }
            .map { (question, tokens) -> question to usif.getSentenceEmbedding(tokens) }

        while (true) {
            print("Question: ")
            val q = readLine()?.toLowerCase()
            if (q == null || q == "q") return
            println(tokenizer.split(q).filterNot(stopWords::contains))
            val embedding = usif.getSentenceEmbedding(tokenizer.split(q).filterNot(stopWords::contains))
            val closest = covidTitleEmbeddings
                .map { (q, e) -> q to SimpleDistances.cosine(embedding, e) }
                .sortedByDescending { (_, dist) -> dist }
                .take(20)
                .map { (question, dist) -> "$dist: $question" }

            println("Closest (5) questions to '$q' :")
            println(closest.joinToString("\n"))
            println("=== New Question Please ===")
        }
    }
}
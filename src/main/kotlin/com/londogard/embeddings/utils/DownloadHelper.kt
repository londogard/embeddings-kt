package com.londogard.embeddings.utils

import com.londogard.embeddings.NormAvgSentenceEmbeddings
import com.londogard.embeddings.WordEmbeddings
import smile.nlp.words
import java.io.File
import java.io.FileOutputStream
import java.net.URL
import java.nio.file.Files
import java.util.zip.ZipFile
import kotlin.system.exitProcess

object DownloadHelper {
    private val embeddingDirPath: String = "${System.getProperty("user.home")}${File.separator}summarize-embeddings"
    val embeddingPath: String = "$embeddingDirPath${File.separator}glove.6B.50d.txt"
    const val dimension: Int = 50

    fun embeddingsExist(): Boolean = File(embeddingDirPath).let {
        it.exists() && it.isDirectory && it.listFiles()?.asList()?.isNotEmpty() == true
    }

    private fun String.saveTo(path: String) {
        URL(this).openStream().use { input ->
            FileOutputStream(File(path)).use { output ->
                input.copyTo(output)
            }
        }
    }

    /**
     * 1. Download to temp directory
     * 2. Extract embeddings into 'summarize-embeddings' which is placed in root of users home folder.
     */
    fun downloadGloveEmbeddings() {
        if (embeddingsExist()) {
            println("Embeddings exist in path $embeddingDirPath, early exiting...")
            return
        }

        val tempFile = Files.createTempFile("glove", ".zip")
        val tempPath = tempFile.toAbsolutePath().toString()
        val customDir = File(embeddingDirPath)

        if (!customDir.exists()) customDir.mkdir()

        println("Downloading X GB of Glove Word Embeddings (this will take a while, ~1 GB)...")
        "http://downloads.cs.stanford.edu/nlp/data/glove.6B.zip".saveTo(tempPath)
        println("Download done!")
        println("Extracting 50d word embeddings (from $tempPath to $customDir). Extract your own if you want larger.")
        ZipFile(tempPath).use { zip ->
            zip.entries().asSequence()
                .filter { it.name.contains("50d") }
                .forEach { entry ->
                    zip.getInputStream(entry).use { input ->
                        File(customDir.absolutePath + File.separator + entry.name).outputStream()
                            .use { output -> input.copyTo(output) }
                    }
                }
        }
    }

    @JvmStatic
    fun main(args: Array<String>) {
        val embedding = WordEmbeddings(300, "/home/londet/git/embeddings-kt/src/main/resources/sv_300d.tsv")
        val questions = javaClass.getResourceAsStream("/covid.tsv").reader().readLines().drop(1).map { it.split('\t').first().toLowerCase() }
        //val sentences = listOf("dotter", "jävla skit", "son")
        // embedding.addWords(questions.flatMap { it.split(" ") }.toSet())

        val senEmb = NormAvgSentenceEmbeddings(embedding)
        val embeddedSent = questions.map { it to senEmb.getSentenceEmbedding(it.words().toList()) }
        var q: String? = ""
        var qEmb: Array<Float> = emptyArray()
        while (true) {
            println("Question:\n> ")
            q = readLine()
            if (q!!.contains("exit")) exitProcess(0)
            qEmb = senEmb.getSentenceEmbedding(q.words().toList())
            println(embeddedSent.asSequence().map { it.first to embedding.cosine(it.second, qEmb) }.sortedByDescending { it.second }.take(5).toList().map { "${it.first}: ${it.second}" }.joinToString("\n"))
        }
        //val q = senEmb.getSentenceEmbedding("hur påverkas jag med astma?".words().toList())
        //println(embeddedSent.asSequence().map { it.first to embedding.cosine(it.second, q) }.sortedByDescending { it.second }.take(5).toList().map { "${it.first}: ${it.second}" }.joinToString("\n"))
        //embeddedSent.minBy {  }
    }
}
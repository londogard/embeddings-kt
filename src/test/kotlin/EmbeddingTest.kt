import com.londogard.embeddings.*
import org.amshove.kluent.shouldBe
import org.amshove.kluent.shouldBeEqualTo
import org.junit.Test

class EmbeddingTest {
    private val swedishEmbeddings = "sv_300d.tsv"

    @Test
    fun simpleEmbed() {
        val embeddings = WordEmbeddings(
            filename = javaClass.getResource(swedishEmbeddings).path,
            delimiter = ' ',
            normalized = false
        )

        embeddings.contains("i") shouldBe true
        embeddings.vocabulary.size shouldBe 6
        (embeddings.vector("i")?.first() == -1.18112397f) shouldBe true
    }

    @Test
    fun lightEmbeddings() {
        val lightEmbeddings = LightWordEmbeddings(
            filename = javaClass.getResource(swedishEmbeddings).path,
            delimiter = ' ', normalized = false, maxWordCount = 2
        )

        lightEmbeddings.vocabulary.size shouldBe 2
        lightEmbeddings.vocabulary.contains("och")
        lightEmbeddings.addWords(setOf("en", "som"))
        lightEmbeddings.vocabulary.size shouldBe 2
        lightEmbeddings.vocabulary.contains("som")
    }

    @Test
    fun normalizedSentenceEmbeddings() {
        val embeddings = WordEmbeddings(
            filename = javaClass.getResource(swedishEmbeddings).path,
            delimiter = ' ',
            normalized = false,
            dimensions = 300
        )
        val sentenceEmbeddings = NormAvgSentenceEmbeddings(embeddings)
        val sentence = sentenceEmbeddings.getSentenceEmbedding(listOf("i", "och"))
        val revSentence = sentenceEmbeddings.getSentenceEmbedding(listOf("och", "i"))

        sentence shouldBeEqualTo revSentence
        sentence shouldBeEqualTo embeddings.traverseVectors(listOf("i", "och"))?.sumByColumns()?.normalize()
        sentence.size shouldBeEqualTo 300
    }

    @Test
    fun tfIdfSentenceEmbeddings() {
    }

    @Test
    fun sifSentenceEmbeddings() {
    }
}
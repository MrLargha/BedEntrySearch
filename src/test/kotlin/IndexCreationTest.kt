import java.io.File
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertTrue

class IndexCreationTest {
    companion object {
        const val BED_FILENAME = "simpleIndexTest.bed"
        const val BED_INDEX_FILENAME = "simpleIndexTest.bedIndx"
    }

    private lateinit var reader: HashIndexBedReader

    @BeforeTest
    fun prepare() {
        reader = HashIndexBedReader()
        File(BED_FILENAME).delete()
        File(BED_INDEX_FILENAME).delete()
    }

    @Test
    fun simpleIndexCreationTest() {
        val fileSample = """
            chr1    2321    2121    2312321 321321, 3213213, 321, 321, 31   321212 fdskajflk
            chr1    4324    6543    2312321 321321, 3213213, 321, 321, 31   321212 fdskajflk
            chr1    8654    9323    2312321 321321, 3213213, 321, 321, 31   321212 fdskajflk
            chr2    10213    12122    2312321 321321, 3213213, 321, 321, 31   321212 fdskajflk
            chr2    232121    4323232    2312321 321321, 3213213, 321, 321, 31   321212 fdskajflk            
        """.trimIndent().replace("    ", "\t")
        val bed = File(BED_FILENAME).also { it.writeText(fileSample) }
        val indexFile = File(BED_INDEX_FILENAME)

        reader.createIndex(bed.toPath(), indexFile.toPath())
        val index = reader.loadIndex(indexFile.toPath()) as HashIndex

        assertTrue { index.indexData.isNotEmpty() }
        assertTrue { index.indexData.keys.count() == 2 }
        assertTrue(index.indexData.keys.contains("chr1"))
        assertTrue { index.indexData.keys.contains("chr2") }
        assertTrue { index.indexData["chr1"]!!.size == 3 }
        assertTrue { index.indexData["chr2"]!!.size == 2 }
    }
}
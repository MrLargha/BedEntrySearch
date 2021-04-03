import java.io.File
import java.io.FileNotFoundException
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.Path
import kotlin.test.*

/**
 * Test for HashIndexBedReader and HashIndex
 */
class IndexTest {
    /**
     * Filenames, and test file content
     */
    companion object {
        const val BED_FILENAME = "simpleIndexTest.bed"
        const val BED_INDEX_FILENAME = "simpleIndexTest.bedIndx"
        val FILE_SAMPLE = """
            chr1    2321    3521    2312321 321321, 3213213, 321, 321, 31    321212 fdskajflk
            chr1    4324    6543    2312321 321321, 3213213, 321, 321, 31    321212 fdskajflk
            chr1    8654    9323    2312321 321321, 3213213, 321, 321, 31    321212 fdskajflk
            chr2    10213    12122    2312321 321321, 3213213, 321, 321, 31    321212 fdskajflk
            chr2    232121    4323232    2312321 321321, 3213213, 321, 321, 31  321212 fdskajflk""".trimIndent()
            .replace("    ", "\t")
        val entries = listOf(
            BedEntry(
                "chr1",
                2321,
                3521,
                listOf("2312321 321321, 3213213, 321, 321, 31", "321212 fdskajflk")
            ),
            BedEntry(
                "chr1",
                4324,
                6543,
                listOf("2312321 321321, 3213213, 321, 321, 31", "321212 fdskajflk")
            ),
            BedEntry(
                "chr1",
                8654,
                9323,
                listOf("2312321 321321, 3213213, 321, 321, 31", "321212 fdskajflk")
            ),
            BedEntry(
                "chr2",
                10213,
                12122,
                listOf("2312321 321321, 3213213, 321, 321, 31", "321212 fdskajflk")
            ),
            BedEntry(
                "chr2",
                232121,
                4323232,
                listOf("2312321 321321, 3213213, 321, 321, 31  321212 fdskajflk")
            ),
        )
    }

    private lateinit var reader: HashIndexBedReader

    /**
     * Delete all created files and re-create HashIndexBedReader
     */
    @BeforeTest
    fun prepare() {
        println("Cleaning up working files...")
        reader = HashIndexBedReader()
        File(BED_FILENAME).delete()
        File(BED_INDEX_FILENAME).delete()
    }


    /**
     * Test that index created successfully and it can be read correctly
     */
    @Test
    fun simpleIndexCreationTest() {
        val bed = File(BED_FILENAME).also { it.writeText(FILE_SAMPLE) }
        val indexFile = File(BED_INDEX_FILENAME)
        reader.createIndex(bed.toPath(), indexFile.toPath())
        checkSimpleIndex(reader.loadIndex(indexFile.toPath()) as HashIndex)
    }

    /**
     * Test that indexer can correctly read files with headers
     * (test with header of 3 strings with random data)
     */
    @Test
    fun simpleIndexCreationTestWithHeader() {
        val bed = File(BED_FILENAME).also {
            it.writeText("ANY HEADER DATA  bla-bla-bla JFKD\t djfsa\n".repeat(3) + FILE_SAMPLE)
        }
        val indexFile = File(BED_INDEX_FILENAME)
        reader.createIndex(bed.toPath(), indexFile.toPath())
        checkSimpleIndex(reader.loadIndex(indexFile.toPath()) as HashIndex)
    }

    /**
     * Method for checking index created for FILE_SAMPLE data
     */
    private fun checkSimpleIndex(index: HashIndex) {
        assertTrue { index.indexData.isNotEmpty() }
        assertTrue { index.indexData.keys.count() == 2 }
        assertTrue(index.indexData.keys.contains("chr1"))
        assertTrue { index.indexData.keys.contains("chr2") }
        assertTrue { index.indexData["chr1"]!!.size == 3 }
        assertTrue { index.indexData["chr2"]!!.size == 2 }
    }

    /**
     * Simple search test
     */
    @Test
    fun checkSearch() {
        val bed = File(BED_FILENAME).also { it.writeText(FILE_SAMPLE) }
        val indexFile = File(BED_INDEX_FILENAME)
        reader.createIndex(bed.toPath(), indexFile.toPath())
        val index = reader.loadIndex(indexFile.toPath()) as HashIndex

        val r1 = reader.findWithIndex(index, bed.toPath(), "chr1", 2321, 10000)
        assertTrue { r1.size == 3 }
        assertTrue {
            r1.first() == entries.first() && r1[1] == entries[1] && r1[2] == entries[2]
        }
        assertTrue { reader.findWithIndex(index, bed.toPath(), "foobar", 2321, 10000).isEmpty() }
    }

    /**
     * Method to check if the correct data can be read from the middle of the file with header.
     */
    @Test
    fun checkSearchInMidFile() {
        val bed = File(BED_FILENAME).also {
            it.writeText("ANY HEADER DATA  bla-bla-bla JFKD\t djfsa\n".repeat(3) + FILE_SAMPLE)
        }
        val indexFile = File(BED_INDEX_FILENAME)
        reader.createIndex(bed.toPath(), indexFile.toPath())
        val index = reader.loadIndex(indexFile.toPath()) as HashIndex

        val r1 = reader.findWithIndex(index, bed.toPath(), "chr2", 10213, 4323233)
        assertTrue { r1.size == 2 }
        assertTrue {
            r1[0] == entries[3] && r1[1] == entries[4]
        }

        val r2 = reader.findWithIndex(index, bed.toPath(), "chr2", 10213, 4323232)
        assertTrue("End search limit is not exclusive") { r2.size == 1 }
        assertTrue {
            r2[0] == entries[3]
        }
    }

    /**
     * Check for a knowingly incorrect search query
     */
    @ExperimentalPathApi
    @Test
    fun checkWrongSearch() {
        val bed = File(BED_FILENAME).also { it.writeText(FILE_SAMPLE) }
        val indexFile = File(BED_INDEX_FILENAME)
        reader.createIndex(bed.toPath(), indexFile.toPath())
        val index = reader.loadIndex(indexFile.toPath()) as HashIndex

        assertTrue("Index-out-of-range search failed") {
            reader.findWithIndex(
                index,
                bed.toPath(),
                "chr1",
                1000000,
                100000
            ).isEmpty()
        }

        assertTrue("Wrong chromosome search failed") {
            reader.findWithIndex(
                index,
                bed.toPath(),
                "chr10",
                2321,
                10000
            ).isEmpty()
        }

        // Check that FNF exception thrown, not any another exception
        assertFailsWith(exceptionClass = FileNotFoundException::class) {
            reader.findWithIndex(
                index,
                Path("C:\\FooBar\\foobar.bed"), // Any wrong filename
                "chr1",
                2321,
                10000
            )
        }
    }
}
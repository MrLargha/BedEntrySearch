import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.nio.file.Path

/**
 * Implementation of BedReader interface
 * Works with HashIndex indexes
 *
 * @constructor Constructs object
 * @see BedReader
 * @see HashIndex
 */
class HashIndexBedReader : BedReader {
    override fun createIndex(bedPath: Path, indexPath: Path) {
        val fis = bedPath.toFile().inputStream()
        val reader = BufferedReader(InputStreamReader(fis))
        val index = mutableMapOf<String, MutableList<HashIndex.IndexedObject>>()
        var i = 0
        reader.lines().forEach {
            val splt = it.split("\t")
            if (splt.size >= 3) {
                if (!index.containsKey(splt.first())) {
                    index[splt.first()] = mutableListOf()
                }
                index[splt.first()]!!.add(HashIndex.IndexedObject(i, splt[1].toInt(), splt[2].toInt()))
            }
            i++
        }
        val oos = ObjectOutputStream(indexPath.toFile().outputStream())
        oos.writeObject(index)
        oos.flush()
        oos.close()
    }

    override fun loadIndex(indexPath: Path): BedIndex {
        val ios = ObjectInputStream(indexPath.toFile().inputStream())
        return try {
            @Suppress("UNCHECKED_CAST")
            HashIndex(ios.readObject() as IndexStruct)
        } catch (e: ClassCastException) {
            HashIndex(mapOf())
        } finally {
            ios.close()
        }
    }

    override fun findWithIndex(
            index: BedIndex,
            bedPath: Path,
            chromosome: String,
            start: Int,
            end: Int
    ): List<BedEntry> {
        val hashIndex = index as HashIndex
        val result = hashIndex.findInIndex(chromosome, start, end)
        if (result.isEmpty()) {
            return emptyList()
        }
        val reader = BufferedReader(InputStreamReader(bedPath.toFile().inputStream()))
        var currentPos = 0
        val entries = mutableListOf<BedEntry>()
        for (entryID in result) {
            if (currentPos != entryID) {
                repeat(entryID - currentPos) {
                    reader.readLine()
                }
                currentPos += (entryID - currentPos)
            }
            val rawText = reader.readLine().split("\t")
            val entry = BedEntry(rawText[0], rawText[1].toInt(), rawText[2].toInt(), rawText.slice(3..rawText.lastIndex))
            entries.add(entry)
            currentPos++
        }
        reader.close()
        return entries
    }
}
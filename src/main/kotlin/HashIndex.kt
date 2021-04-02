import java.io.Serializable

/**
 * Typealias for HashIndex internal structure
 */
typealias IndexStruct = Map<String, MutableList<HashIndex.IndexedObject>>

/**
 * Index for BedReader, uses HashMap for faster search by chromosome
 */
class HashIndex constructor(val indexData: IndexStruct) : BedIndex {

    /**
     * One index entry, consists of:
     * id - number of string in BED file
     * start - start position (inclusive)
     * end - end position (exclusive)
     */
    data class IndexedObject(val id: Int, val start: Int, val end: Int) : Serializable

    /**
     * Method for retrieving numbers of desired line in BED file
     *
     * @param chromosome - chromosome
     * @param start - start position (inclusive)
     * @param end - end position (exclusive)
     *
     * @return list of numbers of strings in BED that we are looking for
     */
    fun findInIndex(chromosome: String, start: Int, end: Int): List<Int> {
        if (!indexData.containsKey(chromosome)) {
            return emptyList()
        }
        val lst = indexData[chromosome]
        if (lst == null || lst.isEmpty()) {
            return emptyList()
        }
        var startIndex = -1
        for (i in 0..lst.lastIndex) {
            if (lst[i].start >= start) {
                startIndex = i
                break
            }
        }
        println("Start index $startIndex")

        if (startIndex == -1) {
            return emptyList()
        }

        val result = arrayListOf<Int>()
        result.ensureCapacity(lst.size - startIndex)

        lst.subList(startIndex, lst.lastIndex + 1).forEach {
            if (it.end < end)
                result.add(it.id)
        }

        return result.toList()
    }
}
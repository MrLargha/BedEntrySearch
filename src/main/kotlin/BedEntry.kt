/**
 * Representation of 1 line of BED file
 */
data class BedEntry(val chromosome: String, val start: Int, val end: Int, val other: List<Any>)

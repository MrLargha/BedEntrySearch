import java.lang.IndexOutOfBoundsException
import java.lang.NumberFormatException
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.Path
import kotlin.io.path.exists

@ExperimentalPathApi
fun main(args: Array<String>) {
    println("Welcome to Bed Entry Search!")
    val bedPath = Path(".\\human2.bed")
    val indexPath = Path(".\\human2.bedIndx")

    val reader = HashIndexBedReader()

    if (!indexPath.exists()) {
        println("Indexing...")
        reader.createIndex(bedPath, indexPath)
        println("Indexing complete")
    }

    println("Loading index...")
    val index = reader.loadIndex(indexPath)
    println("Index loaded")

    while (true) {
        println("Enter chromosome name, start and end on same line with space as delimiter: ")
        try {
            val (chr, start, end) = readLine()!!.split(' ')
            reader.findWithIndex(index, bedPath, chr, start.toInt(), end.toInt()).forEach { println(it) }
            println(" --------------------- ")
        } catch (e: IndexOutOfBoundsException) {
            println("Make sure, that you've entered all 3 arguments")
        } catch (e: NumberFormatException) {
            println("Check format of numbers")
        }

    }
}
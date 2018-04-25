package pclm

import org.hamcrest.Matchers.isOneOf
import org.junit.Assert
import org.junit.Assert.* // ktlint-disable no-wildcard-imports

/** An iterator that allows sniffing out of the next line start */
interface ByteWindowIterator : Iterator<ByteWindow> {
    val lineStart: Int
}

/**
 * Iterates all lines (delimited by [NEWLINE]) in the window from the end of the window.
 * It is assumed that the last character in the window is a NEWLINE, terminating the final
 * line in the window.
 */
val ByteWindow.linesFromEnd get() = object : Iterable<ByteWindow> {
    override fun iterator() = object : Iterator<ByteWindow> {
        var lineStart = offset + length
        var prevNewLine: Int? = null

        override fun hasNext() = findPrevNewLine() != null

        override fun next(): ByteWindow {
            return copy(offset = prevNewLine!!, length = lineStart - prevNewLine!!).also {
                lineStart = prevNewLine!!
                prevNewLine = null
            }
        }

        private fun findPrevNewLine(): Int? {
            if (prevNewLine != null) prevNewLine
            if (array[lineStart - 1] != NEWLINE) throw Exception("buffer not terminated with newline")
            for (i in (lineStart - 2) downTo offset) {
                if (array[i] == NEWLINE) {
                    prevNewLine = i + 1
                    return i + 1
                }
            }
            return null
        }
    }
}

/**
 * Iterates all lines (delimited by [NEWLINE]) in the window from the start of the window
 */
val ByteWindow.lines get() = object : Iterable<ByteWindow> {
    override fun iterator() = object : ByteWindowIterator {
        override var lineStart = offset
        var nextNewline: Int? = null
        override fun next(): ByteWindow {
            nextNewline ?: throw Exception("EOF")
            val length = nextNewline!! - (lineStart) + 1
            return copy(offset = lineStart, length = length).also {
                nextNewline = null
                lineStart += length
            }
        }

        override fun hasNext() = findNextNewline() != null

        private fun findNextNewline(): Int? {
            if (nextNewline != null) nextNewline

            for (i in lineStart until (offset + length)) {
                if (array[i] == NEWLINE) {
                    nextNewline = i
                    return i
                }
            }
            return null
        }
    }
}

fun ByteWindow.intersects(other: ByteWindow): Boolean {
    // Both arrays must be identical for a valid range comparison
    if (array !== other.array) return false
    if (offset + length <= other.offset) return false
    if (offset >= other.offset + other.length) return false
    return true
}

private fun Iterable<ByteWindow>.stripComments(): Iterable<ByteWindow> = filterNot { it.array[it.offset] == '%'.toByte() }

/** Return an iterator that skips any ByteWindow containing a comment */
private fun Iterator<ByteWindow>.stripComments(): Iterator<ByteWindow> {
    val parent = this
    return object : Iterator<ByteWindow> {
        var place: ByteWindow? = null

        override fun next(): ByteWindow {
            hasNext() // Make sure place is populated
            return place!!.also { place = null }
        }

        override fun hasNext(): Boolean {
            while (place == null && parent.hasNext()) {
                place = parent.next()
                if (place?.let { it.array[it.offset] == '%'.toByte() } == true) {
                    place = null
                }
            }
            return place != null
        }
    }
}

/**
 * Return groups of objects split by items matching a delimiter, where the first group contains
 * all items before the first delimiter, and subsequent groups start with the delimiter
 */
fun <T> List<T>.delimit(isDelimiter: (T) -> Boolean): List<List<T>> {
    val compilation = ArrayList<List<T>>()
    var group = ArrayList<T>()
    forEach {
        if (isDelimiter(it)) {
            compilation.add(group)
            group = ArrayList()
        }
        group.add(it)
    }
    compilation.add(group)
    return compilation
}

data class PdfStructure(
    val header: ByteWindow,
    val body: ByteWindow,
    val xref: ByteWindow,
    val trailer: PclmDictionary,
    val xrefObjects: List<PclmObject>
)

/** Parse and validate the contents of a PCLM file */
fun ByteWindow.validatePclm(): PdfStructure {
    val all = this

    // Header is just the comments until we reach the first body line
    val header = copy(length = lines.first { it.array[it.offset] != '%'.toByte() }.offset - offset)

    // Count backwards from end to find the beginning of the trailer
    val trailer = linesFromEnd.first { it.asString() == "trailer\n" }
            .run { copy(length = all.length - offset) }

    // Seek backwards again from start of trailer to find the xref line
    val xref = copy(length = trailer.offset - offset).linesFromEnd.first { it.asString() == "xref\n" }
            // Extend the xref section forward to hit the start of the trailer
            .run { copy(length = trailer.offset - offset) }

    // The body covers everything after the header up to the start of the xref section
    val body = copy(offset = offset + header.length, length = xref.offset - (offset + header.length))

    assertEquals(length, body.length + header.length + xref.length + trailer.length)

    var freeObjectsCount = 0
    // Carve out all xrefable objects
    val xrefObjectPairs = xref.lines
            .stripComments()
            .drop(1)
            .map {
                val fields = it.asString().trim().split(" ")
                // Each item must be either 20 lines long or a two-value group delimiter
                if (it.length != 20) {
                    Assert.assertEquals("$it must be 20 bytes long or a 2-field line", 2, fields.size)
                }
                fields
            }
            .delimit { it.size == 2 }
            .filterNot { it.isEmpty() }
            .flatMap { group ->
                val startObjectNum = group[0][0].toInt()
                val count = group[0][1].toInt()
                (0 until count).mapNotNull { index ->
                    val line = group[index + 1]
                    when (line[2]) {
                        "f" -> {
                            freeObjectsCount++
                            // Ignore, isn't really needed
                            null
                        }
                        "n" -> {
                            val offset = line[0].toInt()
                            val objectWindow = copy(offset = offset, length = xref.offset - offset)
                            val obj = objectWindow.parseObject()
                            assertEquals("Object number (${obj.second.number}) must match the xref table",
                                    startObjectNum + index, obj.second.number)
                            obj
                        }
                        else -> throw AssertionError("Trouble while parsing $group at $index, neither f nor n: \"${group[index][2]}\"")
                    }
                }
            }

    // Validate there are no intersections between objects and the rest of the file
    val allWindows = xrefObjectPairs.map { it.first } + listOf(header, trailer, xref)
    for (list in allWindows.subLists().filter { it.size > 1 }) {
        for (other in list.drop(1)) {
            if (list[0].intersects(other)) {
                throw AssertionError("Overlap between ${list[0]} and $other")
            }
        }
    }

    val xrefObjects: List<PclmObject> = xrefObjectPairs.map { it.second }

    // Validate the trailer while parsing out the dictionary
    val trailerIterator = trailer.lines.stripComments().iterator()
    assertEquals("trailer\n", trailerIterator.next().asString())
    assertEquals("<<\n", trailerIterator.next().asString())
    val trailerDictionary = trailerIterator.nextDictionary()
    assertEquals("startxref\n", trailerIterator.next().asString())
    // We already did this the hard way, but verify the offset is correct
    assertEquals(xref.offset, trailerIterator.next().asString().trim().toInt())
    assertEquals("trailer specifies incorrect number of objects in xref table",
        PclmNumber(xrefObjects.size + freeObjectsCount), trailerDictionary["Size"])

    // Chase down all other objects starting from the root.
    val catalog = xrefObjects.byRef(trailerDictionary["Root"].toRef())
    assertEquals("root object has bad Type", PclmName("Catalog"), catalog["Type"])
    val pageTree = xrefObjects.byRef(catalog["Pages"].toRef())
    assertEquals("pages object has bad Type", PclmName("Pages"), pageTree["Type"])

    val pageCount = (pageTree["Count"].toNumber()).value.toInt()
    assertTrue("pages ($pageCount) must be positive", pageCount > 0)
    val kids = pageTree["Kids"].toArray()
    assertEquals("$kids must contain one entry per page", pageCount, kids.items.size)

    // Validate each page
    kids.items.map { it.toRef().number }
            .forEach { validatePage(pageTree.number, xrefObjects, xrefObjects.byNumber(it)) }

    return PdfStructure(header, body, xref, trailerDictionary, xrefObjects)
}

fun List<PclmObject>.byNumber(number: Int) = first { it.number == number }
fun List<PclmObject>.byRef(ref: PclmObjectRef) = byNumber(ref.number)

fun validatePage(pageTreeNumber: Int, xrefObjects: List<PclmObject>, page: PclmObject) {
    // Validate page-related references
    assertEquals(PclmName("Page"), page["Type"])
    assertEquals(pageTreeNumber, page["Parent"].toRef().number)

    val mediaBox = page["MediaBox"].toArray()
    assertEquals("MediaBox must have 4 elements", 4, mediaBox.items.size)

    val contentObject = xrefObjects.byNumber(page["Contents"].toArray().items[0].toRef().number)
    val imageNames = validatePageContentStream(contentObject)

    // also collect and validate named Images referenced in Resources >> XObject
    val imageObjects = page["Resources"].toDictionary()["XObject"].toDictionary()

    assertEquals("image objects and content stream names must align", imageObjects.items.map { it.first }, imageNames)

    imageObjects.items.forEach {
        validateStrip(xrefObjects.byRef(it.second.toRef()))
    }

    // Note that strip objects appear AFTER during top-to-bottom but BEFORE during bottom-to-top
}

fun validateStrip(stripObject: PclmObject) {
    assertEquals(PclmName("XObject"), stripObject["Type"])
    assertEquals(PclmName("Image"), stripObject["Subtype"])
    assertTrue(stripObject["Width"].toNumber().value.toInt() > 0)
    assertThat(stripObject["ColorSpace"].toName().name, isOneOf("DeviceRGB", "DeviceGray"))
    assertThat(stripObject["Filter"].toName().name, isOneOf("DCTDecode", "FlateDecode", "RunLengthEncode"))
    assertNotNull(stripObject.stream)
}

fun validatePageContentStream(pageContentStream: PclmObject) =
        // Ignore all other lines, just get the image names
        pageContentStream.stream!!.lines.mapNotNull {
            "/(\\w+) Do Q\n".toRegex().matchEntire(it.asString())?.run { groups[1]?.value }
        }

internal fun ByteWindow.parseObject(): Pair<ByteWindow, PclmObject> {
    val rawIterator = lines.iterator() as ByteWindowIterator

    val iterator = rawIterator.stripComments()
    val header = iterator.next().asString().split(" ")
    val objectNumber = header[0].toInt()
    Assert.assertEquals(0, header[1].toInt()) // generation # is always 0 because we don't edit PCLM
    Assert.assertEquals("obj\n", header[2])

    // First element must be a dictionary
    val startDict = iterator.next()
    Assert.assertEquals("<<\n", startDict.asString())
    val dict = iterator.nextDictionary()

    // In PCLM objects might have a stream so check for it
    val lastLine = iterator.next().asString()
    val streamLength = dict.items.firstOrNull { it.first == "Length" }?.second
    return if (lastLine == "stream\n" && streamLength is PclmNumber) {
        val stream = copy(offset = rawIterator.lineStart, length = streamLength.value.toInt())
        val afterStream = drop(rawIterator.lineStart - offset + streamLength.value.toInt())
        val rawTerminal = afterStream.lines.iterator() as ByteWindowIterator
        val terminal = rawTerminal.stripComments()
        val next = terminal.next().asString()
        when (next) {
            "\n" -> Assert.assertEquals("endstream\n", terminal.next().asString())
            "endstream\n" -> Unit // Good
            else -> throw AssertionError("unrecognized object end at $next")
        }
        Assert.assertEquals("endobj\n", terminal.next().asString())
        copy(length = rawTerminal.lineStart - offset) to PclmObject(objectNumber, dict, stream)
    } else if (lastLine == "endobj\n") {
        copy(length = rawIterator.lineStart - offset) to PclmObject(objectNumber, dict)
    } else {
        throw AssertionError("unrecognized object end")
    }
}

/** Parse and return the next dictionary. Assumes the current line already started it */
internal fun Iterator<ByteWindow>.nextDictionary(): PclmDictionary = pclmDictionary {
    var line = next().asString()
    while (line != ">>\n") {
        val parts = line.trim().split(" ")
        assertTrue("Dictionary line doesn't start with a name: $parts", parts[0][0] == '/')
        val key = parts[0].drop(1)
        assertTrue("Empty key", key.isNotEmpty())
        val value = parts.drop(1)
        assertTrue("$key missing value", value.isNotEmpty())
        if (value.size == 3 && value[2] == "R") {
            assertEquals("object reference generation number must be zero", 0, value[1].toInt())
            add(key to PclmObjectRef(value[0].toInt()))
        } else if (value.size == 1) {
            when {
                value[0] == "<<" ->
                    add(key to nextDictionary())
                value[0][0] == '/' ->
                    add(key to PclmName(value[0].drop(1)))
                value[0].matches(NUMBER_REGEX) ->
                    add(key to PclmNumber(value[0].toBigDecimal()))
                else ->
                    fail("Unrecognized dictionary value type $value")
            }
        } else if (value[0] == "[") {
            add(key to arrayItems(value.drop(1), this@nextDictionary))
        } else {
            fail("Unrecognized dictionary value type $value")
        }
        line = next().asString()
    }
}

val NUMBER_REGEX: Regex = "[-+]?[0-9]+(\\.[0-9]*)?".toRegex()

internal fun arrayItems(line: List<String>, lines: Iterator<ByteWindow>): PclmArray = pclmArray {
    var symbols = line
    while ((symbols.isEmpty() && lines.hasNext()) || (symbols[0] != "]")) {
        if (symbols.isEmpty()) symbols = lines.next().asString().split(" ")
        if (symbols.size > 3 && symbols[2] == "R") {
            assertEquals("object reference generation number must be zero", 0, symbols[1].toInt())
            add(PclmObjectRef(symbols[0].toInt()))
            symbols = symbols.drop(3)
        } else if (symbols[0].matches(NUMBER_REGEX)) {
            add(PclmNumber(symbols[0].toBigDecimal()))
            symbols = symbols.drop(1)
        } else {
            fail("Not sure what to do with symbols: $symbols")
        }
    }

    assertEquals("Didn't find end of array", symbols, listOf("]"))
}

/** Return all non-empty sub-lists from (0 until n), (1 until n), (2 until n), ... (n-1 until n) */
internal fun <E> List<E>.subLists() = (0 until size - 1).map { subList(it, size) }

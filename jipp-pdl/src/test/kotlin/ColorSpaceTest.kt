import com.hp.jipp.pdl.ColorSpace
import com.hp.jipp.util.toHexString
import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

@Suppress("SpellCheckingInspection")
class ColorSpaceTest {
    @Test
    fun `RGBA to Grayscale`() {
        assertEquals(
            "ff0036b612",
            ("ffffffff" + "000000ff" + "ff0000ff" + "00ff00ff" + "0000ffff")
                .convert(ColorSpace.Rgba, ColorSpace.Grayscale)
        )

        // At half alpha, half white
        assertEquals(
            "ff7f9adb88",
            ("ffffff80" + "00000080" + "ff000080" + "00ff0080" + "0000ff80")
                .convert(ColorSpace.Rgba, ColorSpace.Grayscale)
        )

        // At no alpha, pure white
        assertEquals(
            "ffffffffff",
            ("ffffff00" + "00000000" + "ff000000" + "00ff0000" + "0000ff00")
                .convert(ColorSpace.Rgba, ColorSpace.Grayscale)
        )
    }

    @Test
    fun `RGBA to RGB`() {
        assertEquals(
            "ffffff" + "000000" + "ff0000" + "00ff00" + "0000ff",
            ("ffffffff" + "000000ff" + "ff0000ff" + "00ff00ff" + "0000ffff")
                .convert(ColorSpace.Rgba, ColorSpace.Rgb)
        )

        assertEquals(
            "ffffff" + "7f7f7f" + "ff7f7f" + "7fff7f" + "7f7fff",
            ("ffffff80" + "00000080" + "ff000080" + "00ff0080" + "0000ff80")
                .convert(ColorSpace.Rgba, ColorSpace.Rgb)
        )

        assertEquals(
            "ffffff" + "ffffff" + "ffffff" + "ffffff" + "ffffff",
            ("ffffffff" + "00000000" + "ff000000" + "00ff0000" + "0000ff00")
                .convert(ColorSpace.Rgba, ColorSpace.Rgb)
        )
    }

    @Test
    fun `RGBA to RGBA`() {
        assertEquals(
            ("ffffffff" + "000000ff" + "ff0000ff" + "00ff00ff" + "0000ffff"),
            ("ffffffff" + "000000ff" + "ff0000ff" + "00ff00ff" + "0000ffff")
                .convert(ColorSpace.Rgba, ColorSpace.Rgba)
        )
    }

    @Test
    fun `RGB to Grayscale`() {
        assertEquals(
            "ff0036b612",
            ("ffffff" + "000000" + "ff0000" + "00ff00" + "0000ff")
                .convert(ColorSpace.Rgb, ColorSpace.Grayscale)
        )
    }

    @Test
    fun `RGB to RGB`() {
        assertEquals(
            "ffffff" + "000000" + "ff0000" + "00ff00" + "0000ff",
            ("ffffff" + "000000" + "ff0000" + "00ff00" + "0000ff")
                .convert(ColorSpace.Rgb, ColorSpace.Rgb)
        )
    }

    @Test
    fun `RGB to RGBA`() {
        assertEquals(
            "ffffffff" + "000000ff" + "ff0000ff" + "00ff00ff" + "0000ffff",
            ("ffffff" + "000000" + "ff0000" + "00ff00" + "0000ff")
                .convert(ColorSpace.Rgb, ColorSpace.Rgba)
        )
        assertEquals(
            "123456ff" + "789abcff",
            ("123456" + "789abc").convert(ColorSpace.Rgb, ColorSpace.Rgba)
        )
    }

    @Test
    fun `Grayscale to Grayscale`() {
        assertEquals("ff7f9adb88", "ff7f9adb88".convert(ColorSpace.Grayscale, ColorSpace.Grayscale))
    }

    @Test
    fun `Grayscale to RGB`() {
        assertEquals(
            "ffffff" + "7f7f7f" + "9a9a9a" + "dbdbdb" + "000000",
            "ff7f9adb00".convert(ColorSpace.Grayscale, ColorSpace.Rgb)
        )
    }

    @Test
    fun `Grayscale to RGBA`() {
        assertEquals(
            "ffffffff" + "7f7f7fff" + "9a9a9aff" + "dbdbdbff" + "000000ff",
            "ff7f9adb00".convert(ColorSpace.Grayscale, ColorSpace.Rgba)
        )
    }

    private fun String.convert(fromSpace: ColorSpace, toSpace: ColorSpace): String =
        ByteArrayOutputStream().also {
            fromSpace.convert(ByteArrayInputStream(hexStringToByteArray()), it, toSpace)
        }.toByteArray().toHexString()

    private fun String.hexStringToByteArray() =
        ByteArray(length / 2).also { array ->
            for (i in indices step 2) {
                array[i / 2] = ((Character.digit(get(i), 16) shl 4) + Character.digit(get(i + 1), 16)).toByte()
            }
        }
}

package util

import com.hp.jipp.pdl.ColorSpace
import com.hp.jipp.pdl.RenderableDocument
import com.hp.jipp.pdl.RenderablePage
import java.util.Arrays
import java.util.Random

class RandomDocument(
    seed: Long,
    pages: Int,
    private val widthPoints: Double,
    private val heightPoints: Double,
    override val dpi: Int
) : RenderableDocument {

    private val widthPixels = (widthPoints * dpi / 72).toInt()
    private val heightPixels = (widthPoints * dpi / 72).toInt()

    private val pages: List<RenderablePage> = (0 until pages).map { Page(seed + it, widthPixels, heightPixels) }

    override fun iterator(): Iterator<RenderablePage> = pages.iterator()

    class Page(seed: Long, widthPixels: Int, heightPixels: Int) : RenderablePage(widthPixels, heightPixels) {
        private val random = Random(seed)

        override fun render(yOffset: Int, swathHeight: Int, colorSpace: ColorSpace, byteArray: ByteArray) {
            if (random.nextInt() % 4 == 0) {
                Arrays.fill(byteArray, 0xFF.toByte())
            } else {
                random.nextBytes(byteArray)
            }
        }
    }
}

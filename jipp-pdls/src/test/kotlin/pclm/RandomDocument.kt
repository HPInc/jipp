package pclm

import com.hp.jipp.doc.ColorSpace
import com.hp.jipp.doc.RenderableDocument
import com.hp.jipp.doc.RenderablePage
import java.util.Arrays
import java.util.Random

class RandomDocument(
    seed: Long,
    pages: Int,
    private val widthPoints: Double,
    private val heightPoints: Double
) : RenderableDocument {

    private val pages: List<RenderablePage> = (0 until pages).map { Page(seed + it, widthPoints, heightPoints) }

    override fun iterator(): Iterator<RenderablePage> = pages.iterator()

    class Page(seed: Long, override val widthPoints: Double, override val heightPoints: Double) : RenderablePage() {
        private val random = Random(seed)

        override fun render(dpi: Int, yOffset: Int, swathHeight: Int, colorSpace: ColorSpace, byteArray: ByteArray) {
            if (random.nextInt() % 4 == 0) {
                Arrays.fill(byteArray, 0xFF.toByte())
            }
            random.nextBytes(byteArray)
        }
    }
}
package util

import com.hp.jipp.pdl.ColorSpace
import com.hp.jipp.pdl.RenderableDocument
import com.hp.jipp.pdl.RenderablePage
import com.hp.jipp.pdl.RotationUnit
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
        override fun rotate(amount: RotationUnit) = TODO()

        private val random = Random(seed)

        override fun render(dpi: Int, yOffset: Int, swathHeight: Int, colorSpace: ColorSpace, byteArray: ByteArray) {
            if (random.nextInt() % 4 == 0) {
                Arrays.fill(byteArray, 0xFF.toByte())
            } else {
                random.nextBytes(byteArray)
            }
        }
    }
}
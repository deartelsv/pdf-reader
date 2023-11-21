package de.artelsv.pdfreader.renderer

import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import de.artelsv.pdfreader.utils.PdfPageQuality
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PdfPageRenderer(
    private val pdfRenderer: PdfRenderer,
    private val quality: PdfPageQuality
) {
    fun getPageCount() = pdfRenderer.pageCount

    fun render(position: Int, onPageRendered: (Bitmap) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            runCatching {
                renderPage(position)
            }.onSuccess { page ->
                withContext(Dispatchers.Main) {
                    onPageRendered(page)
                }
            }
        }
    }

    private fun renderPage(position: Int): Bitmap {
        //Current opened pdf page
        val page = pdfRenderer.openPage(position)

        val width = quality.value
        val height = quality.value * page.height / page.width
        //Create a bitmap with pdf page dimensions
        val bitmap = Bitmap.createBitmap(
            width,
            height,
            Bitmap.Config.ARGB_8888
        )

        //Render the page onto the Bitmap.
        page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)

        //Close pdf page
        page.close()
        return bitmap
    }
}
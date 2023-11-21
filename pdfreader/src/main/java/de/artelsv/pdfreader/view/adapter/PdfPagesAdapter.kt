package de.artelsv.pdfreader.view.adapter

import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import de.artelsv.pdfreader.renderer.PdfPageRenderer
import de.artelsv.pdfreader.utils.PdfPageQuality

abstract class PdfPagesAdapter<T : PdfPageViewHolder>(
    private val pdfRenderer: PdfRenderer,
    private val quality: PdfPageQuality
) : ListAdapter<Bitmap, T>(DiffCallback()) {

    private val pdfPageRenderer: PdfPageRenderer by lazy { PdfPageRenderer(pdfRenderer, quality) }

    fun renderPage(position: Int, onPageRendered: (Bitmap) -> Unit) {
        pdfPageRenderer.render(position, onPageRendered)
    }

    override fun getItemCount() = pdfPageRenderer.getPageCount()

    class DiffCallback : DiffUtil.ItemCallback<Bitmap>() {
        override fun areItemsTheSame(oldItem: Bitmap, newItem: Bitmap) = oldItem === newItem

        override fun areContentsTheSame(oldItem: Bitmap, newItem: Bitmap) =
            oldItem.hashCode() == newItem.hashCode()
    }
}
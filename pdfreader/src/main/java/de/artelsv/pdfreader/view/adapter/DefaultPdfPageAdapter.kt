package de.artelsv.pdfreader.view.adapter

import android.graphics.pdf.PdfRenderer
import android.view.LayoutInflater
import android.view.ViewGroup
import de.artelsv.pdfreader.databinding.PdfPageBinding
import de.artelsv.pdfreader.utils.PdfPageQuality

class DefaultPdfPageAdapter(
    pdfRenderer: PdfRenderer,
    quality: PdfPageQuality
) : PdfPagesAdapter<DefaultPdfPageViewHolder>(pdfRenderer, quality) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = DefaultPdfPageViewHolder(
        PdfPageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    override fun onBindViewHolder(holder: DefaultPdfPageViewHolder, position: Int) {
        renderPage(position) {
            holder.bind(it)
        }
    }
}

package de.artelsv.pdfreader.view.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import de.artelsv.pdfreader.databinding.PdfPageBinding
import de.artelsv.pdfreader.utils.PdfPageQuality
import java.io.File

class DefaultPdfPageAdapter(
    file: File,
    quality: PdfPageQuality
) : PdfPagesAdapter<DefaultPdfPageViewHolder>(file, quality) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = DefaultPdfPageViewHolder(
        PdfPageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    override fun onBindViewHolder(holder: DefaultPdfPageViewHolder, position: Int) {
        renderPage(position) {
            holder.bind(it)
        }
    }
}

package de.artelsv.pdfreader.view.adapter

import android.graphics.Bitmap
import de.artelsv.pdfreader.databinding.PdfPageBinding

class DefaultPdfPageViewHolder(
    private val binding: PdfPageBinding
) : PdfPageViewHolder(binding.root) {

    override fun bind(page: Bitmap) = with(binding) {
        image.layoutParams.height = page.height * image.width / page.width
        image.setImageBitmap(page)
    }
}

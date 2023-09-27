package de.artelsv.pdfreader.view.adapter

import android.os.Build
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import de.artelsv.pdfreader.utils.PdfPageQuality
import de.artelsv.pdfreader.databinding.PdfPageBinding
import java.io.File

@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
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

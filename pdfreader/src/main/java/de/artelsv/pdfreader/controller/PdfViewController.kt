package de.artelsv.pdfreader.controller

import android.view.View
import de.artelsv.pdfreader.utils.PdfPageQuality
import java.io.File

interface PdfViewController {

    fun getView(): View

    fun setup(file: File)

    fun setZoomEnabled(isZoomEnabled: Boolean)

    fun setZoomFixed(value: Boolean)

    fun setMaxZoom(value: Float)
    fun setMinZoom(value: Float)

    fun setQuality(quality: PdfPageQuality)

    fun setOnPageChangedListener(onPageChangedListener: (page: Int, total: Int) -> Unit)

    fun goToPosition(position: Int)
}
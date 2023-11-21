package de.artelsv.pdfreader.controller

import android.content.Context
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import android.util.Log
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import de.artelsv.pdfreader.utils.PdfPageQuality
import de.artelsv.pdfreader.view.ZoomableRecyclerView
import de.artelsv.pdfreader.view.adapter.DefaultPdfPageAdapter
import java.io.File
import java.io.IOException

class PdfViewControllerImpl(
    context: Context
) : PdfViewController {

    private var view: ZoomableRecyclerView = ZoomableRecyclerView(context)
    private var pageQuality: PdfPageQuality = PdfPageQuality.QUALITY_1080
    private var onPageChangedListener: (page: Int, total: Int) -> Unit = { _, _ -> }
    private var lastVisiblePosition = -1

    override fun setup(file: File, onError: (e: Throwable) -> Unit) {
        file.deleteOnExit()
        val fileDescriptor = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)

        try {
            view.adapter = DefaultPdfPageAdapter(PdfRenderer(fileDescriptor), pageQuality)
        } catch (e: IOException) {
            Log.e(javaClass.simpleName, e.message.toString())
            onError(e)
        } catch (e: SecurityException) {
            Log.e(javaClass.simpleName, e.message.toString())
            onError(e)
        }
    }

    override fun setZoomEnabled(isZoomEnabled: Boolean) {
        view.isZoomEnabled = isZoomEnabled
        view.addOnScrollListener(onScrollListener)
    }

    override fun setZoomFixed(value: Boolean) {
        view.isFixedZoom = value
    }

    override fun setMaxZoom(value: Float) {
        view.maxZoom = value
    }

    override fun setMinZoom(value: Float) {
        view.minZoom = value
    }

    override fun setQuality(quality: PdfPageQuality) {
        this.pageQuality = quality
    }

    override fun setOnPageChangedListener(onPageChangedListener: (page: Int, total: Int) -> Unit) {
        this.onPageChangedListener = onPageChangedListener
    }

    override fun goToPosition(position: Int) {
        view.adapter?.run {
            if (position in 0 until itemCount) {
                view.smoothScrollToPosition(position)
            }
        }
    }

    override fun getView(): View = view

    private val onScrollListener = object : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
            val position =
                (recyclerView.layoutManager as LinearLayoutManager).findLastCompletelyVisibleItemPosition()
            if (position != lastVisiblePosition && position != -1) {
                lastVisiblePosition = position
                onPageChangedListener(
                    lastVisiblePosition + 1,
                    recyclerView.adapter?.itemCount ?: 0
                )
            }
        }
    }
}
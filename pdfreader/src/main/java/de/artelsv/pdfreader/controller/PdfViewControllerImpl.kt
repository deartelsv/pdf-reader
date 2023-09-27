package de.artelsv.pdfreader.controller

import android.content.Context
import android.os.Build
import android.view.View
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import de.artelsv.pdfreader.view.adapter.DefaultPdfPageAdapter
import de.artelsv.pdfreader.utils.PdfPageQuality
import de.artelsv.pdfreader.view.ZoomableRecyclerView
import java.io.File

@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
class PdfViewControllerImpl(
    context: Context
) : PdfViewController {

    private var view: ZoomableRecyclerView = ZoomableRecyclerView(context)
    private var pageQuality: PdfPageQuality = PdfPageQuality.QUALITY_1080
    private var onPageChangedListener: (page: Int, total: Int) -> Unit = { _, _ -> }
    private var lastVisiblePosition = -1

    override fun setup(file: File) {
        file.deleteOnExit()
        view.adapter = DefaultPdfPageAdapter(file, pageQuality)
    }

    override fun setZoomEnabled(isZoomEnabled: Boolean) {
        view.isZoomEnabled = isZoomEnabled
        view.addOnScrollListener(onScrollListener)
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
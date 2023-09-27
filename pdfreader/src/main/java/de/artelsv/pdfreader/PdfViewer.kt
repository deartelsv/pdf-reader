package de.artelsv.pdfreader

import android.content.Context
import android.os.Build
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import de.artelsv.pdfreader.controller.PdfViewController
import de.artelsv.pdfreader.controller.PdfViewControllerImpl
import de.artelsv.pdfreader.decoder.FileLoader
import de.artelsv.pdfreader.errors.Error
import de.artelsv.pdfreader.utils.PdfPageQuality
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.IOException

@RequiresApi(Build.VERSION_CODES.FROYO)
class PdfViewer private constructor(
    pdfViewController: PdfViewController,
    rootView: ViewGroup,
    private val errorListener: (error: Error) -> Unit
) : PdfViewController by pdfViewController {

    private val context: Context by lazy { rootView.context }

    private val fileLoader by lazy { FileLoader() }

    init {
        try {
            rootView.addView(getView())
        } catch (e: IOException) {
            errorListener(Error.AttachViewError(e))
        }
    }

    private fun display(file: File) {
        try {
            setup(file)
        } catch (e: IOException) {
            errorListener(Error.PdfRendererError(e))
        } catch (e: Exception) {
            errorListener(Error.AttachViewError(e))
        }
    }

    fun load(file: File) {
        display(file)
    }

    fun load(url: String) {
        CoroutineScope(Dispatchers.Main).launch {
            runCatching {
                fileLoader.loadFile(context, url)
            }.onFailure {
                errorListener(Error.FileLoadError(it))
            }.onSuccess {
                display(it)
            }
        }
    }

    companion object {

        @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
        fun create(
            rootView: ViewGroup,
            pdfViewController: PdfViewController = PdfViewControllerImpl(rootView.context),
            quality: PdfPageQuality = PdfPageQuality.QUALITY_1080,
            maxZoom: Float = 3f,
            minZoom: Float = 1f,
            isZoomEnabled: Boolean = true,
            onPageChangedListener: (page: Int, total: Int) -> Unit,
            onErrorListener: (error: Error) -> Unit
        ): PdfViewer {
            val pdfViewer = PdfViewer(
                pdfViewController = pdfViewController,
                rootView = rootView,
                errorListener = onErrorListener
            )
            pdfViewController.setQuality(quality)
            pdfViewController.setZoomEnabled(isZoomEnabled)
            pdfViewController.setMaxZoom(maxZoom)
            pdfViewController.setMinZoom(minZoom)
            pdfViewController.setOnPageChangedListener(onPageChangedListener)
            return pdfViewer
        }
    }
}
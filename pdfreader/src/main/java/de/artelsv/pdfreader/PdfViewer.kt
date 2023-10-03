package de.artelsv.pdfreader

import android.content.Context
import android.view.ViewGroup
import de.artelsv.pdfreader.controller.Header
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

class PdfViewer private constructor(
    pdfViewController: PdfViewController,
    rootView: ViewGroup,
    private val headers: List<Header>,
    private val isCacheEnabled: Boolean,
    private val errorListener: (error: Error) -> Unit,
    private val startListener: () -> Unit,
    private val completeListener: () -> Unit
) : PdfViewController by pdfViewController {

    private val context: Context by lazy { rootView.context }

    private val fileLoader by lazy { FileLoader(headers) }

    private val cacheFiles = hashMapOf<String, File>()

    init {
        try {
            rootView.addView(getView())
        } catch (e: IOException) {
            errorListener(Error.AttachViewError(e))
        }
    }

    private fun display(file: File) {
        try {
            CoroutineScope(Dispatchers.Main).launch {
                setup(file)
            }
        } catch (e: IOException) {
            errorListener(Error.PdfRendererError(e))
        } catch (e: Exception) {
            errorListener(Error.AttachViewError(e))
        }
        CoroutineScope(Dispatchers.Main).launch {
            completeListener()
        }
    }

    fun load(file: File) {
        display(file)
    }

    fun load(url: String) {
        startListener()
        if (isCacheEnabled) {
            cacheFiles[url]?.let {
                display(it)
            } ?: fileLoader.loadFile(
                context = context,
                url = url,
                onFileDownloaded = {
                    cacheFiles[url] = it
                    display(it)
                },
                onError = {
                    errorListener(it)
                }
            )
        } else {
            fileLoader.loadFile(
                context = context,
                url = url,
                onFileDownloaded = {
                    display(it)
                },
                onError = {
                    errorListener(it)
                }
            )
        }
    }

    companion object {

        fun create(
            rootView: ViewGroup,
            pdfViewController: PdfViewController = PdfViewControllerImpl(rootView.context),
            quality: PdfPageQuality = PdfPageQuality.QUALITY_1080,
            maxZoom: Float = 3f,
            minZoom: Float = 1f,
            isZoomEnabled: Boolean = true,
            isFixedZoom: Boolean = false,
            headers: List<Header> = emptyList(),
            isCacheEnabled: Boolean = false,
            onPageChangedListener: (page: Int, total: Int) -> Unit = { _, _ -> },
            onErrorListener: (error: Error) -> Unit = { },
            onStartListener: () -> Unit = { },
            onCompletedListener: () -> Unit = { }
        ): PdfViewer {
            val pdfViewer = PdfViewer(
                pdfViewController = pdfViewController,
                rootView = rootView,
                headers = headers,
                isCacheEnabled = isCacheEnabled,
                errorListener = onErrorListener,
                startListener = onStartListener,
                completeListener = onCompletedListener
            )
            pdfViewController.setQuality(quality)
            pdfViewController.setZoomEnabled(isZoomEnabled)
            pdfViewController.setZoomFixed(isFixedZoom)
            pdfViewController.setMaxZoom(maxZoom)
            pdfViewController.setMinZoom(minZoom)
            pdfViewController.setOnPageChangedListener(onPageChangedListener)
            return pdfViewer
        }
    }
}
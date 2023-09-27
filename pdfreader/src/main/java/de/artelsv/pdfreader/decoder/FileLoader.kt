package de.artelsv.pdfreader.decoder

import android.content.Context
import de.artelsv.pdfreader.controller.Header
import de.artelsv.pdfreader.controller.addHeader
import de.artelsv.pdfreader.errors.Error
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.apache.commons.io.FileUtils
import java.io.File
import java.io.IOException

class FileLoader(private val headers: List<Header>) {

    private val tempFileName = "temp${System.currentTimeMillis()}.pdf"

    private fun getTempFile(context: Context): File = File(context.cacheDir, tempFileName)

    fun loadFile(
        context: Context,
        url: String,
        onFileDownloaded: (File) -> Unit,
        onError: (e: Error.FileLoadError) -> Unit
    ) {
        val mFile = getTempFile(context)
        val request = Request.Builder()
            .apply {
                headers.forEach { it.addHeader(this) }
            }
            .url(url)
            .build()
        val client = OkHttpClient.Builder().build()

        client.newCall(request).enqueue(
            object : Callback {
                override fun onResponse(call: Call, response: Response) {
                    response.body?.byteStream()?.let {
                        FileUtils.copyInputStreamToFile(it, mFile)
                        onFileDownloaded(mFile)
                    }
                }

                override fun onFailure(call: Call, e: IOException) {
                    onError(Error.FileLoadError(e))
                }
            }
        )
    }
}
package de.artelsv.pdfreader.decoder

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.net.URL

class FileLoader {

    private val tempFileName = "temp${System.currentTimeMillis()}.pdf"

    private fun getTempFile(context: Context): File = File(context.cacheDir, tempFileName)

    suspend fun loadFile(context: Context, url: String): File =
        withContext(Dispatchers.IO) {
            val imageUrl = URL(url)
            val urlConnection = imageUrl.openConnection()
            val input = urlConnection.getInputStream()
            doLoadFile(input = input, file = getTempFile(context))
        }

    private fun doLoadFile(input: InputStream, file: File): File {
        val output = FileOutputStream(file)
        val buffer = ByteArray(BUFFER_SIZE)
        var read: Int = input.read(buffer)

        while ((read) != -1) {
            output.write(buffer, 0, read)
            read = input.read(buffer)
        }

        output.flush()
        return file
    }

    companion object {
        private const val BUFFER_SIZE = 4 * 1024
    }
}
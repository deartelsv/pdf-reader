package de.artelsv.pdfreader.errors

sealed class Error(open val e: Throwable) : Exception() {
    data class FileLoadError(override val e: Throwable) : Error(e)
    data class AttachViewError(override val e: Throwable) : Error(e)
    data class PdfRendererError(override val e: Throwable) : Error(e)
}

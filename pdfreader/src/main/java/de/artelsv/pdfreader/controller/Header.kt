package de.artelsv.pdfreader.controller

import okhttp3.Request

sealed class Header(open val name: String, open val value: String) {
    data class AuthBearer(override val name: String, override val value: String) : Header(name, value)
}

fun Header.addHeader(builder: Request.Builder) = builder.addHeader(name, value)

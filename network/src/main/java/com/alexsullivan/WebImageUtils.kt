package com.alexsullivan

import okhttp3.OkHttpClient
import okhttp3.Request

fun isImage(url: String, client: OkHttpClient = defaultClient()): Boolean {
    val request = Request.Builder().url(url).head().build()
    val response = client.newCall(request).execute()
    val contentType = response.headers().get("Content-Type")
    return contentType == "image/gif"
}

fun String.massageGfycatLink(): String {
    val splits = this.split("https://")
    if (splits.size != 2) {
        throw RuntimeException("Received gyfcat malformed gyfcat link! " + this)
    }
    return "https://" + "giant." + splits[1] + ".gif"
}

private fun defaultClient(): OkHttpClient {
    return okhttp3.OkHttpClient.Builder().build()
}
package com.alexsullivan

import okhttp3.Call
import okhttp3.Request

internal fun isPlayingMedia(url: String, client: Call.Factory): Boolean {
    val contentType = getUrlContentType(url, client)
    return contentType == "image/gif" || contentType == "video/mp4"
}

internal fun isStaticImgae(url: String, client: Call.Factory): Boolean {
    val contentType = getUrlContentType(url, client)
    val split = contentType.split("/")
    return split[0] == "image" && split[1] != "gif"
}

private fun getUrlContentType(url: String, client: Call.Factory): String {
    val request = Request.Builder().url(url).head().build()
    val response = client.newCall(request).execute()
    return response.headers().get("Content-Type")
}
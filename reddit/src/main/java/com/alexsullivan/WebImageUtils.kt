package com.alexsullivan

import okhttp3.Call
import okhttp3.Request

internal fun isPlayingMedia(url: String, client: Call.Factory): Boolean {
    val request = Request.Builder().url(url).head().build()
    val response = client.newCall(request).execute()
    val contentType = response.headers().get("Content-Type")
    return contentType == "image/gif" || contentType == "video/mp4"
}
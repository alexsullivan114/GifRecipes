package com.alexsullivan

import okhttp3.OkHttpClient
import okhttp3.Request

internal fun isPlayingMedia(url: String, client: OkHttpClient): Boolean {
    val request = Request.Builder().url(url).head().build()
    val response = client.newCall(request).execute()
    val contentType = response.headers().get("Content-Type")
    return contentType == "image/gif" || contentType == "video/mp4"
}

internal fun String.massageGfycatLink(): String {
    if (!this.contains("gfycat")) {
        return this
    }

    val splits = this.split("https://")
    if (splits.size != 2) {
        return this
    }
    return "https://" + "thumbs." + splits[1] + "-size_restricted.gif"
}
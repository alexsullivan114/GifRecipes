package com.alexsullivan.reddit

import com.alexsullivan.isPlayingMedia
import com.alexsullivan.isStaticImgae
import com.alexsullivan.reddit.testutils.buildFakeCallFactory
import com.alexsullivan.reddit.testutils.buildFakeResponse
import okhttp3.Call
import okhttp3.Headers
import org.junit.Assert
import org.junit.Test

/**
 * Created by Alexs on 8/30/2017.
 */

class WebImageUtilsTests {

    @Test fun testIsPlayingMediaHappy() {
        val gifIsMedia = isPlayingMedia("https://www.google.com", buildCallFactory("image/gif"))
        Assert.assertTrue(gifIsMedia)
        val videoIsMedia = isPlayingMedia("https://www.google.com", buildCallFactory("video/mp4"))
        Assert.assertTrue(videoIsMedia)
    }

    @Test fun testIsPlayingMediaFalse() {
        val jpgIsMedia = isPlayingMedia("https://www.google.com", buildCallFactory("image/jpg"))
        Assert.assertFalse(jpgIsMedia)
        val textIsMedia = isPlayingMedia("https://www.google.com", buildCallFactory("text/json"))
        Assert.assertFalse(textIsMedia)
    }

    @Test fun testIsPlayingMediaMalformed() {
        val blank = isPlayingMedia("https://www.google.com", buildCallFactory(""))
        Assert.assertFalse(blank)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testMalformedExceptions() {
        val malformed = isPlayingMedia("", buildCallFactory(""))
        Assert.assertFalse(malformed)
    }

    @Test fun testIsStaticMediaTrue() {
        val blank = isStaticImgae("https://www.google.com", buildCallFactory("image/jpg"))
        Assert.assertTrue(blank)
    }

    @Test fun testIsStaticMediaFalse() {
        val blank = isStaticImgae("https://www.google.com", buildCallFactory("image/gif"))
        Assert.assertFalse(blank)
    }

    private fun buildCallFactory(contentType: String): Call.Factory {
        val headers = Headers.Builder().add("Content-Type", contentType).build()
        val response = buildFakeResponse(headers)
        return buildFakeCallFactory(response)
    }
}
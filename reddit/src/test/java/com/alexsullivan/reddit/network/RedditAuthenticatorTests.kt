package com.alexsullivan.reddit.network

import com.alexsullivan.reddit.testutils.buildFakeCallFactory
import com.alexsullivan.reddit.testutils.buildFakeResponse
import okhttp3.Headers
import okhttp3.Request
import okhttp3.ResponseBody
import org.junit.Assert
import org.junit.Test

/**
 * Created by Alexs on 8/30/2017.
 */
class RedditAuthenticatorTests {
    val cannedAuthResponse = """{
  "access_token": "JrlBMsUNGHRTAh8NsRN_JsuIJ-8",
  "token_type": "bearer",
  "device_id": "385ad0c4-31cc-11e7-93ae-92361f002671",
  "expires_in": 3600,
  "scope": "*"
}"""
    @Test fun testAuthenticateHappyPath() {
        val accessTokenConsumer = fun(token: String) {
            Assert.assertEquals("JrlBMsUNGHRTAh8NsRN_JsuIJ-8", token)
        }
        val mediaType = okhttp3.MediaType.parse("application/x-www-form-urlencoded")
        val body = ResponseBody.create(mediaType, cannedAuthResponse)
        val request = Request.Builder().url("https://www.google.com").build()
        val response = buildFakeResponse(Headers.of(), body).request(request)
        val callFactory = buildFakeCallFactory(response)
        val authenticator = RedditAuthenticator(accessTokenConsumer, {callFactory}, "blank")
        Assert.assertEquals("Bearer JrlBMsUNGHRTAh8NsRN_JsuIJ-8", authenticator.authenticate(null, response.build())?.header("Authorization"))
    }
}
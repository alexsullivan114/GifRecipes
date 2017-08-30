package com.alexsullivan.reddit.network

import com.alexsullivan.reddit.testutils.buildFakeChain
import okhttp3.Request
import org.junit.Assert
import org.junit.Test

/**
 * Created by Alexs on 8/30/2017.
 */

class RedditAuthInterceptorTests {

    @Test fun emptyAuthHappy() {
        val authTokenProvider = fun() = "TestAuth"
        val authInterceptor = RedditAuthInterceptor(authTokenProvider)
        val request = Request.Builder().url("https://www.google.com").build()
        val assertionBlock = fun(request: Request) {
            Assert.assertEquals("Bearer TestAuth", request.header("Authorization"))
        }
        val chain = buildFakeChain(request, assertionBlock)
        authInterceptor.intercept(chain)
    }

    @Test fun emptyAuthMultipleCallsHappy() {
        var authToken = "TestAuth"
        val authTokenProvider = fun() = authToken
        val authInterceptor = RedditAuthInterceptor(authTokenProvider)
        val request = Request.Builder().url("https://www.google.com").build()
        val assertionBlock = fun(request: Request) {
            Assert.assertEquals("Bearer TestAuth", request.header("Authorization"))
        }
        val initialChain = buildFakeChain(request, assertionBlock)
        // At this point, our request should have the right authorization. Let's make sure we don't unnecesarily change that.
        authInterceptor.intercept(initialChain)
        val authedRequest = Request.Builder().url("https://www.google.com").header("Authorization", "Bearer TestAuth").build()
        authToken = "TestAuth5"
        val authedChain = buildFakeChain(authedRequest, assertionBlock)
        authInterceptor.intercept(authedChain)
    }
}

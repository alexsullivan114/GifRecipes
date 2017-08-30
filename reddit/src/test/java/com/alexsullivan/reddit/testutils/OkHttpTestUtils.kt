package com.alexsullivan.reddit.testutils

import okhttp3.*

/**
 * Created by Alexs on 8/30/2017.
 */

fun buildFakeCallFactory(response: Response.Builder): Call.Factory {
    val call = object: Call {

        var originalRequest: Request? = null

        override fun enqueue(responseCallback: Callback?) {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun isExecuted(): Boolean {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun clone(): Call {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun isCanceled(): Boolean {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun cancel() {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun request() = originalRequest

        override fun execute() = response.request(originalRequest).build()
    }

    return Call.Factory { request ->
        call.originalRequest = request
        call
    }
}

fun buildFakeResponse(headers: Headers, body: ResponseBody? = null): Response.Builder {
    return Response.Builder()
            .headers(headers)
            .code(200)
            .body(body)
            .protocol(Protocol.HTTP_2)
}

fun buildFakeChain(request: Request, assertionBlock: (Request) -> Unit): Interceptor.Chain {
    return object: Interceptor.Chain {
        override fun proceed(request: Request): Response {
            assertionBlock(request)
            return buildFakeResponse(Headers.of()).request(request).build()
        }

        override fun connection(): Connection {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun request() = request
    }
}
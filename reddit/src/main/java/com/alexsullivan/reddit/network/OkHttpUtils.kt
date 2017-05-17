package com.alexsullivan.reddit.network

import okhttp3.Response

/**
 * Created by Alexs on 5/9/2017.
 */

fun Response.count(): Int {
    var result = 1
    var updatedResponse = this.priorResponse()
    while (updatedResponse != null) {
        result++
        updatedResponse = updatedResponse.priorResponse()
    }
    return result
}
package com

import okhttp3.Response

internal fun Response.count(): Int {
    var result = 1
    var updatedResponse = this.priorResponse()
    while (updatedResponse != null) {
        result++
        updatedResponse = updatedResponse.priorResponse()
    }
    return result
}


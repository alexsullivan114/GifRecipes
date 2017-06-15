package com.gfycat

import io.reactivex.Observable
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface GfycatService {
    object statics {
        val baseUrl = "https://api.gfycat.com/v1/gfycats/"
        const val clientId = "2_ntJYHc"
    }

    @GET("{id}")
    fun getImage(@Path(value="id", encoded = true) gfycatId: String): Observable<Response<GfycatPost>>
}
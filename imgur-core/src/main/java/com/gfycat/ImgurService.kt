package com.gfycat

import com.gfycat.ImgurService.statics.clientId
import io.reactivex.Observable
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Path

interface ImgurService {
    object statics {
        val baseUrl = "https://api.imgur.com/3/"
        const val clientId = "2678d1463e767db"
    }

    @Headers("Authorization: Client-ID $clientId")
    @GET("image/{id}")
    fun getImage(@Path(value = "id", encoded = true) imageId: String): Observable<Response<ImgurPost>>
}
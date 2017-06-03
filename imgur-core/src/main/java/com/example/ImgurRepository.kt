package com.example

import com.google.gson.Gson
import com.google.gson.JsonObject
import io.reactivex.Observable
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

class ImgurRepository(val service: ImgurService) {

    companion object Factory {
        fun create(): ImgurRepository {
            val client = OkHttpClient()
            val service = Retrofit.Builder().client(client)
                    .baseUrl(ImgurService.statics.baseUrl)
                    .addConverterFactory(GsonConverterFactory.create(Gson()))
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .build().create(ImgurService::class.java)
            return ImgurRepository(service)
        }
    }

    fun getImageInfo(imgurUrl: String): Observable<JsonObject> = service.getImage(imgurUrl)
}
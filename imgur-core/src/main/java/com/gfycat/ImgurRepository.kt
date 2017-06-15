package com.gfycat

import com.google.gson.GsonBuilder
import io.reactivex.Observable
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

class ImgurRepository(private val service: ImgurService) {

    companion object Factory {
        fun create(): ImgurRepository {
            val client = OkHttpClient()
            val gson = GsonBuilder().registerTypeAdapter(ImgurPost::class.java, ImgurPostDeserializer()).create()
            val service = Retrofit.Builder().client(client)
                    .baseUrl(ImgurService.statics.baseUrl)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .build().create(ImgurService::class.java)
            return ImgurRepository(service)
        }
    }

    fun getImageInfo(imageId: String): Observable<ImgurPost> = service.getImage(imageId)
            .flatMap {
                if (it.isSuccessful) {
                    Observable.just(it.body())
                } else {
                    Observable.empty()
                }
            }
}
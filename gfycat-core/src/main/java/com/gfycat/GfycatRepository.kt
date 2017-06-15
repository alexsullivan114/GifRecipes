package com.gfycat

import com.google.gson.GsonBuilder
import io.reactivex.Observable
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

class GfycatRepository(private val service: GfycatService) {
    companion object Factory {
        fun create(): GfycatRepository {
            val client = WebClient().client
            val gson = GsonBuilder().registerTypeAdapter(GfycatPost::class.java, GfycatPostDeserializer()).create()
            val service = Retrofit.Builder().client(client).baseUrl(GfycatService.statics.baseUrl).addConverterFactory(GsonConverterFactory.create(gson)).addCallAdapterFactory(RxJava2CallAdapterFactory.create()).build().create(GfycatService::class.java)
            return GfycatRepository(service)
        }
    }

    fun getImageInfo(imageId: String): Observable<GfycatPost> = service.getImage(imageId).flatMap {
        if (it.isSuccessful) {
            Observable.just(it.body())
        } else {
            Observable.empty()
        }
    }
}
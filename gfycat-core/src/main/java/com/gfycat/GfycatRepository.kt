package com.gfycat

import com.alexsullivan.logging.Logger
import com.google.gson.GsonBuilder
import io.reactivex.Observable
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

class GfycatRepository(private val service: GfycatService, private val logger: Logger) {

    private val TAG: String = javaClass.simpleName

    companion object Factory {
        fun create(logger: Logger): GfycatRepository {
            val client = WebClient.okHttpClient
            val gson = GsonBuilder().registerTypeAdapter(GfycatPost::class.java, GfycatPostDeserializer()).create()
            val service = Retrofit.Builder().client(client).baseUrl(GfycatService.statics.baseUrl).addConverterFactory(GsonConverterFactory.create(gson)).addCallAdapterFactory(RxJava2CallAdapterFactory.create()).build().create(GfycatService::class.java)
            return GfycatRepository(service, logger)
        }
    }

    fun getImageInfo(imageId: String): Observable<GfycatPost> {
        var startTime = 0L
        return service.getImage(imageId).flatMap {
            if (it.isSuccessful) {
                Observable.just(it.body())
            } else {
                Observable.empty()
            }
        }.doOnNext {
            val elapsedTime = System.currentTimeMillis() - startTime
            logger.d(TAG, "Fetching gfycat image info took $elapsedTime milliseconds")
        }.doOnSubscribe { startTime = System.currentTimeMillis() }
    }
}
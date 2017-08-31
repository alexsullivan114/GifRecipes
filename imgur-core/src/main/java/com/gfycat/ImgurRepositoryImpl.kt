package com.gfycat

import com.alexsullivan.logging.Logger
import com.google.gson.GsonBuilder
import io.reactivex.Observable
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

class ImgurRepositoryImpl(private val service: ImgurService,
                          private val logger: Logger): ImgurRepository {

    private val TAG: String = javaClass.simpleName

    companion object Factory {
        val client = OkHttpClient()
        fun create(logger: Logger): ImgurRepositoryImpl {
            val gson = GsonBuilder().registerTypeAdapter(ImgurPost::class.java, ImgurPostDeserializer()).create()
            val service = Retrofit.Builder().client(client)
                    .baseUrl(ImgurService.statics.baseUrl)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .build().create(ImgurService::class.java)
            return ImgurRepositoryImpl(service, logger)
        }
    }

    override fun getImageInfo(imageId: String): Observable<ImgurPost> {
        var startTime = 0L
        return service.getImage(imageId).flatMap {
                    if (it.isSuccessful) {
                        Observable.just(it.body())
                    } else {
                        Observable.empty()
                    }
                }
                .doOnNext {
                    val elapsedTime = System.currentTimeMillis() - startTime
                    logger.d(TAG, "Fetching imgur image info took $elapsedTime milliseconds")
                }
                .doOnSubscribe { startTime = System.currentTimeMillis() }
    }
}
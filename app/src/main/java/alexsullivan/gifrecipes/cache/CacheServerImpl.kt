package alexsullivan.gifrecipes.cache

import alexsullivan.gifrecipes.application.AndroidLogger
import android.content.Context
import com.danikula.videocache.CacheListener
import com.danikula.videocache.HttpProxyCacheServer
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.functions.Consumer
import io.reactivex.internal.functions.Functions
import io.reactivex.schedulers.Schedulers
import java.io.IOException
import java.net.URL

object CacheServerImpl: CacheServer {

    private lateinit var cacheServer: HttpProxyCacheServer

    fun initialize(context: Context) {
        cacheServer = HttpProxyCacheServer(context)
    }

    fun instance() = this

    override fun get(url: String) = cacheServer.getProxyUrl(url)

    override fun isCached(url: String) = cacheServer.isCached(url)

    override fun cacheProgress(url: String): Observable<Int> {
        return Observable.create {
            if (cacheServer.isCached(url)) {
                it.onComplete()
            } else {
                val listener = CacheListener { _, _, percentsAvailable ->
                    it.onNext(percentsAvailable)
                    if (percentsAvailable == 100) {
                        it.onComplete()
                    }
                }
                cacheServer.registerCacheListener(listener, url)
                it.setCancellable({ cacheServer.unregisterCacheListener(listener, url) })
            }
        }
    }

    override fun prefetch(url: String): Observable<Int> {
        if (cacheServer.isCached(url)) {
            return Observable.empty()
        } else {
            // Now return the observable that
            return Observable.create { observableEmitter ->
                // Begin downloading our cached file.
                download(cacheServer.getProxyUrl(url))
                        .subscribeOn(Schedulers.io())
                        .retry({ count, throwable ->
                            if (count > 2){
                                false
                            } else {
                                throwable is IOException
                            }
                        })
                        .subscribe(Functions.EMPTY_ACTION, Consumer<Throwable> { t -> observableEmitter.onError(t) })

                val listener = CacheListener { _, _, percentsAvailable ->
                    observableEmitter.onNext(percentsAvailable)
                    if (percentsAvailable == 100) {
                        observableEmitter.onComplete()
                    }
                }
                cacheServer.registerCacheListener(listener, url)
                observableEmitter.setCancellable({ cacheServer.unregisterCacheListener(listener, url) })
            }
        }
    }

    private fun download(url: String): Completable {
        return Completable.fromAction {
            val cacheUrl = URL(cacheServer.getProxyUrl(url))
            val inputStream = cacheUrl.openStream()
            val bufferSize = 4096
            val buffer = ByteArray(bufferSize)
            var length = inputStream.read(buffer)
            var totalSize = length
            var startTime = System.currentTimeMillis()
            while (length != -1) {
                length = inputStream.read(buffer)
                totalSize += length
//                Since we just need to kick start the prefetching, dont need to do anything here
            }
            var endTime = System.currentTimeMillis()
            AndroidLogger.d("Downloading", "Total size: ${totalSize/1000000}, total time: ${endTime - startTime}")
        }
    }
}
package alexsullivan.gifrecipes.cache

import android.content.Context
import android.util.Log
import com.danikula.videocache.CacheListener
import com.danikula.videocache.HttpProxyCacheServer
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import java.net.URL

object CacheServerImpl: CacheServer {

    private lateinit var cacheServer: HttpProxyCacheServer

    fun initialize(context: Context) {
        cacheServer = HttpProxyCacheServer(context)
    }

    fun instance(): CacheServerImpl {
        return this
    }

    override fun get(url: String): String {
        if (!cacheServer.isCached(url)) {
            cacheServer.registerCacheListener({cacheFile, cachedUrl, percentsAvailable ->
                Log.d("CacheServer", "$cacheFile + $cachedUrl + $percentsAvailable")
            }, url)
        }
        return cacheServer.getProxyUrl(url)
    }

    override fun isCached(url: String): Boolean {
        return cacheServer.isCached(url)
    }

    override fun cacheProgress(url: String): Observable<Int> {
        return Observable.create {
            if (cacheServer.isCached(url)) {
                it.onComplete()
            } else {
                val listener = CacheListener { _, _, percentsAvailable ->
                    it.onNext(percentsAvailable)
                    if (percentsAvailable == 100) {
                        it.onComplete()
                        Log.d("Cache", "progress: $percentsAvailable")
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
            return Observable.create {
                // Begin downloading our cached file.
                download(cacheServer.getProxyUrl(url))
                        .subscribeOn(Schedulers.io())
                        .subscribe()

                val listener = CacheListener { _, _, percentsAvailable ->
                    it.onNext(percentsAvailable)
                    if (percentsAvailable == 100) {
                        it.onComplete()
                        Log.d("Cache", "progress: $percentsAvailable")
                    }
                }
                cacheServer.registerCacheListener(listener, url)
                it.setCancellable({ cacheServer.unregisterCacheListener(listener, url) })
            }
        }
    }

    private fun download(url: String): Completable {
        return Completable.fromAction {
            val cacheUrl = URL(cacheServer.getProxyUrl(url))
            val inputStream = cacheUrl.openStream()
            val bufferSize = 1024
            val buffer = ByteArray(bufferSize)
            var length = inputStream.read(buffer)
            while (length != -1) {
                length = inputStream.read(buffer)
                //Since we just need to kick start the prefetching, dont need to do anything here
            }
        }
    }
}
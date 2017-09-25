package alexsullivan.testutils

import alexsullivan.gifrecipes.cache.CacheServer
import io.reactivex.Observable

abstract class CacheServerAdapter: CacheServer {
    override fun get(url: String) = url
    override fun prefetch(url: String): Observable<Int> = Observable.empty()
    override fun isCached(url: String) = false
    override fun cacheProgress(url: String): Observable<Int> = Observable.empty()
}
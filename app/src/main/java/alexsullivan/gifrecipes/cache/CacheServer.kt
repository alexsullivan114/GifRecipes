package alexsullivan.gifrecipes.cache

import io.reactivex.Observable

interface CacheServer {
    fun get(url: String): String
    fun prefetch(url: String): Observable<Int>
    fun isCached(url: String): Boolean
    fun cacheProgress(url: String): Observable<Int>
}
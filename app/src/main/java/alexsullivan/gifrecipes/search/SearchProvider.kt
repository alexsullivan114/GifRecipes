package alexsullivan.gifrecipes.search

import io.reactivex.Observable

interface SearchProvider {
    fun getObservableSource(): Observable<String>
}
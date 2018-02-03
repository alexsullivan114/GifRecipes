package alexsullivan.gifrecipes.utils

import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import java.io.IOException
import java.util.concurrent.TimeUnit

fun Disposable.addTo(disposables: CompositeDisposable) {
    disposables.add(this)
}

fun <T> Observable<T>.exponentialBackoff(maxTries: Int, scheduler: Scheduler): Observable<T> {
    var retryCount = 0

    return retryWhen { errors ->
        errors.flatMap {
            if (retryCount >= maxTries) {
                Observable.error(it)
            }
            else if (it is IOException) {
                retryCount++
                Observable.just(1).delay(Math.pow(3.toDouble(), retryCount.toDouble()).toLong(), TimeUnit.SECONDS, scheduler)
            } else {
                Observable.error(it)
            }
        }
    }
}
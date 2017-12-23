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

//public class RetryWithDelay implements Function<Observable<? extends Throwable>, Observable<?>> {
//    private final int maxRetries;
//    private final int retryDelayMillis;
//    private int retryCount;
//
//    public RetryWithDelay(final int maxRetries, final int retryDelayMillis) {
//        this.maxRetries = maxRetries;
//        this.retryDelayMillis = retryDelayMillis;
//        this.retryCount = 0;
//    }
//
//    @Override
//    public Observable<?> apply(final Observable<? extends Throwable> attempts) {
//        return attempts
//            .flatMap(new Function<Throwable, Observable<?>>() {
//                @Override
//                public Observable<?> apply(final Throwable throwable) {
//                if (++retryCount < maxRetries) {
//                    // When this Observable calls onNext, the original
//                    // Observable will be retried (i.e. re-subscribed).
//                    return Observable.timer(retryDelayMillis,
//                        TimeUnit.MILLISECONDS);
//                }
//
//                // Max retries hit. Just pass the error along.
//                return Observable.error(throwable);
//            }
//            });
//    }
//}
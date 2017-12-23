package alexsullivan.utils

import alexsullivan.gifrecipes.utils.exponentialBackoff
import io.reactivex.Observable
import io.reactivex.schedulers.TestScheduler
import org.junit.Assert
import org.junit.Test
import java.io.IOException
import java.util.concurrent.TimeUnit

class ExponentialBackoffTests {
    @Test
    fun testNetworkError() {
        val error = IOException()
        val scheduler = TestScheduler()
        val observable = Observable.create<Int> {
            it.onNext(1)
            it.onNext(2)
            it.onNext(3)
            it.onError(error)
        }

        val test = observable.exponentialBackoff(3, scheduler).test()
        scheduler.advanceTimeBy(3, TimeUnit.SECONDS)
        scheduler.advanceTimeBy(9, TimeUnit.SECONDS)
        scheduler.advanceTimeBy(27, TimeUnit.SECONDS)
        Assert.assertTrue(test.awaitTerminalEvent())
        test.assertError(error)
    }

    @Test
    fun testNonNetworkError() {
        val error = RuntimeException()
        val scheduler = TestScheduler()
        val observable = Observable.create<Int> {
            it.onNext(1)
            it.onNext(2)
            it.onNext(3)
            it.onError(error)
        }

        val test = observable.exponentialBackoff(3, scheduler).test()
        Assert.assertTrue(test.awaitTerminalEvent())
        test.assertError(error)
    }
}
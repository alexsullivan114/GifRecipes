package alexsullivan.gifrecipes.application

import alexsullivan.gifrecipes.cache.CacheServerImpl
import android.app.Application
import android.util.Log
import com.alexsullivan.ApplicationInitialization.CoreInitializer
import com.alexsullivan.logging.Logger
import com.alexsullivan.reddit.RedditGifRecipeProvider
import com.facebook.drawee.backends.pipeline.Fresco
import io.reactivex.exceptions.UndeliverableException
import io.reactivex.functions.Consumer
import io.reactivex.plugins.RxJavaPlugins
import java.io.IOException
import java.net.SocketException


class GifRecipesApp: Application(){

    override fun onCreate() {
        super.onCreate()
        // TODO: Real device ID.
        val logger = object: Logger {
            override fun printLn(priority: Int, tag: String, msg: String) = Log.println(priority, tag, msg)
        }
        CoreInitializer.initialize(RedditGifRecipeProvider.create("385ad0c4-31cc-11e7-93ae-92361f002671", logger))
        CacheServerImpl.initialize(this)
        Fresco.initialize(this);

        RxJavaPlugins.setErrorHandler(Consumer<Throwable>({
            var updated: Throwable? = it
            if (it is UndeliverableException) {
                updated = it.cause
            }
            if (updated is IOException || it is SocketException) {
                // fine, irrelevant network problem or API that throws on cancellation
                return@Consumer
            }
            if (updated is InterruptedException) {
                // fine, some blocking code was interrupted by a dispose call
                return@Consumer
            }
            if (updated is NullPointerException || updated is IllegalArgumentException) {
                // that's likely a bug in the application
                Thread.currentThread().uncaughtExceptionHandler.uncaughtException(Thread.currentThread(), updated)
            }
            if (updated is IllegalStateException) {
                // that's a bug in RxJava or in a custom operator
                Thread.currentThread().uncaughtExceptionHandler.uncaughtException(Thread.currentThread(), updated)
            }
            Log.w("Application", "Undeliverable exception received, not sure what to do", it)
        }))
    }
}


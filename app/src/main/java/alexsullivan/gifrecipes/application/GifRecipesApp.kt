package alexsullivan.gifrecipes.application

import alexsullivan.gifrecipes.cache.CacheServerImpl
import alexsullivan.gifrecipes.preferences.RecipePreferences
import android.app.Application
import android.util.Log
import com.alexsullivan.ApplicationInitialization.CoreInitializer
import com.alexsullivan.reddit.providers.createRAlcoholGifRecipesProvider
import com.alexsullivan.reddit.providers.createRGifRecipesProvider
import com.alexsullivan.reddit.providers.createRVeganGifRecipesProvider
import com.facebook.drawee.backends.pipeline.Fresco
import io.reactivex.exceptions.UndeliverableException
import io.reactivex.functions.Consumer
import io.reactivex.plugins.RxJavaPlugins
import java.io.IOException
import java.net.SocketException
import java.util.*


class GifRecipesApp : Application() {

  override fun onCreate() {
    super.onCreate()
    initDeviceId()
    initModules()
    initLibraries()
  }

  private fun initDeviceId() {
    RecipePreferences.init(this)
    if (RecipePreferences.deviceId.isEmpty()) {
      RecipePreferences.deviceId = UUID.randomUUID().toString()
    }
  }

  private fun initModules() {
    CoreInitializer.initialize(
        createRGifRecipesProvider(RecipePreferences.deviceId, AndroidLogger),
        createRVeganGifRecipesProvider(RecipePreferences.deviceId, AndroidLogger),
        createRAlcoholGifRecipesProvider(RecipePreferences.deviceId, AndroidLogger)
    )
  }

  private fun initLibraries() {
    CacheServerImpl.initialize(this)
    Fresco.initialize(this)
  }

  private fun initErrorHandling() {
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
        return@Consumer
      }
      if (updated is IllegalStateException) {
        // that's a bug in RxJava or in a custom operator
        Thread.currentThread().uncaughtExceptionHandler.uncaughtException(Thread.currentThread(), updated)
        return@Consumer
      }
      if (updated is RuntimeException) {
        // Probably our bug.
        Thread.currentThread().uncaughtExceptionHandler.uncaughtException(Thread.currentThread(), updated)
      }
      Log.w("Application", "Undeliverable exception received, not sure what to do", it)
    }))
  }
}


package alexsullivan.gifrecipes.application

import android.app.Application
import android.util.Log
import com.alexsullivan.ApplicationInitialization.CoreInitializer
import com.alexsullivan.logging.Logger
import com.alexsullivan.reddit.RedditGifRecipeProvider
import com.facebook.drawee.backends.pipeline.Fresco

class GifRecipesApp: Application(){

    override fun onCreate() {
        super.onCreate()
        // TODO: Real device ID.
        val logger = object: Logger {
            override fun printLn(priority: Int, tag: String, msg: String) = Log.println(priority, tag, msg)
        }
        CoreInitializer.init(RedditGifRecipeProvider.create("385ad0c4-31cc-11e7-93ae-92361f002671", logger))
        Fresco.initialize(this);
    }
}


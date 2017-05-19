package alexsullivan.gifrecipes.application

import android.app.Application
import com.alexsullivan.ApplicationInitialization.CoreInitializer
import com.alexsullivan.reddit.RedditGifRecipeProvider
import com.facebook.drawee.backends.pipeline.Fresco

class GifRecipesApp: Application(){

    override fun onCreate() {
        super.onCreate()
        // TODO: Real device ID.
        CoreInitializer.init(RedditGifRecipeProvider.create("385ad0c4-31cc-11e7-93ae-92361f002671"))
        Fresco.initialize(this);
    }
}


package alexsullivan.gifrecipes

import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.alexsullivan.GifRecipeRepository
import com.facebook.drawee.backends.pipeline.Fresco
import com.facebook.drawee.view.SimpleDraweeView
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class RecipesActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recipes)
        val view = findViewById(R.id.draweeView) as SimpleDraweeView
        GifRecipeRepository.default().consumeGifRecipes(5)
                .subscribeOn(Schedulers.io())
                .take(1)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    val uri = Uri.parse(it.url)
                    val controller = Fresco.newDraweeControllerBuilder()
                            .setUri(uri)
                            .setAutoPlayAnimations(true)
                            .build();
                    view.controller = controller
                }

    }
}

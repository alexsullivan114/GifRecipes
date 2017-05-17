package alexsullivan.gifrecipes

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.alexsullivan.reddit.RedditGifRecipeProvider
import io.reactivex.schedulers.Schedulers

class RecipesActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recipes)
        RedditGifRecipeProvider.create("385ad0c4-31cc-11e7-93ae-92361f002671")
                .consumeRecipes(5)
                .subscribeOn(Schedulers.io())
                .repeat(2)
                .subscribe {
                    print("wtf")
                }
    }
}

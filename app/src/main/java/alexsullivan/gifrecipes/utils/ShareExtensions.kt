package alexsullivan.gifrecipes.utils

import alexsullivan.gifrecipes.R
import android.content.Context
import android.content.Intent

fun Context.shareRecipe(url: String) {
    val sendIntent = Intent()
    sendIntent.action = Intent.ACTION_SEND
    sendIntent.putExtra(Intent.EXTRA_TEXT, url)
    sendIntent.type = "text/plain"
    startActivity(Intent.createChooser(sendIntent, str(R.string.share_title)))
}
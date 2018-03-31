package alexsullivan.gifrecipes.utils

import alexsullivan.gifrecipes.R
import android.content.Context
import android.content.Intent
import android.net.Uri

fun Context.shareRecipe(url: String) {
    val sendIntent = Intent()
    sendIntent.action = Intent.ACTION_SEND
    sendIntent.putExtra(Intent.EXTRA_TEXT, url)
    sendIntent.type = "text/plain"
    startActivity(Intent.createChooser(sendIntent, str(R.string.share_title)))
}

fun Context.link(url: String) {
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
    startActivity(intent)
}
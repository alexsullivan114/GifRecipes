package alexsullivan.gifrecipes.utils

import android.content.Context
import android.support.annotation.StringRes

fun Context.str(@StringRes stringRes: Int) = getString(stringRes)
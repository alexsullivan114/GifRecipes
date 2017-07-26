package alexsullivan.gifrecipes.utils

import android.app.Activity
import android.content.Context
import android.graphics.Point
import android.support.annotation.StringRes
import android.support.v4.app.Fragment

fun Context.str(@StringRes stringRes: Int) = getString(stringRes)

fun Fragment.str(@StringRes stringRes: Int) = getString(stringRes)
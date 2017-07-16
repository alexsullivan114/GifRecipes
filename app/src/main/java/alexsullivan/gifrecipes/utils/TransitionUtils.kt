package alexsullivan.gifrecipes.utils

import android.R
import android.app.Activity
import android.support.v4.app.ActivityOptionsCompat
import android.support.v4.util.Pair
import android.transition.Transition
import android.view.View
import android.view.Window

fun endListener(callback: (Transition?) -> Unit): Transition.TransitionListener {
    return object: Transition.TransitionListener {
        override fun onTransitionEnd(transition: Transition?) {callback(transition)}
        override fun onTransitionResume(transition: Transition?) {}
        override fun onTransitionPause(transition: Transition?) {}
        override fun onTransitionCancel(transition: Transition?) {}
        override fun onTransitionStart(transition: Transition?) {}
    }
}

fun Activity.makeSceneTransitionWithNav(activity: Activity, vararg pairs: Pair<View, String>): ActivityOptionsCompat {
    val navBar: View? = activity.findViewById(R.id.navigationBarBackground)
    var updatedPairs = pairs
    navBar?.let {
        val navPair = android.support.v4.util.Pair(it, Window.NAVIGATION_BAR_BACKGROUND_TRANSITION_NAME)
        updatedPairs = arrayOf(*pairs, navPair)
    }
    return ActivityOptionsCompat.makeSceneTransitionAnimation(activity, *updatedPairs)
}
package alexsullivan.gifrecipes.utils

import android.transition.Transition

fun endListener(callback: (Transition?) -> Unit): Transition.TransitionListener {
    return object: Transition.TransitionListener {
        override fun onTransitionEnd(transition: Transition?) {}
        override fun onTransitionResume(transition: Transition?) {}
        override fun onTransitionPause(transition: Transition?) {}
        override fun onTransitionCancel(transition: Transition?) {}
        override fun onTransitionStart(transition: Transition?) {
            callback(transition)
        }
    }
}
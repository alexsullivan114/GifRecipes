package alexsullivan.gifrecipes.utils

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.support.annotation.IdRes
import android.view.View
import android.view.ViewAnimationUtils
import android.view.animation.OvershootInterpolator
import android.widget.ImageView

fun ImageView.animatedSetImage(@IdRes resource: Int) {
    // Don't try to animate if we're not attached to the window...
    if (!isAttachedToWindow) {
        setImageResource(resource)
        return
    }
    val startRadius = Math.sqrt((width * width + height * height).toDouble()).toFloat()
    val startAnimator = ViewAnimationUtils.createCircularReveal(this, width / 2, height / 2, startRadius, 0f)
    val endAnimator = ViewAnimationUtils.createCircularReveal(this, width / 2, height / 2, 0f, startRadius)

    startAnimator.addListener(object : AnimatorListenerAdapter() {
        override fun onAnimationEnd(animation: Animator?) {
            setImageResource(resource)
            endAnimator.start()
        }
    })

    startAnimator.start()
}

fun ImageView.animateImageChange(block: (ImageView) -> Unit) {
    val startAnimatorX = ObjectAnimator.ofFloat(this, View.SCALE_X, 0f)
    val startAnimatorY = ObjectAnimator.ofFloat(this, View.SCALE_Y, 0f)
    val endAnimatorX = ObjectAnimator.ofFloat(this, View.SCALE_X, 1f)
    val endAnimatorY = ObjectAnimator.ofFloat(this, View.SCALE_Y, 1f)
    val startSet = AnimatorSet()
    startSet.playTogether(startAnimatorX, startAnimatorY)
    val endSet = AnimatorSet()
    endSet.playTogether(endAnimatorX, endAnimatorY)
    endSet.interpolator = OvershootInterpolator(4.0f)
    startSet.addListener(object: AnimatorListenerAdapter() {
        override fun onAnimationEnd(animation: Animator?) {
            super.onAnimationEnd(animation)
            block(this@animateImageChange)
            endSet.start()
        }
    })

    startSet.start()
}
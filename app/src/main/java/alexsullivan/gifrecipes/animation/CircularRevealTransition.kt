package alexsullivan.gifrecipes.animation

import alexsullivan.gifrecipes.utils.gone
import alexsullivan.gifrecipes.utils.isInvisible
import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.transition.Transition
import android.transition.TransitionValues
import android.view.ViewAnimationUtils
import android.view.ViewGroup

/**
 * Created by Alex Sullivan on 5/7/2016.
 */
class CircularRevealTransition(val x: Float, val y: Float) : Transition() {

    override fun captureStartValues(transitionValues: TransitionValues) {
        // IF VIEW IS INVISIBLE (and we're transitioning into the scene)
        // start at 0,0
        // start radius should be 0
        // end radius should be big
        // IF VIEW IS VISIBLE (and we're transitioning out of the scene)
        // start at width and height of view
        // start radius should be big
        // end radius should be 0
        captureValues(transitionValues)
    }

    override fun captureEndValues(transitionValues: TransitionValues) {
        // I don't think we need to do anything here, but the animation won't run without
        // a difference in start values and end values...
        captureValues(transitionValues)
    }

    override fun createAnimator(sceneRoot: ViewGroup, startValues: TransitionValues, endValues: TransitionValues): Animator? {
        if (endValues.view == null) {
            return null
        }

        val cx = (startValues.values[PROPNAME_CX] as Float).toInt()
        val cy = (startValues.values[PROPNAME_CY] as Float).toInt()
        val endRadius = startValues.values[PROPNAME_END_RADIUS] as Int
        val startRadius = startValues.values[PROPNAME_START_RADIUS] as Int

        // create the animator for this view (the start radius is zero)
        val animator = ViewAnimationUtils.createCircularReveal(endValues.view, cx, cy, startRadius.toFloat(), endRadius.toFloat())
        // This is strange, but I'm seeing a flash at the end of an exit transition, and setting the views
        // visibility to gone seems to fix it. Unfortunate hack.
        animator.addListener(object: AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator?) {
                if (endValues.view.isInvisible) {
                    endValues.view.gone()
                }
            }
        })

        return animator
    }

    private fun captureValues(transitionValues: TransitionValues) {
        val view = transitionValues.view

        var endRadius = 0
        var startRadius = 0
        if (view.isInvisible) {
            startRadius = 0
            endRadius = Math.sqrt((view.width * view.width + view.height * view.height).toDouble()).toInt()
        } else {
            startRadius = Math.sqrt((view.width * view.width + view.height * view.height).toDouble()).toInt()
            endRadius = 0
        }

        transitionValues.values.put(PROPNAME_START_RADIUS, startRadius)
        transitionValues.values.put(PROPNAME_END_RADIUS, endRadius)
        transitionValues.values.put(PROPNAME_CX, x)
        transitionValues.values.put(PROPNAME_CY, y)
    }

    companion object {
        private val PROPNAME_END_RADIUS = "com.peoples.materialfitness:circularreveal:endRadius"
        private val PROPNAME_START_RADIUS = "com.peoples.materialfitness.circularreveal:startRadius"
        private val PROPNAME_CX = "com.peoples.materialfitness:circularreveal:cx"
        private val PROPNAME_CY = "com.peoples.materialfitness:circularreveal:cy"
    }
}
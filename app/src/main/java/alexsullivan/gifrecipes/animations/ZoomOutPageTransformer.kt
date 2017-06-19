package alexsullivan.gifrecipes.animations

import android.support.v4.view.ViewPager
import android.view.View




class ZoomOutPageTransformer : ViewPager.PageTransformer {

    override fun transformPage(view: View, position: Float) {
        val pageWidth = view.width
        val pageHeight = view.height

        val parent = view.parent as ViewPager
        val updatedPosition = position - parent.paddingRight / pageWidth.toFloat()

        if (updatedPosition < -2) { // [-Infinity,-1)
            // This page is way off-screen to the left.
            view.alpha = MIN_ALPHA

        } else if (updatedPosition<= 2) { // [-1,1]
            // Modify the default slide transition to shrink the page as well
            val scaleFactor = Math.max(MIN_SCALE, 1 - Math.abs(updatedPosition))
            val vertMargin = pageHeight * (1 - scaleFactor) / 2
            val horzMargin = pageWidth * (1 - scaleFactor) / 2
            if (updatedPosition < 0) {
                view.translationX = horzMargin - vertMargin / 2
            } else {
                view.translationX = -horzMargin + vertMargin / 2
            }

            // Scale the page down (between MIN_SCALE and 1)
            view.scaleX = scaleFactor
            view.scaleY = scaleFactor

            // Fade the page relative to its size.
            view.alpha = MIN_ALPHA + (scaleFactor - MIN_SCALE) / (1 - MIN_SCALE) * (1 - MIN_ALPHA)

        } else { // (1,+Infinity]
            // This page is way off-screen to the right.
            view.alpha = MIN_ALPHA
        }
    }

    companion object {
        private val MIN_SCALE = 0.55f
        private val MIN_ALPHA = 0.5f
    }
}

package alexsullivan.gifrecipes.utils

import android.support.v4.view.ViewPager
import android.view.View

fun View.show(value: Boolean) {
    if (value) {
        this.visibility = View.VISIBLE
    } else {
        this.visibility = View.GONE
    }
}

fun ViewPager.pageChangeListener(pageScrollStateChanged: (Int) -> Unit = {},
                                 pageScrolled: (Int, Float, Int) -> Unit = { _, _, _ -> },
                                 pageSelected: (Int) -> Unit = {}) {
    this.addOnPageChangeListener(object: ViewPager.OnPageChangeListener {
        override fun onPageScrollStateChanged(state: Int) = pageScrollStateChanged(state)
        override fun onPageSelected(position: Int) = pageSelected(position)
        override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
            pageScrolled(position, positionOffset, positionOffsetPixels)
        }
    })
}
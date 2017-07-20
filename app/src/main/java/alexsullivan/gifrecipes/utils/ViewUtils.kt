package alexsullivan.gifrecipes.utils

import android.graphics.Matrix
import android.graphics.SurfaceTexture
import android.support.v4.view.ViewPager
import android.view.TextureView
import android.view.View

fun View.show(value: Boolean) {
    if (value) {
        this.visibility = View.VISIBLE
    } else {
        this.visibility = View.GONE
    }
}

fun View.visible() {
    this.visibility = View.VISIBLE
}

fun View.gone() {
    this.visibility = View.GONE
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

fun TextureView.adjustAspectRatio(videoWidth: Int, videoHeight: Int) {
    val viewWidth = width
    val viewHeight = height
    val aspectRatio = videoHeight.toDouble() / videoWidth

    val newWidth: Int
    val newHeight: Int
    if (viewHeight > (viewWidth * aspectRatio).toInt()) {
        // limited by narrow width; restrict height
        newWidth = viewWidth
        newHeight = (viewWidth * aspectRatio).toInt()
    } else {
        // limited by short height; restrict width
        newWidth = (viewHeight / aspectRatio).toInt()
        newHeight = viewHeight
    }
    val xoff = (viewWidth - newWidth) / 2f
    val yoff = (viewHeight - newHeight) / 2f

    val txform = Matrix()
    getTransform(txform)
    txform.setScale(newWidth.toFloat() / viewWidth, newHeight.toFloat() / viewHeight)
    txform.postTranslate(xoff, yoff)
    setTransform(txform)
}

fun TextureView.surfaceTextureAvailableListener(callback: (surface: SurfaceTexture, width: Int, height: Int) -> Unit) {
    this.surfaceTextureListener = object: TextureView.SurfaceTextureListener {
        override fun onSurfaceTextureSizeChanged(p0: SurfaceTexture?, p1: Int, p2: Int) {}
        override fun onSurfaceTextureUpdated(p0: SurfaceTexture?) {}
        override fun onSurfaceTextureDestroyed(p0: SurfaceTexture?): Boolean = true
        override fun onSurfaceTextureAvailable(surface: SurfaceTexture, width: Int, height: Int) = callback(surface, width, height)
    }
}
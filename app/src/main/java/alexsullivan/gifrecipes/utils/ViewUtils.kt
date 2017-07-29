package alexsullivan.gifrecipes.utils

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.graphics.Matrix
import android.graphics.SurfaceTexture
import android.support.annotation.IdRes
import android.support.v4.view.ViewPager
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.Editable
import android.text.TextWatcher
import android.view.TextureView
import android.view.View
import android.view.ViewAnimationUtils
import android.widget.EditText
import android.widget.ImageView
import io.reactivex.Observable

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

fun View.invisible() {
    this.visibility = View.INVISIBLE
}

val View.isVisible: Boolean
    get() {
        return visibility == View.VISIBLE
    }

val View.isInvisible: Boolean
    get() {
        return visibility == View.INVISIBLE
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

fun RecyclerView.addInfiniteScrollListener(onScrolledToBottomListener: () -> Unit) {
    addOnScrollListener(object: RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            // We're scrolling down.
            if (dy > 0) {
                val manager = recyclerView.layoutManager as LinearLayoutManager
                if (manager.findLastVisibleItemPosition() >= recyclerView.adapter.itemCount - 2) {
                    onScrolledToBottomListener()
                }
            }
        }
    })
}

fun <T: RecyclerView.Adapter<*>> RecyclerView.castedAdapter(clazz: Class<T>): T {
    return adapter as T
}

fun EditText.textObservable(): Observable<String> {
    return Observable.create {
        addTextChangedListener(object: TextWatcher{
            override fun afterTextChanged(s: Editable?) {
                it.onNext(s.toString())
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                //no-op
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                //no-op
            }
        })
    }
}

fun ImageView.animatedSetImage(@IdRes resource: Int) {
    // Don't try to animate if we're not attached to the window...
    if (!isAttachedToWindow) {
        setImageResource(resource)
        return
    }
    val startRadius = Math.sqrt((width * width + height * height).toDouble()).toFloat()
    val startAnimator = ViewAnimationUtils.createCircularReveal(this, width / 2, height / 2, startRadius, 0f)
    val endAnimator = ViewAnimationUtils.createCircularReveal(this, width / 2, height / 2, 0f, startRadius)

    startAnimator.addListener(object: AnimatorListenerAdapter(){
        override fun onAnimationEnd(animation: Animator?) {
            setImageResource(resource)
            endAnimator.start()
        }
    })

    startAnimator.start()
}
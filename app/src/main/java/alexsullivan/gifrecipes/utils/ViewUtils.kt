package alexsullivan.gifrecipes.utils

import android.graphics.Matrix
import android.graphics.Rect
import android.graphics.SurfaceTexture
import android.support.v4.view.ViewPager
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.Editable
import android.text.TextWatcher
import android.view.TextureView
import android.view.TouchDelegate
import android.view.View
import android.widget.EditText
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

fun RecyclerView.addInfiniteScrollListener(onScrolledToBottomListener: () -> Unit): RecyclerView.OnScrollListener {
    val listener = object: RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            // We're scrolling down.
            if (dy > 0) {
                val manager = recyclerView.layoutManager as LinearLayoutManager
                val lastVisibleItemPosition = manager.findLastVisibleItemPosition()
                val adapterCount = recyclerView.adapter.itemCount
                if (lastVisibleItemPosition >= adapterCount - 2) {
                    onScrolledToBottomListener()
                }
            }
        }
    }

    addOnScrollListener(listener)
    return listener
}

fun RecyclerView.removeInfiniteScrollListener(scrollListener: RecyclerView.OnScrollListener) {
    removeOnScrollListener(scrollListener)
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

fun View.bumpTapTarget() {

    val runnable = {
        val delegateArea = Rect()
        getHitRect(delegateArea)
        val touchAreaExpansion = 50
        delegateArea.top -= touchAreaExpansion
        delegateArea.left -= touchAreaExpansion
        delegateArea.right += touchAreaExpansion
        delegateArea.bottom += touchAreaExpansion
        val touchDelegate = TouchDelegate(delegateArea, this)
        if (parent is View) {
            (parent as View).touchDelegate = touchDelegate
        }
    }

    if (isAttachedToWindow && isLaidOut) {
        runnable()
    } else {
        addOnAttachStateChangeListener(object: View.OnAttachStateChangeListener{
            var hasRun = false
            override fun onViewDetachedFromWindow(v: View?) = Unit

            override fun onViewAttachedToWindow(v: View?) {
                if (!hasRun) {
                    post(runnable)
                    hasRun = true
                }
            }
        })
    }
}
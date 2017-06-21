package alexsullivan.gifrecipes

import android.graphics.Bitmap

/**
 * This class is deeply regettable - we want to pass bitmaps to different activities for smooth
 * animation purposes, but bitmaps are often > 1mb, thus exceeding the bundle size limit and removing
 * our ability to pass them through conventional intents. This class is veeerry dangerous - bitmaps
 * are heavy objects and keeping them in a static reference is dangerous, but I can't think of
 * a better way to achieve the smoothness I want.
 */
object BitmapHolder {
    private val map = HashMap<String, Bitmap>()

    fun put(url: String, bitmap: Bitmap) = map.put(url, bitmap)

    fun remove(url: String) = map.remove(url)

    fun clear() = map.clear()

    fun contains(url: String) = map.containsKey(url)

    fun get(url: String) = map.get(url)
}
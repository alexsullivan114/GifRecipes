package alexsullivan.gifrecipes.utils

import android.media.MediaPlayer

/**
 * Apply a method on this media player catching and ignored the possible IllegalStateException
 */
fun MediaPlayer?.safeApply(block: MediaPlayer.() -> Unit) {
    apply {
        try {
            this?.block()
        } catch (ignored: IllegalStateException) {
            //no-opz
        }
    }
}
package alexsullivan.gifrecipes.components

import alexsullivan.gifrecipes.R
import alexsullivan.gifrecipes.utils.animateImageChange
import android.content.Context
import android.util.AttributeSet
import android.widget.ImageView

class LikeButton : ImageView {

    var liked = false
        private set

    constructor(context: Context) : super(context) {
        initImage()
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        initImage()
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        initImage()
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes) {
        initImage()
    }

    private fun initImage() {
        setImageResource(R.drawable.ic_star_border)
    }

    fun setLiked(liked: Boolean, animate: Boolean) {
        if (this.liked != liked) {
            this.liked = liked
            val drawableId = if (liked) R.drawable.ic_star else R.drawable.ic_star_border
            if (animate) {
                animateImageChange {
                    setImageResource(drawableId)
                }
            } else {
                setImageResource(drawableId)
            }
        }
    }
}
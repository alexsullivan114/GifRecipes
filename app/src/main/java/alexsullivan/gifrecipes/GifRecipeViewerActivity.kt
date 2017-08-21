package alexsullivan.gifrecipes

import alexsullivan.gifrecipes.cache.CacheServerImpl
import alexsullivan.gifrecipes.components.State
import alexsullivan.gifrecipes.components.StateAwareMediaPlayer
import alexsullivan.gifrecipes.favoriting.RoomFavoriteCache
import alexsullivan.gifrecipes.database.RoomRecipeDatabaseHolder
import alexsullivan.gifrecipes.utils.*
import alexsullivan.gifrecipes.viewarchitecture.BaseActivity
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.Surface
import android.view.View
import android.view.WindowManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.facebook.drawee.backends.pipeline.Fresco
import kotlinx.android.synthetic.main.layout_gif_recipe_viewer.*
import kotlin.properties.Delegates

class GifRecipeViewerActivity : BaseActivity<GifRecipeViewerViewState, GifRecipeViewerPresenter>() {

    private val PLAYBACK_POSITION_KEY = "PLAYBACK_POSITION_KEY"
    private var mediaPlayer: StateAwareMediaPlayer? = null
    private var initPlaybackPosition = 0

    private var url: String? by Delegates.observable<String?>(null) {
        _, oldValue, newValue ->  triggerPlaybackCheck()
    }
    private var surface: Surface? by Delegates.observable<Surface?>(null) {
        _, oldValue, newValue ->  if (oldValue != newValue) triggerPlaybackCheck()
    }

    private var sharedElementTransitionDone: Boolean by Delegates.observable(false) {
        _, oldValue, newValue -> if (oldValue != newValue) triggerPlaybackCheck()
    }

    companion object IntentFactory {

        val RECIPE_KEY = "RECIPE_KEY"

        fun build(context: Context, recipe: GifRecipeUI): Intent {
            val intent = Intent(context, GifRecipeViewerActivity::class.java)
                    .putExtra(RECIPE_KEY, recipe)
            return intent
        }
    }

    override fun initPresenter(): GifRecipeViewerPresenter {
        return GifRecipeViewerPresenter.create(intent.getParcelableExtra(RECIPE_KEY),
                CacheServerImpl.instance(),
                RoomFavoriteCache.getInstance(RoomRecipeDatabaseHolder.get(applicationContext).gifRecipeDao()
                ))
    }

    @SuppressLint("Recycle")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Keep the screen on so we don't get annoyingly dimmed.
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        // If our activity is being recreated, the shared element transition already happened.
        savedInstanceState?.let {
            sharedElementTransitionDone = true
            initPlaybackPosition = it.getInt(PLAYBACK_POSITION_KEY)
        }
        setContentView(R.layout.layout_gif_recipe_viewer)
        postponeEnterTransition()
        window.enterTransition.addListener(endListener { sharedElementTransitionDone = true })
        video.surfaceTextureAvailableListener { surface, _, _ -> this@GifRecipeViewerActivity.surface = Surface(surface) }
        root.setOnClickListener { finishAfterTransition() }
        // We're doing this so that if a user accidentally clicks the bottom bar (i.e. when trying to
        // click the favorite button or something) we dont frustratingly close the page.
        bottomBar.isClickable = true
        favorite.bumpTapTarget()
        favorite.setOnClickListener {
            presenter.favoriteClicked(!favorite.liked)
        }
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        mediaPlayer?.safeApply {
            outState?.putInt(PLAYBACK_POSITION_KEY, currentPosition)
        }
    }

    override fun onStop() {
        super.onStop()
        mediaPlayer?.safeApply {
            initPlaybackPosition = currentPosition
            stop()
            release()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        surface?.release()
    }

    override fun accept(viewState: GifRecipeViewerViewState) {
        favorite.setLiked(viewState.favoriteStatus(), true)
        favorite.isClickable = !viewState.favoriteLocked()
        when (viewState) {
            is GifRecipeViewerViewState.Preloading -> {
                loadPlaceholderImage(viewState.recipe)
                progress.visibility = View.VISIBLE
                titleText.text = viewState.recipe.title
            }
            is GifRecipeViewerViewState.Loading -> {
                loadPlaceholderImage(viewState.recipe)
                progress.visibility = View.VISIBLE
                progress.progress = viewState.progress.toFloat()
                titleText.text = viewState.recipe.title
            }
            is GifRecipeViewerViewState.PlayingVideo -> {
                progress.visibility = View.GONE
                titleText.text = viewState.recipe.title
                loadPlaceholderImage(viewState.recipe)
                url = viewState.url
                toggleVideoMode()
            }
            is GifRecipeViewerViewState.PlayingGif -> {
                progress.visibility = View.GONE
                titleText.text = viewState.recipe.title
                loadPlaceholderImage(viewState.recipe, {aspectRatio ->
                    toggleGifMode(aspectRatio, viewState.url)
                })
            }
            is GifRecipeViewerViewState.Favorited -> {
                throw RuntimeException("Woops, got a favorited!")
            }
        }
    }

    private fun triggerPlaybackCheck() {
        if (sharedElementTransitionDone && surface != null && !url.isNullOrEmpty()) {
            // If our media player is already initialized don't go through the rest.
            if (mediaPlayer != null && mediaPlayer?.state != State.UNINITIALIZED) return
            mediaPlayer = StateAwareMediaPlayer()
            mediaPlayer?.isLooping = true
            mediaPlayer?.setDataSource(url)
            mediaPlayer?.setSurface(surface)
            mediaPlayer?.prepareAsync()
            mediaPlayer?.setOnPreparedListener {
                mp -> mp.start()
                mp.seekTo(initPlaybackPosition)
                video.adjustAspectRatio(mp.videoWidth, mp.videoHeight)
            }
        }
    }

    private fun toggleGifMode(aspectRatio: Float, url: String) {
        video.visibility = View.GONE
        val controller = Fresco.newDraweeControllerBuilder()
                .setUri(url)
                .setAutoPlayAnimations(true)
                .build()
        gif.controller = controller
        gif.aspectRatio = aspectRatio
    }

    private fun toggleVideoMode() {
        gif.visibility = View.GONE
        video.visibility = View.VISIBLE
    }

    // Loads the placeholder image, doing nothing if there's already a drawable set on the placeholder.
    private fun loadPlaceholderImage(gifRecipe: GifRecipeUI, aspectRatioCallback: (Float) -> Unit = { _ ->}) {
        if (placeholder.drawable == null) {
            Glide.with(this).asDrawable().load(gifRecipe.previewImageUrl()).listener(object: RequestListener<Drawable> {
                override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Drawable>?, isFirstResource: Boolean): Boolean {
                    // Nothing. Won't see an image but the gif/video will still load. Make sure to finish our transition tho
                    startPostponedEnterTransition()
                    return false
                }

                override fun onResourceReady(resource: Drawable, model: Any?, target: Target<Drawable>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean {
                    startPostponedEnterTransition()
                    aspectRatioCallback(resource.intrinsicWidth/resource.intrinsicHeight.toFloat())
                    return false
                }
            }).into(placeholder)
        } else {
            // placeholder is already loaded, so we know the aspect ratio of it already.
            placeholder.drawable.apply {
                aspectRatioCallback(intrinsicWidth.toFloat()/intrinsicHeight)
            }
        }
    }
}
package alexsullivan.gifrecipes;

import alexsullivan.gifrecipes.cache.CacheServerImpl
import alexsullivan.gifrecipes.utils.adjustAspectRatio
import alexsullivan.gifrecipes.utils.endListener
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.SurfaceTexture
import android.media.MediaPlayer
import android.os.Bundle
import android.view.Surface
import android.view.TextureView
import android.view.View
import com.alexsullivan.ImageType
import com.facebook.drawee.backends.pipeline.Fresco
import kotlinx.android.synthetic.main.layout_gif_recipe_viewer.*
import kotlin.properties.Delegates

class GifRecipeViewerActivity : BaseActivity<GifRecipeViewerViewState, GifRecipeViewerPresenter>() {

    private var mediaPlayer: MediaPlayer? = null
    private var url: String? by Delegates.observable<String?>(null) {
        _, oldValue, newValue ->  if (oldValue != newValue) triggerPlaybackCheck()
    }
    private var surface: Surface? by Delegates.observable<Surface?>(null) {
        _, oldValue, newValue ->  if (oldValue != newValue) triggerPlaybackCheck()
    }

    private var sharedElementTransitionDone: Boolean by Delegates.observable(false) {
        _, oldValue, newValue -> if (oldValue != newValue) triggerPlaybackCheck()
    }

    object IntentFactory {

        val URL_KEY = "URL_KEY"
        val TITLE_KEY = "TITLE_KEY"
        val IMAGE_TYPE_KEY = "IMAGE_TYPE_KEY"

        fun build(context: Context, url: String, title: String, imageType: ImageType): Intent {
            val intent = Intent(context, GifRecipeViewerActivity::class.java)
                    .putExtra(URL_KEY, url)
                    .putExtra(TITLE_KEY, title)
                    .putExtra(IMAGE_TYPE_KEY, imageType)
            return intent
        }
    }

    override fun initPresenter(): GifRecipeViewerPresenter {
        return GifRecipeViewerPresenter.create(intent.getStringExtra(IntentFactory.URL_KEY),
                intent.getStringExtra(IntentFactory.TITLE_KEY), intent.getSerializableExtra(IntentFactory.IMAGE_TYPE_KEY) as ImageType,
                CacheServerImpl.instance())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout_gif_recipe_viewer)
        window.enterTransition.addListener(endListener { sharedElementTransitionDone = true })
        video.surfaceTextureListener = (object: TextureView.SurfaceTextureListener{
            override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture?, width: Int, height: Int) {}

            override fun onSurfaceTextureUpdated(surface: SurfaceTexture?) {}

            override fun onSurfaceTextureDestroyed(surface: SurfaceTexture?): Boolean = true

            @SuppressLint("Recycle") override fun onSurfaceTextureAvailable(surface: SurfaceTexture?, width: Int, height: Int) {
                this@GifRecipeViewerActivity.surface = Surface(surface)
            }
        })
    }

    override fun onStop() {
        super.onStop()
        mediaPlayer?.stop()
        surface?.release()
    }

    override fun accept(viewState: GifRecipeViewerViewState) {
        when (viewState) {
            is GifRecipeViewerViewState.Preloading -> {
                placeholder.setImageBitmap(viewState.image)
                recipeTitle.text = viewState.title
                progress.visibility = View.VISIBLE
            }
            is GifRecipeViewerViewState.Loading -> {
                progress.visibility = View.VISIBLE
                progress.progress = viewState.progress.toFloat()
            }
            is GifRecipeViewerViewState.PlayingVideo -> {
                progress.visibility = View.GONE
                placeholder.setImageBitmap(viewState.image)
                recipeTitle.text = viewState.title
                url = viewState.url
                toggleVideoMode()
            }
            is GifRecipeViewerViewState.PlayingGif -> {
                progress.visibility = View.GONE
                placeholder.setImageBitmap(viewState.image)
                recipeTitle.text = viewState.title
                url = viewState.url
                toggleGifMode(viewState.image)
            }
        }
    }

    override fun acknowledge(error: Throwable) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    private fun triggerPlaybackCheck() {
        if (sharedElementTransitionDone && surface != null && !url.isNullOrEmpty()) {
            mediaPlayer = MediaPlayer()
            mediaPlayer?.setDataSource(url)
            mediaPlayer?.setSurface(surface)
            mediaPlayer?.prepareAsync()
            mediaPlayer?.setOnPreparedListener {
                mp -> mp.start()
                video.adjustAspectRatio(mp.videoWidth, mp.videoHeight)
            }
        }
    }

    private fun toggleGifMode(image: Bitmap?) {
        video.visibility = View.GONE
        val controller = Fresco.newDraweeControllerBuilder()
                .setUri(url)
                .setAutoPlayAnimations(true)
                .build()
        var aspectRatio = 1f
        image?.let {
            aspectRatio = (it.width/ it.height).toFloat()
        }
        gif.aspectRatio = aspectRatio
        gif.controller = controller
    }

    private fun toggleVideoMode() {
        gif.visibility = View.GONE
        video.visibility = View.VISIBLE
    }
}
package alexsullivan.gifrecipes;

import alexsullivan.gifrecipes.GifRecipeViewerActivity.IntentFactory.TITLE_KEY
import alexsullivan.gifrecipes.GifRecipeViewerActivity.IntentFactory.URL_KEY
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Matrix
import android.graphics.SurfaceTexture
import android.media.MediaPlayer
import android.os.Bundle
import android.support.v4.app.SharedElementCallback
import android.util.Log
import android.view.Surface
import android.view.TextureView
import android.view.View
import kotlinx.android.synthetic.main.layout_gif_recipe_viewer.*
import kotlin.properties.Delegates




class GifRecipeViewerActivity : BaseActivity<GifRecipeViewerViewState>() {

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

    override val presenter by lazy {
        GifRecipeViewerPresenter.create(intent.getStringExtra(URL_KEY),
                intent.getStringExtra(TITLE_KEY))
    }

    object IntentFactory {

        val URL_KEY = "URL_KEY"
        val TITLE_KEY = "TITLE_KEY"

        fun build(context: Context, url: String, title: String): Intent {
            val intent = Intent(context, GifRecipeViewerActivity::class.java)
                    .putExtra(URL_KEY, url)
                    .putExtra(TITLE_KEY, title)
            return intent
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout_gif_recipe_viewer)
        setEnterSharedElementCallback(object: SharedElementCallback(){
            override fun onSharedElementsArrived(sharedElementNames: MutableList<String>?, sharedElements: MutableList<View>?, listener: OnSharedElementsReadyListener?) {
                super.onSharedElementsArrived(sharedElementNames, sharedElements, listener)
                Log.i("foo", "shared elemeny done")
                sharedElementTransitionDone = true
            }

        })
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
    }

    override fun accept(viewState: GifRecipeViewerViewState) {
        when (viewState) {
            is GifRecipeViewerViewState.Playing -> {
                placeholder.setImageBitmap(viewState.image)
                recipeTitle.text = viewState.title
                url = viewState.url
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
                adjustAspectRatio(mp.videoWidth, mp.videoHeight)
            }
        }
    }

    private fun adjustAspectRatio(videoWidth: Int, videoHeight: Int) {
        val viewWidth = video.width
        val viewHeight = video.height
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
        Log.v(TAG, "video=" + videoWidth + "x" + videoHeight + " view=" + viewWidth + "x" + viewHeight + " newView=" + newWidth + "x" + newHeight + " off=" + xoff + "," + yoff)

        val txform = Matrix()
        video.getTransform(txform)
        txform.setScale(newWidth.toFloat() / viewWidth, newHeight.toFloat() / viewHeight)
        txform.postTranslate(xoff, yoff)
        video.setTransform(txform)
    }
}
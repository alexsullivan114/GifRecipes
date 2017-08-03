package alexsullivan.gifrecipes.components

import android.media.MediaPlayer

class StateAwareMediaPlayer: MediaPlayer(), MediaPlayer.OnPreparedListener {

    var state = State.UNINITIALIZED
    private var wrappedSetupListener: OnPreparedListener? = null

    override fun setOnPreparedListener(listener: OnPreparedListener?) {
        super.setOnPreparedListener(listener)
        wrappedSetupListener = listener
    }

    override fun prepare() {
        super.prepare()
        state = State.PREPARED
    }

    override fun prepareAsync() {
        super.prepareAsync()
        state = State.PREPARING
    }

    override fun release() {
        super.release()
        state = State.UNINITIALIZED
    }

    override fun onPrepared(mp: MediaPlayer?) {
        state = State.PREPARED
        wrappedSetupListener?.onPrepared(mp)
    }
}

public enum class State {
    UNINITIALIZED,
    PREPARED,
    PREPARING
}
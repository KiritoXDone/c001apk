package com.example.c001apk.media

import android.content.Context
import android.view.TextureView
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.media3.common.AudioAttributes
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.VideoSize
import androidx.media3.exoplayer.ExoPlayer
import androidx.core.net.toUri
import java.io.File

class MediaPlaybackSession : DefaultLifecycleObserver {

    private var player: ExoPlayer? = null
    private var activeTarget: PlaybackTarget? = null
    private var lifecycleOwner: LifecycleOwner? = null
    private val targets = mutableSetOf<PlaybackTarget>()
    private var selectedPosition: Int? = null

    internal fun register(target: PlaybackTarget) {
        targets += target
        target.onPageSelectionChanged(selectedPosition == target.pagePosition)
    }

    internal fun unregister(target: PlaybackTarget) {
        stop(target)
        targets -= target
    }

    fun selectPage(position: Int) {
        if (selectedPosition == position) return
        selectedPosition = position
        targets.toList().forEach { target ->
            target.onPageSelectionChanged(target.pagePosition == position)
        }
    }

    internal fun attach(context: Context) {
        val owner = context as? LifecycleOwner ?: return
        if (lifecycleOwner === owner) return
        lifecycleOwner?.lifecycle?.removeObserver(this)
        lifecycleOwner = owner
        owner.lifecycle.addObserver(this)
    }

    internal fun play(
        context: Context,
        target: PlaybackTarget,
        textureView: TextureView,
        video: File,
        muted: Boolean,
    ) {
        if (selectedPosition != target.pagePosition) return
        attach(context)
        val exoPlayer = player ?: createPlayer(context).also { player = it }
        val previousTarget = activeTarget
        activeTarget = null
        if (previousTarget !== target) previousTarget?.onPlaybackDetached()
        exoPlayer.stop()
        exoPlayer.clearMediaItems()
        exoPlayer.clearVideoSurface()
        exoPlayer.setVideoTextureView(textureView)
        exoPlayer.volume = if (muted) 0f else 1f
        exoPlayer.repeatMode = Player.REPEAT_MODE_OFF
        exoPlayer.setMediaItem(MediaItem.fromUri(video.toUri()))
        activeTarget = target
        exoPlayer.prepare()
        exoPlayer.play()
    }

    internal fun setMuted(target: PlaybackTarget, muted: Boolean) {
        if (activeTarget === target) {
            player?.volume = if (muted) 0f else 1f
        }
    }

    internal fun clearVideoSurface(target: PlaybackTarget) {
        if (activeTarget === target) {
            player?.clearVideoSurface()
        }
    }

    internal fun pause(target: PlaybackTarget) {
        if (activeTarget === target) {
            player?.pause()
        }
    }

    internal fun stop(target: PlaybackTarget) {
        if (activeTarget !== target) return
        player?.run {
            pause()
            stop()
            clearMediaItems()
            clearVideoSurface()
        }
        activeTarget = null
    }

    private fun createPlayer(context: Context): ExoPlayer = ExoPlayer.Builder(context).build().apply {
        setAudioAttributes(AudioAttributes.DEFAULT, true)
        addListener(object : Player.Listener {
            override fun onRenderedFirstFrame() {
                activeTarget?.onVideoFrameAvailable()
            }

            override fun onVideoSizeChanged(videoSize: VideoSize) {
                activeTarget?.onVideoSizeChanged(
                    videoSize.width,
                    videoSize.height,
                    videoSize.pixelWidthHeightRatio,
                )
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == Player.STATE_ENDED) {
                    pause()
                    activeTarget?.onPlaybackEnded()
                }
            }

            override fun onPlayerError(error: PlaybackException) {
                activeTarget?.onPlaybackFailed(error)
            }
        })
    }

    override fun onDestroy(owner: LifecycleOwner) {
        activeTarget?.onPlaybackDetached()
        activeTarget = null
        targets.clear()
        selectedPosition = null
        player?.release()
        player = null
        lifecycleOwner = null
        owner.lifecycle.removeObserver(this)
    }
}

internal interface PlaybackTarget {
    val pagePosition: Int
    fun onPageSelectionChanged(selected: Boolean)
    fun onVideoFrameAvailable()
    fun onVideoSizeChanged(width: Int, height: Int, pixelWidthHeightRatio: Float)
    fun onPlaybackEnded()
    fun onPlaybackFailed(error: PlaybackException)
    fun onPlaybackDetached()
}

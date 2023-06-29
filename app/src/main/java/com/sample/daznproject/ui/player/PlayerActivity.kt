package com.sample.daznproject.ui.player

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.dash.DashMediaSource
import androidx.media3.ui.PlayerView
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.analytics.ktx.logEvent
import com.google.firebase.ktx.Firebase
import com.sample.daznproject.Constants
import com.sample.daznproject.ui.main.MainActivity
import com.sample.daznproject.ui.theme.DaznProjectTheme

class PlayerActivity : ComponentActivity() {

    private var pauseCount = 0
    private val pauseCountText = mutableStateOf("Pause Count :: 0")
    private var forwardCount = 0
    private val forwardCountText = mutableStateOf("Forward Count :: 0")
    private var backwardCount = 0
    private val backwardCountText = mutableStateOf("Backward Count :: 0")
    private var isForwardOrBackward = false
    private lateinit var analytics: FirebaseAnalytics

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        analytics = Firebase.analytics
        setContent {
            DaznProjectTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    val videoUrl = intent.getStringExtra(MainActivity.SELECTED_VIDEO_URL)
                    val padding = 8.dp
                    var lifecycle by remember {
                        mutableStateOf(Lifecycle.Event.ON_CREATE)
                    }
                    val lifeCycleOwner = LocalLifecycleOwner.current
                    DisposableEffect(lifeCycleOwner) {
                        val observer = LifecycleEventObserver { _, event ->
                            lifecycle = event
                        }
                        lifeCycleOwner.lifecycle.addObserver(observer)
                        onDispose {
                            lifeCycleOwner.lifecycle.removeObserver(observer)
                        }
                    }
                    Column(
                        modifier = Modifier
                            .padding(padding)
                            .fillMaxWidth()
                    ) {
                        Card(
                            elevation = 8.dp,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            videoUrl?.let { uri ->
                                val exoPlayer = buildExoPlayer(this@PlayerActivity, uri = uri)
                                exoPlayer.playWhenReady = true
                                exoPlayer.videoScalingMode =
                                    C.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING
                                exoPlayer.addListener(playerListener)
                                AndroidView(
                                    factory = { context ->
                                        PlayerView(context).also {
                                            it.player = exoPlayer
                                        }
                                    },
                                    update = {
                                        when (lifecycle) {
                                            Lifecycle.Event.ON_PAUSE -> {
                                                it.onResume()
                                                it.player?.pause()
                                            }
                                            Lifecycle.Event.ON_RESUME -> {
                                                it.onResume()
                                            }
                                            else -> Unit
                                        }
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .aspectRatio(16 / 9f)
                                )
                            }
                        }

                        PauseCountTextView()
                        ForwardCountTextView()
                        Spacer(modifier = Modifier.size(15.dp))
                        BackwardCountTextView()
                    }
                }
            }
        }
    }

    @Composable
    fun PauseCountTextView() {
        val finalPauseCountText by pauseCountText
        Text(
            text = finalPauseCountText,
            modifier = Modifier.padding(15.dp),
            style = TextStyle(fontWeight = FontWeight.Bold)
        )
    }

    @Composable
    fun ForwardCountTextView() {
        val finalForwardCountText by forwardCountText
        Text(
            text = finalForwardCountText,
            modifier = Modifier.padding(horizontal = 15.dp),
            style = TextStyle(fontWeight = FontWeight.Bold)
        )
    }

    @Composable
    fun BackwardCountTextView() {
        val finalBackwardCountText by backwardCountText
        Text(
            text = finalBackwardCountText,
            modifier = Modifier.padding(horizontal = 15.dp),
            style = TextStyle(fontWeight = FontWeight.Bold)
        )
    }

    private fun buildExoPlayer(context: Context, uri: String): ExoPlayer {
        return ExoPlayer.Builder(context)
            .setSeekBackIncrementMs(10000)
            .setSeekForwardIncrementMs(10000)
            .build()
            .apply {
                val defaultDataSourceFactory = DefaultDataSource.Factory(context)
                val dataSourceFactory: DataSource.Factory = DefaultDataSource.Factory(
                    context,
                    defaultDataSourceFactory
                )
                val source = DashMediaSource.Factory(dataSourceFactory)
                    .createMediaSource(MediaItem.fromUri(uri))
                setMediaSource(source)
                prepare()
            }
    }

    private val playerListener = object : Player.Listener {
        override fun onPositionDiscontinuity(
            oldPosition: Player.PositionInfo,
            newPosition: Player.PositionInfo,
            reason: Int
        ) {
            when (reason) {
                Player.DISCONTINUITY_REASON_SEEK -> {
                    if (oldPosition.positionMs < newPosition.positionMs) {
                        isForwardOrBackward = true
                        forwardCount++
                        forwardCountText.value = "Forward Count :: $forwardCount"
                        analytics.logEvent(Constants.FORWARD, null)
                    } else {
                        isForwardOrBackward = true
                        backwardCount++
                        backwardCountText.value = "Backward Count :: $backwardCount"
                        analytics.logEvent(Constants.BACKWARD, null)
                    }
                }
                else -> Unit
            }
        }

        override fun onIsPlayingChanged(isPlaying: Boolean) {
            super.onIsPlayingChanged(isPlaying)
            if (isPlaying) {
                analytics.logEvent(Constants.PLAYING, null)
            } else {
                if (!isForwardOrBackward) {
                    pauseCount++
                    pauseCountText.value = "Pause Count :: $pauseCount"
                    analytics.logEvent(Constants.PAUSE, null)
                }
                isForwardOrBackward = false
            }
        }

        override fun onPlaybackStateChanged(playbackState: Int) {
            super.onPlaybackStateChanged(playbackState)
            analytics.logEvent(Constants.PLAY_STATE_CHANGED) {
                param(Constants.PLAY_STATE, playbackState.toString())
            }
        }
    }
}


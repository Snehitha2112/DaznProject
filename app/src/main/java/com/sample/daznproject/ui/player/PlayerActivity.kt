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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.util.Util
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.dash.DashMediaSource
import androidx.media3.ui.PlayerView
import com.sample.daznproject.ui.main.MainActivity
import com.sample.daznproject.ui.theme.DaznProjectTheme

class PlayerActivity : ComponentActivity() {

    private var exoPlayer: ExoPlayer? = null
    var playbackPosition: Long = 0
    var currentWindow = 0
    private var pauseCount = 0
    private var forwardCount = 0
    private var backwardCount = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DaznProjectTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    VideoPlayerView(
                        intent.getStringExtra(MainActivity.SELECTED_VIDEO_URL)
                        /*"asset:///food.mp4"*/
                    )
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        restartPlayer()
    }

    override fun onPause() {
        super.onPause()
        if (Util.SDK_INT <= 23) {
            stopPlayer()
        }
    }

    override fun onStop() {
        super.onStop()
        if (Util.SDK_INT > 23) {
            stopPlayer()
        }
    }

    private fun stopPlayer() {
        if (exoPlayer != null) {
            playbackPosition = exoPlayer!!.currentPosition
            currentWindow = exoPlayer!!.currentMediaItemIndex
            exoPlayer!!.playWhenReady = false
        }
    }

    private fun restartPlayer() {
        exoPlayer?.seekTo(currentWindow, playbackPosition)
    }

    @Composable
    fun VideoPlayerView(videoUrl: String?) {
        val padding = 8.dp
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
                    exoPlayer = remember {
                        buildExoPlayer(this@PlayerActivity, uri = uri)
                    }
                    exoPlayer?.playWhenReady = true
                    exoPlayer?.videoScalingMode = C.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING

                    DisposableEffect(
                        AndroidView(
                            factory = {
                                PlayerView(it).apply {
                                    player = exoPlayer
                                }
                            }, modifier = Modifier
                                .height(height = 200.dp)
                                .fillMaxWidth()
                        )
                    ) {
                        onDispose { exoPlayer?.release() }
                    }
                }
            }

            Text(
                text = "Pause Count :: $pauseCount",
                modifier = Modifier.padding(15.dp)
            )
            Text(
                text = "Forward Count :: $pauseCount",
                modifier = Modifier.padding(horizontal = 15.dp)
            )
            Spacer(modifier = Modifier.size(15.dp))
            Text(
                text = "Backward Count :: $pauseCount",
                modifier = Modifier.padding(horizontal = 15.dp)
            )

        }
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
}
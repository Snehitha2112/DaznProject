package com.sample.daznproject.ui.player

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.dash.DashMediaSource
import androidx.media3.ui.PlayerView
import com.sample.daznproject.ui.main.MainActivity
import com.sample.daznproject.ui.theme.DaznProjectTheme

class PlayerActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            DaznProjectTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    VideoPlayer(
                        this,
                        intent.getStringExtra(MainActivity.SELECTED_VIDEO_URL)
                        /*"asset:///food.mp4"*/
                    )
                }
            }
        }
    }
}

@Composable
fun VideoPlayer(context: Context, videoUrl: String?) {
    videoUrl?.let { uri ->
        val exoPlayer = createExoPlayer(context = context, uri = uri)
        exoPlayer.playWhenReady = true
        exoPlayer.videoScalingMode = C.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING
        exoPlayer.repeatMode = Player.REPEAT_MODE_ONE
        val playerView = createPlayerView(context = context)
        DisposableEffect(AndroidView(factory = { playerView }) {
            playerView.player = exoPlayer
        }) {
            onDispose { exoPlayer.release() }
        }
    }
}

@Composable
fun createPlayerView(context: Context): PlayerView {
    return remember {
        PlayerView(context).apply {
            hideController()
        }
    }
}

@Composable
fun createExoPlayer(context: Context, uri: String): ExoPlayer {
    return remember {
        ExoPlayer.Builder(context)
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

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    DaznProjectTheme {
        VideoPlayer(LocalContext.current, null)
    }
}
package com.sample.daznproject.ui.main

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sample.daznproject.R
import com.sample.daznproject.ui.player.PlayerActivity
import com.sample.daznproject.ui.theme.DaznProjectTheme
import com.sample.daznproject.viewmodel.MainViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    companion object {
        const val SELECTED_VIDEO_URL = "selected_video_url"
    }

    private val mainViewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            DaznProjectTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    VideoListView(this, mainViewModel)
                }
            }
        }
    }
}

@Composable
fun VideoListView(context: Context, viewModel: MainViewModel) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text(text = context.getString(R.string.text_video_list))

        val videoListState = viewModel.getVideoListLiveData().observeAsState()
        LaunchedEffect(Unit) {
            viewModel.loadVideoList(context)
        }

        when (val videoList = videoListState.value) {
            null -> Text(text = "Loading Video List...")
            else -> {
                LazyColumn {
                    itemsIndexed(videoList) { position, video ->
                        Text(
                            text = video.name ?: "Video::${position + 1}",
                            fontSize = 16.sp,
                            modifier = Modifier
                                .padding(8.dp)
                                .fillMaxWidth()
                                .clickable {
                                    if (video.videoUri?.isNotEmpty() == true) {
                                        val intent = Intent(context, PlayerActivity::class.java)
                                        intent.putExtra(
                                            MainActivity.SELECTED_VIDEO_URL,
                                            video.videoUri
                                        )
                                        context.startActivity(intent)
                                    } else {
                                        Toast
                                            .makeText(
                                                context,
                                                context.getString(R.string.text_video_not_exists),
                                                Toast.LENGTH_SHORT
                                            )
                                            .show()
                                    }
                                }
                        )

                        if (position < videoList.size - 1) {
                            Divider(color = Color.Gray, thickness = 1.dp, startIndent = 8.dp)
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    DaznProjectTheme {
        VideoListView(LocalContext.current, MainViewModel())
    }
}

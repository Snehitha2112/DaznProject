package com.sample.daznproject.ui.main

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sample.daznproject.R
import com.sample.daznproject.data.model.VideoListModel
import com.sample.daznproject.ui.player.PlayerActivity
import com.sample.daznproject.ui.theme.DaznProjectTheme
import com.sample.daznproject.viewmodel.MainViewModel
import com.sample.daznproject.viewmodel.MainViewModelState
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    companion object {
        const val SELECTED_VIDEO_URL = "selected_video_url"
    }

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            DaznProjectTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(), color = MaterialTheme.colors.background
                ) {
                    val state = viewModel.videoListLiveData.observeAsState()
                    viewModel.loadVideoList()

                    when (val result = state.value) {
                        is MainViewModelState.Videos -> MainView(
                            videos = result.list
                        ) { video ->
                            if (video.videoUri?.isNotEmpty() == true) {
                                val intent = Intent(this, PlayerActivity::class.java)
                                intent.putExtra(
                                    SELECTED_VIDEO_URL, video.videoUri
                                )
                                startActivity(intent)
                            } else {
                                Toast.makeText(
                                    this,
                                    getString(R.string.text_video_not_exists),
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }

                        MainViewModelState.Error -> Error()
                        MainViewModelState.Loading -> Loading()
                        else -> {}
                    }
                }
            }
        }
    }
}

@Composable
fun MainView(videos: List<VideoListModel>, click: (VideoListModel) -> Unit) {
    val padding = 15.dp
    Column(
        modifier = Modifier
            .padding(padding)
            .fillMaxWidth()
    ) {
        val context = LocalContext.current
        Text(
            text = context.getString(R.string.text_video_list),
            Modifier.align(Alignment.CenterHorizontally),
            fontSize = 18.sp,
            style = TextStyle(fontWeight = FontWeight.Bold)
        )
        Spacer(modifier = Modifier.size(padding))
        Card(
            elevation = 8.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            LazyColumn {
                itemsIndexed(videos) { position, video ->
                    Box(modifier = Modifier
                        .fillMaxWidth()
                        .clickable { click(video) }) {
                        Text(
                            text = video.name ?: "Video::${position + 1}",
                            style = TextStyle(fontWeight = FontWeight.SemiBold),
                            fontSize = 16.sp,
                            modifier = Modifier
                                .padding(vertical = 20.dp, horizontal = 10.dp)
                        )
                    }

                    if (position < videos.size - 1) {
                        Divider(color = Color.Gray, thickness = 1.dp)
                    }
                }
            }
        }
    }
}

@Composable
fun Loading() {
    Text(text = "Loading")
}

@Composable
fun Error() {
    Text(text = "Error")
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    DaznProjectTheme {
        MainView(listOf()) {

        }
    }
}



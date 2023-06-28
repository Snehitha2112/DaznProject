package com.sample.daznproject.viewmodel

import android.annotation.SuppressLint
import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.sample.daznproject.data.model.VideoListModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.launch
import java.io.IOException
import javax.inject.Inject

@SuppressLint("StaticFieldLeak")
@HiltViewModel
class MainViewModel @Inject constructor(
    @ApplicationContext private val application: Context
) : ViewModel() {

    private var _videoListLiveData = MutableLiveData<MainViewModelState>()
    val videoListLiveData: LiveData<MainViewModelState> = _videoListLiveData

    fun loadVideoList() {
        _videoListLiveData.value = MainViewModelState.Loading
        viewModelScope.launch {
            try {
                val videosList: List<VideoListModel> =
                    Gson().fromJson(/* json = */ application.assets.open("videolist.json")
                        .bufferedReader().use { it.readText() },/* typeOfT = */
                        object : TypeToken<List<VideoListModel>>() {}.type
                    )
                _videoListLiveData.value = MainViewModelState.Videos(videosList)
            } catch (e: IOException) {
                e.printStackTrace()
                _videoListLiveData.value = MainViewModelState.Error
            }
        }
    }
}

sealed interface MainViewModelState {
    class Videos(val list: List<VideoListModel>) : MainViewModelState
    object Loading : MainViewModelState
    object Error : MainViewModelState
}
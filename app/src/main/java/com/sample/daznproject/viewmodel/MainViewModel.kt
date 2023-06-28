package com.sample.daznproject.viewmodel

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.sample.daznproject.data.model.VideoListModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.io.InputStream
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor() : ViewModel() {
    private var videoListLiveData = MutableLiveData<List<VideoListModel>?>()
    suspend fun loadVideoList(context: Context) {
        return withContext(Dispatchers.IO) {
            try {
                val inputStream: InputStream = context.assets.open("videolist.json")
                val jsonString = inputStream.bufferedReader().use { it.readText() }
                val gson = Gson()
                val videoListType = object : TypeToken<List<VideoListModel>>() {}.type
                val videosList: List<VideoListModel> = gson.fromJson(jsonString, videoListType)
                videoListLiveData.postValue(videosList)
            } catch (e: IOException) {
                e.printStackTrace()
                videoListLiveData.postValue(null)
            }
        }
    }

    fun getVideoListLiveData(): MutableLiveData<List<VideoListModel>?> = videoListLiveData
}
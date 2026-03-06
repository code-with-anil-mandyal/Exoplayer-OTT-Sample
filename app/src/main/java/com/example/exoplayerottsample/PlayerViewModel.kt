package com.example.exoplayerottsample

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector

class PlayerViewModel(application: Application) : AndroidViewModel(application) {

    var isLandscape = false
    var isPlaying = true

    val cache = (application as MyApp).simpleCache

    private val cacheFactory = CacheDataSource.Factory()
        .setCache(cache)
        .setUpstreamDataSourceFactory(DefaultHttpDataSource.Factory())
        .setFlags(CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR)

    private val mediaSourceFactory = DefaultMediaSourceFactory(cacheFactory)

    val trackSelector = DefaultTrackSelector(application)

    val player: ExoPlayer by lazy {
        ExoPlayer.Builder(application)
            .setTrackSelector(trackSelector)
            .setMediaSourceFactory(mediaSourceFactory) // ✅ CORRECT PLACE
            .build()
    }


    override fun onCleared() {
        player.release()
        super.onCleared()

    }
}
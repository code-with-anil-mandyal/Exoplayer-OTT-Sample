package com.example.exoplayerottsample

import android.app.Application
import androidx.media3.database.StandaloneDatabaseProvider
import androidx.media3.datasource.cache.LeastRecentlyUsedCacheEvictor
import androidx.media3.datasource.cache.SimpleCache
import java.io.File

class MyApp : Application() {

    lateinit var simpleCache: SimpleCache
        private set

    override fun onCreate() {
        super.onCreate()

        val databaseProvider = StandaloneDatabaseProvider(this)

        simpleCache = SimpleCache(
            File(cacheDir, "video_cache"),
            LeastRecentlyUsedCacheEvictor(200L * 1024 * 1024),
            databaseProvider
        )
    }
}
package com.example.exoplayerottsample

import android.content.Context
import android.util.Log
import androidx.media3.database.StandaloneDatabaseProvider
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.offline.Download
import androidx.media3.exoplayer.offline.DownloadManager
import java.util.concurrent.Executors

object DownloadUtil {

    private var downloadManager: DownloadManager? = null

    fun getDownloadManager(context: Context): DownloadManager {

        if (downloadManager == null) {

            val databaseProvider = StandaloneDatabaseProvider(context)

            val cache = (context.applicationContext as MyApp).simpleCache

            val upstreamFactory = DefaultHttpDataSource.Factory()

           // val executor = Executors.newFixedThreadPool(2)
            val executor = Executors.newSingleThreadExecutor()

            downloadManager = DownloadManager(
                context,
                databaseProvider,
                cache,
                upstreamFactory,
                executor
            )

            // 👇 ADD LISTENER HERE (only once)
            downloadManager?.addListener(object : DownloadManager.Listener {

                override fun onDownloadChanged(
                    downloadManager: DownloadManager,
                    download: Download,
                    finalException: Exception?
                ) {
                    Log.e("DOWNLOAD_STATE", "State: ${download.state}")
                }

                override fun onDownloadRemoved(
                    downloadManager: DownloadManager,
                    download: Download
                ) {
                    Log.e("DOWNLOAD_STATE", "Removed: ${download.state}")
                }
            })
        }

        return downloadManager!!
    }
}
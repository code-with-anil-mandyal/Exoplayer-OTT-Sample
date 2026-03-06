package com.example.exoplayerottsample

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import android.util.Log
import androidx.media3.exoplayer.offline.Download
import androidx.media3.exoplayer.offline.DownloadManager
import androidx.media3.exoplayer.offline.DownloadNotificationHelper
import androidx.media3.exoplayer.offline.DownloadService
import androidx.media3.exoplayer.scheduler.Scheduler

class MyDownloadService : DownloadService(
    1,
    DEFAULT_FOREGROUND_NOTIFICATION_UPDATE_INTERVAL,
    "download_channel",
    R.string.app_name,
    0
) {

    private lateinit var notificationHelper: DownloadNotificationHelper

    override fun onCreate() {
        super.onCreate()
        Log.e("DOWNLOAD_SERVICE", "Service Started")
        notificationHelper = DownloadNotificationHelper(
            this,
            "download_channel"
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "download_channel",
                "Downloads",
                NotificationManager.IMPORTANCE_LOW
            )

            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    override fun getDownloadManager(): DownloadManager {
        return DownloadUtil.getDownloadManager(this)
    }

    override fun getScheduler(): Scheduler?=null
    override fun getForegroundNotification(
        downloads: List<Download>,
        notMetRequirements: Int
    ): Notification {
        downloads.forEach { download ->

            val cache = (application as MyApp).simpleCache
            Log.e("CACHE_SIZE", "downloading: ${cache.cacheSpace / (1024 * 1024)} MB")
            Log.e("CACHE_SIZE", "download.state: ${download.state}")

            if (download.state == Download.STATE_COMPLETED) {
                Log.e("CACHE_SIZE", "Download COMPLETED")
                Log.e("CACHE_SIZE", "Cache after download: ${cache.cacheSpace / (1024 * 1024)} MB")
            }

            if (download.state == Download.STATE_FAILED) {
                Log.e("CACHE_SIZE", "Download FAILED")
            }
        }
        return notificationHelper.buildProgressNotification(
            this,
            R.drawable.ic_download,
            null,
            null,
            downloads,
            notMetRequirements
        )

    }


}
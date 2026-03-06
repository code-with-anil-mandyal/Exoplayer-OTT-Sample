package com.example.exoplayerottsample

import android.content.pm.ActivityInfo
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.offline.DownloadHelper
import androidx.media3.exoplayer.offline.DownloadRequest
import androidx.media3.exoplayer.offline.DownloadService
import androidx.media3.ui.PlayerView
import kotlinx.coroutines.launch
import java.io.IOException


class MainActivity : AppCompatActivity() {

    private lateinit var seekBar: SeekBar
    private lateinit var tvCurrent : TextView
    private lateinit var tvDuration : TextView

    lateinit var progress : ProgressBar

    private val progressHandler = Handler(Looper.getMainLooper())

    private val progressRunnable = object : Runnable {
        override fun run() {
            updateProgress()
            progressHandler.postDelayed(this, 500)
        }
    }
    private lateinit var viewModel: PlayerViewModel
    private lateinit var playerView: PlayerView
    private var player: ExoPlayer? = null


    private lateinit var controlsContainer : View
    private val hideHandler = Handler(Looper.getMainLooper())

   // private val videoUrl = "https://test-streams.mux.dev/x36xhzz/x36xhzz.m3u8" // HLS sample
    private val videoUrl = "https://storage.googleapis.com/exoplayer-test-media-0/BigBuckBunny_320x180.mp4" // mp4 sample


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        viewModel = ViewModelProvider(this)[PlayerViewModel::class.java]

         progress = findViewById<ProgressBar>(R.id.progress)

        seekBar = findViewById(R.id.seekBar)
        tvCurrent = findViewById(R.id.tvCurrent)
        tvDuration = findViewById(R.id.tvDuration)

        controlsContainer = findViewById(R.id.controlsContainer)
        playerView = findViewById(R.id.playerView)

        seekBar.setPadding(0, 0, 0, 0)
        clickListeners()
    }

    private val playerListener = object : Player.Listener {
        override fun onPlaybackStateChanged(state: Int) {
            when(state){
                Player.STATE_BUFFERING -> {
                    progress.visibility = View.VISIBLE
                }
                Player.STATE_READY -> {
                    progress.visibility = View.GONE
                    startProgressUpdates()

                    val cache = viewModel.cache  // expose cache from ViewModel
                    Log.e("CACHE_SIZE", "Cache size: ${cache.cacheSpace / (1024 * 1024)} MB")

                }
                Player.STATE_ENDED, Player.STATE_IDLE -> {

                }

            }
        }

        override fun onPlayerError(error: PlaybackException) {
            Toast.makeText(this@MainActivity, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun startProgressUpdates() {
        progressHandler.removeCallbacks(progressRunnable) // kill old
        updateProgress() // immediate UI sync
        progressHandler.post(progressRunnable)
    }

    private fun stopProgressUpdates() {
        progressHandler.removeCallbacks(progressRunnable)
    }
    private fun updateProgress() {
        player?.let {

            val duration = it.duration

            if (duration <= 0 || duration == C.TIME_UNSET) return

            val position = it.currentPosition

            seekBar.max = duration.toInt()
            seekBar.progress = position.toInt()

            tvCurrent.text = formatTime(position)+"/"
            tvDuration.text = formatTime(duration)
        }
    }

    private fun toggleControls(){
        if(controlsContainer.visibility == View.VISIBLE){
            hideControls()
        } else {
            showControls()
        }
    }

    private fun showControls() {

        controlsContainer.animate().cancel()
        controlsContainer.visibility = View.VISIBLE   // IMPORTANT
        controlsContainer.alpha = 0f                  // reset
        controlsContainer.animate()
            .alpha(1f)
            .setDuration(200)
            .start()

        hideHandler.removeCallbacksAndMessages(null)
        hideHandler.postDelayed({
            hideControls()
        }, 3000)
    }

    private fun hideControls() {
        controlsContainer.animate().cancel()

        controlsContainer.animate()
            .alpha(0f)
            .setDuration(200)
            .withEndAction {
                controlsContainer.visibility = View.GONE
            }
            .start()
    }

    private fun clickListeners() {

        playerView.setOnClickListener {
            toggleControls()
        }

        findViewById<ImageView>(R.id.btnPlay).apply {
            setOnClickListener {
                if(viewModel.isPlaying){
                    viewModel.isPlaying = false
                    player?.pause()
                    setImageResource(R.drawable.ic_pause)
                }else{
                    player?.play()
                    viewModel.isPlaying = true
                    setImageResource(R.drawable.ic_play)
                }
            }



        }

        findViewById<ImageView>(R.id.btnFullScreen).apply {
            setOnClickListener {
                if(viewModel.isLandscape){
                requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                viewModel.isLandscape = false
                    setImageResource(R.drawable.ic_fullscreen_exit)
            }else{
                requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                viewModel.isLandscape = true
                    setImageResource(R.drawable.ic_fullscreen)
            }
            }
        }

        seekBar.setOnSeekBarChangeListener(object  : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(
                p0: SeekBar?,
                p1: Int,
                p2: Boolean
            ) {
                if(p2){
                    player?.seekTo(p1.toLong())
                }
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {
                hideHandler.removeCallbacksAndMessages(null)//dont hide while seeking
            }

            override fun onStopTrackingTouch(p0: SeekBar?) {
               showControls()//restart auto hide
            }

        })

        findViewById<ImageView>(R.id.btnSettings).setOnClickListener {
            showSettingsDialog()
        }

        //forward
        findViewById<ImageView>(R.id.btnForward).setOnClickListener {
            player?.let {
                val newPos = (it.currentPosition + 10_000).coerceAtLeast(0)
                it.seekTo(newPos)
            }

        }

        //replay
        findViewById<ImageView>(R.id.btnReplay).setOnClickListener {
           player?.let {
               val newPos = (it.currentPosition - 10_000).coerceAtLeast(0)
               it.seekTo(newPos)
           }
        }

        //download
        findViewById<ImageView>(R.id.btnDownload).setOnClickListener {
//            val mediaItem = MediaItem.fromUri(videoUrl)
//
//            val downloadRequest = DownloadRequest.Builder("video1", mediaItem.localConfiguration!!.uri)
//                .setMimeType(MimeTypes.APPLICATION_M3U8)
//                .build()
//
//            DownloadService.sendAddDownload(
//                this,
//                MyDownloadService::class.java,
//                downloadRequest,
//                false
//            )
//
//            Toast.makeText(this, "Download started", Toast.LENGTH_SHORT).show()

            val request = DownloadRequest.Builder(
                "video1",
                Uri.parse(videoUrl)
            ).build()

            DownloadService.sendAddDownload(
                this,
                MyDownloadService::class.java,
                request,
                false
            )

            Toast.makeText(this, "Download started", Toast.LENGTH_SHORT).show()
        }



    }

    private fun showQualityDialog(){
        val qualities = arrayOf("Auto", "240p", "360p", "480p", "720p")

        AlertDialog.Builder(this)
            .setTitle("Video Quality")
            .setItems(qualities) { _, which ->

                val selector = viewModel.trackSelector

                val builder = selector.buildUponParameters()

                if (which == 0) {
                    // AUTO (adaptive bitrate)
                    builder.clearVideoSizeConstraints()
                } else {
                    val height = qualities[which].replace("p", "").toInt()

                    // Force resolution
                    builder.setMaxVideoSize(Int.MAX_VALUE, height)
                }

                selector.parameters = builder.build()
            }
            .show()
    }


    private fun showSettingsDialog(){
        val options = arrayOf("Playback Speed", "Video Quality")

        AlertDialog.Builder(this)
            .setTitle("Settings")
            .setItems(options) { _, which ->
                when(which){
                    0 -> showSpeedDialog()
                    1 -> showQualityDialog()
                }
            }.show()
    }

  private fun showSpeedDialog(){
      val speeds = arrayOf("0.5x", "1x", "1.5x", "2x")

      AlertDialog.Builder(this)
          .setTitle("Playback Speed")
          .setItems(speeds) { _, which ->
              val speed = when (which) {
                  0 -> 0.5f
                  1 -> 1f
                  2 -> 1.25f
                  3 -> 1.5f
                  else -> 2f
              }

              player?.setPlaybackSpeed(speed)
          }.show()

    }

    private fun initializePlayer(){
        player = viewModel.player
        playerView.player = player

        player?.removeListener(playerListener)
        player?.addListener(playerListener)


//        if (player?.mediaItemCount == 0) {
//            val mediaItem = MediaItem.fromUri(videoUrl)
//            player?.setMediaItem(mediaItem)
//            player?.prepare()
//            player?.play()
//        }

        val mediaItem = MediaItem.fromUri(videoUrl)
        player?.setMediaItem(mediaItem)
        player?.prepare()
        player?.play()

        if(player?.playbackState == Player.STATE_READY){
                startProgressUpdates()
        }
    }


    private fun releasePlayer(){
        player?.release()
        player = null
    }

    override fun onStart() {
        super.onStart()
        initializePlayer()
    }

    override fun onStop() {
        super.onStop()
       // releasePlayer()
        stopProgressUpdates()
    }

    fun formatTime(ms : Long) : String{
        val totalSecond = ms / 1000
        val minutes = totalSecond / 60
        val seconds = totalSecond % 60
        return String.format("%02d:%02d", minutes, seconds)
    }
}
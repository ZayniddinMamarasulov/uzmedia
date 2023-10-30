package uz.tune.uzmediaplayer

import android.annotation.SuppressLint
import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.MotionEvent
import android.view.View.OnTouchListener
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import uz.tune.uzmediaplayer.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity(), MediaPlayer.OnPreparedListener {

    private lateinit var binding: ActivityMainBinding
    private var mediaPlayer: MediaPlayer? = null
    private var handler: Handler? = null
    private var handlerForward: Handler? = null

    private var currentSongIndex = 0
    private var isPaused = false

    private var songsList = arrayListOf<String>(
        "https://uzbmp3.com/uploads/files/2022-09/alan-walker-faded_(uzbmp3.com).mp3",
        "https://uzhits.net/uploads/files/2023-10/asqar-umarxon-dam-dam_(uzhits.net).mp3",
        "https://uzhits.net/uploads/files/2023-10/alisher-karimov-hasratingda-yonaman_(uzhits.net).mp3",
        "https://uzhits.net/uploads/files/2023-10/doston-ergashev-va-sardor-mamadaliyev-musofir_(uzhits.net).mp3",
        "https://uzhits.net/uploads/files/2023-10/jaloliddin-ahmadaliyev-otda-yurganimdan-senga-ne-zarar_(uzhits.net).mp3",
        "https://uzhits.net/uploads/files/2023-10/jaloliddin-ahmadaliyev-dil-ekan_(uzhits.net).mp3"
    )

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mediaPlayer = MediaPlayer()
        mediaPlayer?.setOnPreparedListener(this)

        binding.btnPlay.setOnClickListener {
            play()
            initPlayButton()

            handler = Handler(Looper.getMainLooper())
            handler?.postDelayed(runnable, 100)
        }

        binding.btnStop.setOnClickListener {
            mediaPlayer?.stop()
        }

        binding.btnBackward.setOnClickListener {
            mediaPlayer?.seekTo(mediaPlayer?.currentPosition?.minus(10000) ?: 0)
        }

        binding.btnForward.setOnClickListener {
            mediaPlayer?.seekTo(mediaPlayer?.currentPosition?.plus(10000) ?: 0)
        }

        binding.btnPrev.setOnClickListener {
            mediaPlayer?.stop()
            if (currentSongIndex > 0) {
                currentSongIndex--
                initPlayButton()
                play()
            }
        }

        binding.btnNext.setOnClickListener {
            mediaPlayer?.stop()
            if (currentSongIndex < songsList.size - 1) {
                currentSongIndex++
                initPlayButton()
                play()
            }
        }



        binding.btnNext.setOnTouchListener(OnTouchListener { v, event ->
            when (event.action) {

                MotionEvent.ACTION_BUTTON_PRESS -> {

                }

                MotionEvent.ACTION_DOWN -> {
                    handlerForward = Handler(Looper.getMainLooper())
                    handlerForward?.postDelayed(forwardRunnable, 500)
                    return@OnTouchListener true
                }

                MotionEvent.ACTION_UP -> {
                    handlerForward?.removeCallbacks(forwardRunnable)

                    mediaPlayer?.stop()
                    if (currentSongIndex < songsList.size - 1) {
                        currentSongIndex++
                        initPlayButton()
                        play()
                    }
                    return@OnTouchListener true
                }
            }
            false
        });

        val songDuration = mediaPlayer?.duration
        if (songDuration != null)
            binding.seekbar.max = songDuration

        binding.seekbar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                if (p2) {
                    mediaPlayer?.seekTo(p1)
                }
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {

            }

            override fun onStopTrackingTouch(p0: SeekBar?) {

            }
        })
    }

    override fun onPrepared(player: MediaPlayer?) {
        if (isPaused.not()) {
            player?.start()
            binding.tvAllTime.text = player?.duration?.let { millisToMinute(it) }
        } else
            player?.seekTo(player.currentPosition)
    }

    private val runnable = object : Runnable {
        override fun run() {
            val currentPosition = mediaPlayer?.currentPosition

            if (currentPosition != null) {
                binding.seekbar.progress = currentPosition
                binding.tvCurrentTime.text = millisToMinute(currentPosition)
                handler?.postDelayed(this, 100)
            }
        }
    }

    private val forwardRunnable = object : Runnable {
        override fun run() {
            mediaPlayer?.seekTo(mediaPlayer?.currentPosition?.plus(3000) ?: 0)
            handlerForward?.postDelayed(this, 500)
        }
    }

    private fun initPlayButton() {
        val songDuration = mediaPlayer?.duration
        if (songDuration != null)
            binding.seekbar.max = songDuration

        if (mediaPlayer?.isPlaying == false) {
            mediaPlayer?.start()
            isPaused = false
            binding.btnPlay.text = "Pause"
            binding.btnPlay.setCompoundDrawablesWithIntrinsicBounds(
                R.drawable.ic_pause,
                0,
                0,
                0
            )
        } else {
            mediaPlayer?.pause()
            isPaused = true
            binding.btnPlay.text = "Play"
            binding.btnPlay.setCompoundDrawablesWithIntrinsicBounds(
                R.drawable.ic_play,
                0,
                0,
                0
            )
        }


    }

    private fun play() {
        if (mediaPlayer?.isPlaying == false) {
            mediaPlayer?.reset()
            mediaPlayer?.setDataSource(songsList[currentSongIndex])
            mediaPlayer?.prepareAsync()
        }
    }

    private fun millisToMinute(songDuration: Int): String {
        val min = songDuration.div(1000).div(60)
        val sec = (songDuration - min * 60 * 1000) / 1000
        return if (sec < 10)
            "$min:0$sec"
        else
            "$min:$sec"
    }
}
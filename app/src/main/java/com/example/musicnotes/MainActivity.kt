package com.example.musicnotes

import android.media.AudioAttributes
import android.media.SoundPool
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var soundPool: SoundPool
    private val recordedNotes = mutableListOf<Int>()
    private var isRecording = false
    private var isPlaying = false
    private var currentOctave = 4
    private val uiScope = CoroutineScope(Dispatchers.Main)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Prepare SoundPool
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_MEDIA)
            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
            .build()
        soundPool = SoundPool.Builder()
            .setMaxStreams(1)
            .setAudioAttributes(audioAttributes)
            .build()

        // Note button handlers
        val notes = listOf(
            R.id.buttonC to "c",
            R.id.buttonD to "d",
            R.id.buttonE to "e",
            R.id.buttonF to "f",
            R.id.buttonG to "g",
            R.id.buttonA to "a",
            R.id.buttonB to "b"
        )
        notes.forEach { (btnId, name) ->
            findViewById<Button>(btnId).setOnClickListener {
                val resName = "${name}${currentOctave}"
                val resId = resources.getIdentifier(resName, "raw", packageName)
                if (resId != 0) {
                    // Play via MediaPlayer to avoid SoundPool load delay
                    val player = android.media.MediaPlayer.create(this, resId)
                    player.setVolume(1.0f, 1.0f)  // ensure maximum playback volume
                    player.setOnCompletionListener { it.release() }
                    player.start()
                    if (isRecording) recordedNotes.add(resId)
                }
            }
        }

        // Octave controls
        val octaveText = findViewById<TextView>(R.id.octaveText)
        findViewById<Button>(R.id.octaveDownButton).setOnClickListener {
            if (currentOctave > 1) {
                currentOctave--
                octaveText.text = "Octave: $currentOctave"
            }
        }
        findViewById<Button>(R.id.octaveUpButton).setOnClickListener {
            if (currentOctave < 7) {
                currentOctave++
                octaveText.text = "Octave: $currentOctave"
            }
        }

        // Record toggle
        val recordBtn = findViewById<Button>(R.id.recordButton)
        recordBtn.setOnClickListener {
            isRecording = !isRecording
            if (isRecording) {
                recordedNotes.clear()
                recordBtn.text = "Stop"
            } else {
                recordBtn.text = "Record"
            }
        }

        // Play recorded sequence
        findViewById<Button>(R.id.playButton).setOnClickListener {
            if (!isPlaying && recordedNotes.isNotEmpty()) {
                isPlaying = true
                uiScope.launch {
                    for (resId in recordedNotes) {
                        val player = android.media.MediaPlayer.create(this@MainActivity, resId)
                        player.setVolume(1.0f, 1.0f)
                        player.setOnCompletionListener { it.release() }
                        player.start()
                        delay(500L)
                    }
                    isPlaying = false
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        soundPool.release()
    }
}
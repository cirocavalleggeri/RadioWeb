package  org.altervista.ultimaprovaprmadi.radioweb

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.ComponentActivity

import androidx.annotation.RequiresApi

import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class ReistraActivityKotlin : ComponentActivity() {
    private lateinit var mediaProjectionManager: MediaProjectionManager
    lateinit  var pulsantestop: Button
   lateinit   var pulsanteStart:Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_registratore)

        pulsantestop= findViewById<Button>(R.id.btn_stop_recording)
        pulsantestop.setOnClickListener{
            stopCapturing()
        }
        pulsanteStart=  findViewById<Button>(R.id.btn_start_recording)

        pulsanteStart.setOnClickListener {
                startCapturing()
            }



    }
    private fun setButtonsEnabled(isCapturingAudio: Boolean) {
        findViewById<Button>(R.id.btn_start_recording).isEnabled = !isCapturingAudio
        findViewById<Button>(R.id.btn_stop_recording).isEnabled = isCapturingAudio
    }
    private fun startCapturing() {
        pulsantestop.isEnabled = true
        if (!isRecordAudioPermissionGranted()) {
            requestRecordAudioPermission()
        } else {
            startMediaProjectionRequest()
        }
    }

    private fun stopCapturing() {

        setButtonsEnabled(isCapturingAudio = false)
        //pulsantestop.isEnabled = false
        startService(Intent(this, AudioCaptureService::class.java).apply {
            action = AudioCaptureService.ACTION_STOP
        })
    }

    private fun isRecordAudioPermissionGranted(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestRecordAudioPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.RECORD_AUDIO),
            RECORD_AUDIO_PERMISSION_REQUEST_CODE
        )
    }
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == RECORD_AUDIO_PERMISSION_REQUEST_CODE) {
            if (grantResults.firstOrNull() == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(
                    this,
                    "Permissions to capture audio granted. Click the button once again.",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                Toast.makeText(
                    this, "Permissions to capture audio denied.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun startMediaProjectionRequest() {
        // use applicationContext to avoid memory leak on Android 10.
        // see: https://partnerissuetracker.corp.google.com/issues/139732252
        mediaProjectionManager =
            applicationContext.getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        startActivityForResult(
            mediaProjectionManager.createScreenCaptureIntent(),
            MEDIA_PROJECTION_REQUEST_CODE
        )
    }
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == MEDIA_PROJECTION_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                Toast.makeText(
                    this,
                    "MediaProjection permission obtained. Foreground service will be started to capture audio.",
                    Toast.LENGTH_SHORT
                ).show()

                val audioCaptureIntent = Intent(this, AudioCaptureService::class.java).apply {
                    action = AudioCaptureService.ACTION_START
                    putExtra(AudioCaptureService.EXTRA_RESULT_DATA, data!!)
                }
                startForegroundService(audioCaptureIntent)

               // setButtonsEnabled(isCapturingAudio = true)
            } else {
                Toast.makeText(
                    this, "Request to obtain MediaProjection denied.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
    companion object {
        private const val RECORD_AUDIO_PERMISSION_REQUEST_CODE = 42
        private const val MEDIA_PROJECTION_REQUEST_CODE = 13
    }



}



package org.altervista.ultimaprovaprmadi.radioweb

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.MediaPlayer.OnPreparedListener
import android.media.projection.MediaProjectionManager
import android.os.AsyncTask
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.ui.AppBarConfiguration
import com.google.android.material.textfield.TextInputEditText
import org.altervista.ultimaprovaprmadi.radioweb.Dati.vettoreStazioniRadio
import org.altervista.ultimaprovaprmadi.radioweb.databinding.ActivityMainBinding
import java.io.IOException

class MainActivity : AppCompatActivity() {
    private lateinit var mediaProjectionManager: MediaProjectionManager
    private val MY_PERMISSIONS_RECORD_AUDIO = 8
    var RADIO_STATION_URL = "http://icestreaming.rai.it/3.mp3"
    var artista: TextView? = null
    var titolo: TextView? = null
    var runnable: Runnable? = null
    var mediaplayer: MediaPlayer? = null
    var handler: Handler? = null
    var spinner_elenco_radio: Spinner? = null
    lateinit var indirizzoWebRadio: TextInputEditText
    //var ricevitoreCanzone: RicevitoreCanzone? = null
    var mReceiver: BroadcastReceiver? = null
    lateinit var btn_startMusica: Button
    lateinit var btn_stoptMusica: Button
    lateinit var btn_startregistraMusica: Button
    lateinit var btn_stopregistraMusica: Button
    //var traduciInTesto: TraduciInTesto? = null
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    var posizioneRadio:Int=1

    override fun onStop() {
        super.onStop()
        /* if (mediaplayer!=null){
         mediaplayer.release();
         mediaplayer = null;
        }
       */
    }

    override fun onDestroy() {
        super.onDestroy()
        if (mediaplayer != null) {
            mediaplayer!!.release()
            mediaplayer = null
        }
        if (mReceiver != null) {
            unregisterReceiver(mReceiver)
        }
    }

    override fun onResume() {
        super.onResume()
        // ricavaIndirizzoAlCambiamentoSpinner();
        AsyncTask.execute(runnable)
        //registraBroadcastREceiver();
        //ricevitoreCanzone = RicevitoreCanzone(this)
        if (mReceiver != null) {
            mReceiver = null
        }
        //mReceiver = ricevitoreCanzone.registraBroadcastREceiver()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        titolo = findViewById(R.id.id_tx_titolo)
        artista = findViewById(R.id.id_tx_artista)
        indirizzoWebRadio = findViewById(R.id.textInputEdit)
        btn_startMusica = findViewById(R.id.id_btn_start)
        btn_stoptMusica = findViewById(R.id.id_btn_stop)
        btn_startregistraMusica = findViewById(R.id.id_btn_registra)
        btn_stopregistraMusica = findViewById(R.id.id_btn_stopregistra)
        btn_stoptMusica.setOnClickListener(View.OnClickListener { stopMusica() })
        btn_startMusica.setOnClickListener(View.OnClickListener { startMusica() })
        btn_startregistraMusica.setOnClickListener(View.OnClickListener {
          Toast.makeText(this,"Ancora da implementare",Toast.LENGTH_LONG).show()

        })
        btn_stopregistraMusica.setOnClickListener(View.OnClickListener {

            Toast.makeText(this,"Ancora da implementare",Toast.LENGTH_LONG).show()
        })
        mediaplayer = MediaPlayer()
        inizializzalistaRadio()
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf<String>(Manifest.permission.RECORD_AUDIO),
                    MY_PERMISSIONS_RECORD_AUDIO
            )
        } else {
            spinner_elenco_radio!!.onItemSelectedListener =
                object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(
                        parentView: AdapterView<*>,
                        selectedItemView: View,
                        position: Int,
                        id: Long
                    ) {
                        // your code here

                        val analisiStringa = parentView.getItemAtPosition(posizioneRadio).toString()
                        if (analisiStringa.contains("http")) {
                            RADIO_STATION_URL =
                                parentView.getItemAtPosition(posizioneRadio).toString()
                        } else {
                            posizioneRadio = position + 1
                            RADIO_STATION_URL =
                                parentView.getItemAtPosition(posizioneRadio).toString()
                        }

                        indirizzoWebRadio.setText(RADIO_STATION_URL)

                        Log.d("RADIO",RADIO_STATION_URL)
                        AsyncTask.execute(runnable)
                    }

                    override fun onNothingSelected(parentView: AdapterView<*>?) {
                        // your code here
                    }
                }
            handler = Handler()
        }
        runnable = Runnable { //your action
            //LeggiTitoloCanzone.titleOfSong();
            try {
                Thread.sleep(1000)
                suonaOnline()
            } catch (e: InterruptedException) {
                throw RuntimeException(e)
            }
        }
        //
        //
        //AsyncTask.execute(runnable);

        // ricavaIndirizzoAlCambiamentoSpinner();
        //traduciInTesto = TraduciInTesto(this)

    }

    private fun suonaOnline() {
        // inizializzalistaRadio();
        stopMusica()
        mediaplayer = MediaPlayer()
        mediaplayer!!.setAudioAttributes(
            AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .build()
        )
        mediaplayer!!.setAudioStreamType(AudioManager.STREAM_MUSIC)
        try {
            mediaplayer!!.setDataSource(RADIO_STATION_URL)
            mediaplayer!!.prepare()
            //mediaplayer.setOnPreparedListener(miolistenermusic);
            mediaplayer!!.prepareAsync()
        } catch (e: IllegalArgumentException) {
            // TODO Auto-generated catch block
            e.printStackTrace()
        } catch (e: SecurityException) {
            // TODO Auto-generated catch block
            e.printStackTrace()
        } catch (e: IllegalStateException) {
            // TODO Auto-generated catch block
            e.printStackTrace()
        } catch (e: IOException) {
            // TODO Auto-generated catch block
            e.printStackTrace()
        }
       /* mediaplayer!!.setOnPreparedListener { mp ->
            mp.start()

            // LeggiTitoloCanzone.ricavaTitoloCanzone();
        }*/
        mediaplayer!!.start()
    }
    private fun stopMusica() {
        if (mediaplayer != null) {
            if (mediaplayer!!.isPlaying) {
                mediaplayer!!.stop()
                mediaplayer!!.reset()
                mediaplayer!!.release()
               // mediaplayer = null
            }
        } else {
        }
    }
    private fun startMusica() {
        AsyncTask.execute(runnable)
    }
    private fun inizializzalistaRadio() {
        val adapter: ArrayAdapter<String> = ArrayAdapter<String>(
            this,
            androidx.appcompat.R.layout.support_simple_spinner_dropdown_item, vettoreStazioniRadio
        )
        spinner_elenco_radio = findViewById<View>(R.id.id_spin_titolo) as Spinner
        spinner_elenco_radio!!.adapter = adapter
        RADIO_STATION_URL = spinner_elenco_radio!!.selectedItem.toString()
        indirizzoWebRadio!!.setText(RADIO_STATION_URL)
    }
    private val miolistenermusic =
        OnPreparedListener { mp ->
            mp.duration
            mp.start()
        }

}
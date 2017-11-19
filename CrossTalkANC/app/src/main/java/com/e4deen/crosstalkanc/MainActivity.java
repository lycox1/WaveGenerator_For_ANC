package com.e4deen.crosstalkanc;
import android.Manifest;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.util.Arrays;

import static java.lang.Math.exp;
import static java.lang.Math.log10;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    String LOG_TAG = "AudioTrackTest";
    Button button_play1, button_stop;
    boolean isPlaying = false;
    int CHANNEL_MONO = 1;
    int CHANNEL_STEREO = 2;
    double[] wave_freq = new double[4];
    double wave_gain_floating, wave_gain_db;
    int NUM_CHANNEL = CHANNEL_STEREO;

//    private final int duration = 1; // seconds
    private final int sampleRate = 44100;
    //private final int sampleRate = 8000;
    //private final int numSamples = duration * sampleRate;
//    private final int numSamples = duration * sampleRate;

    private final double sample[] = new double[sampleRate * NUM_CHANNEL];
    private final double freqOfTone = 1500; // hz
    private final byte generatedSnd[] = new byte[2 * sampleRate * NUM_CHANNEL];
    public EditText et_wave1, et_wave2, et_wave3, et_wave4, et_gain;

    Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        checkPermissions();

        button_play1 = (Button)findViewById(R.id.button_play);
        button_stop = (Button)findViewById(R.id.button_stop);

        et_wave1 = (EditText) findViewById(R.id.et_wave1);
        et_wave2 = (EditText) findViewById(R.id.et_wave2);
        et_wave3 = (EditText) findViewById(R.id.et_wave3);
        et_wave4 = (EditText) findViewById(R.id.et_wave4);
        et_gain = (EditText) findViewById(R.id.et_gain);

        button_play1.setOnClickListener(this);
        button_stop.setOnClickListener(this);
    }

    void play_start() {
        Log.e(LOG_TAG,"play_start");

        Arrays.fill(wave_freq, 0);
        wave_gain_db = 0;

        if( false == et_wave1.getText().toString().equals("") )
            wave_freq[0] = Integer.parseInt(et_wave1.getText().toString());

        if( false == et_wave2.getText().toString().equals("") )
            wave_freq[1] = Integer.parseInt(et_wave2.getText().toString());

        if( false == et_wave3.getText().toString().equals("") )
            wave_freq[2] = Integer.parseInt(et_wave3.getText().toString());

        if( false == et_wave4.getText().toString().equals("") )
            wave_freq[3] = Integer.parseInt(et_wave4.getText().toString());

        if( false == et_gain.getText().toString().equals("") && false == et_gain.getText().toString().equals("0"))
            wave_gain_db = -1 * (Double.parseDouble(et_gain.getText().toString()));

        //Log.e(LOG_TAG,"play_start wave value test " + et_wave4.getText().toString().equals(""));

        Log.e(LOG_TAG,"play_start wave value test wave_freq " + wave_freq[0] + ", wave_freq " + wave_freq[1] + ", wave_freq "+ wave_freq[2] + ", wave_freq " + wave_freq[3]);

        Thread thread = new Thread(new Runnable() {
            public void run() {
                isPlaying = true;
                genTone();
                playSound();
            }
        });
        thread.start();
    }

    void play_stop() {
        Log.e(LOG_TAG,"play_stop");
        isPlaying = false;
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.button_play:
                play_start();
                break;

            case R.id.button_stop:
                play_stop();
                break;
        }
    }

    void genTone(){
        // fill out the array
        Log.d(LOG_TAG, "genTone() test 1" );
        Arrays.fill(sample, 0);
/*
        for (int i = 0; i < numSamples; ++i) {
            sample[i * 2] = Math.sin(2 * Math.PI * i / (sampleRate / freqOfTone));
            sample[i * 2 + 1] = 0.01 * Math.sin(2 * Math.PI * i / (sampleRate / freqOfTone));
            //            test_sample[i] = Math.sin(2 * Math.PI * freqOfTone * (i/sampleRate) ); // need to cast double;
        }
*/

        if(wave_gain_db> 0) {
            wave_gain_db = 0;
        } else if ( wave_gain_db < -200)
            wave_gain_db = -200;
        wave_gain_floating = dB2amp(wave_gain_db);

        Log.d(LOG_TAG, "genTone() wave_gain_db " + wave_gain_db + ", wave_gain_floating " + wave_gain_floating);

        for(int i =0; i <4; i++) {
            if(wave_freq[i] != 0 ) {
                for (int j = 0; j < sampleRate; ++j) {
                    sample[j * 2] += Math.sin(2 * Math.PI * j / (sampleRate / wave_freq[i] ));
                    //sample[j * 2 + 1] += Math.sin(2 * Math.PI * j / (sampleRate / wave_freq[i]));
                    sample[j * 2 + 1] += wave_gain_floating * Math.sin(2 * Math.PI * j / (sampleRate / wave_freq[i]));
                    //test_sample[i] = Math.sin(2 * Math.PI * freqOfTone * (i/sampleRate) ); // need to cast double;
                }
            }
        }

        // convert to 16 bit pcm sound array
        // assumes the sample buffer is normalised.

        int idx = 0;
        for (double dVal : sample) {
            short val = (short) (dVal * 32767);
            generatedSnd[idx++] = (byte) (val & 0x00ff);
            generatedSnd[idx++] = (byte) ((val & 0xff00) >>> 8);
        }
    }

    void playSound(){
        Log.d(LOG_TAG, "playSound()" );
        AudioTrack audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC,
                //sampleRate, AudioFormat.CHANNEL_CONFIGURATION_MONO,
                sampleRate, AudioFormat.CHANNEL_CONFIGURATION_STEREO,
                //AudioFormat.ENCODING_PCM_16BIT, numSamples * NUM_CHANNEL, AudioTrack.MODE_STATIC);
                AudioFormat.ENCODING_PCM_16BIT, sampleRate * NUM_CHANNEL, AudioTrack.MODE_STREAM);

        Log.d(LOG_TAG, "playSound() getMinBufferSize " + AudioTrack.getMinBufferSize(sampleRate, AudioFormat.CHANNEL_CONFIGURATION_STEREO, AudioFormat.ENCODING_PCM_16BIT));
        Log.d(LOG_TAG, "playSound() generatedSnd size " + generatedSnd.length);

        audioTrack.play();

        while(isPlaying == true) {
            audioTrack.write(generatedSnd, 0, sampleRate * NUM_CHANNEL);
        }
    }

    double amp2dB(double amp)
    {
        // input must be positive +1.0 = 0dB
        if (amp < 0.0000000001) { return -200.0; }
        return (20.0 * log10(amp));
    }

    double dB2amp(double dB)
    {
        // 0dB = 1.0
        //return pow(10.0,(dB * 0.05)); // 10^(dB/20)
        return exp(dB * 0.115129254649702195134608473381376825273036956787109375);
    }



    void checkPermissions() {
        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.MODIFY_AUDIO_SETTINGS) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.MODIFY_AUDIO_SETTINGS)) {
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.MODIFY_AUDIO_SETTINGS}, 1);
            }
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.RECORD_AUDIO)) {
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, 1);
            }
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
            }
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
            }
        }
    }

}


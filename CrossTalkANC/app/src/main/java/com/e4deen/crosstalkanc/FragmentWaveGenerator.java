package com.e4deen.crosstalkanc;

import android.app.Fragment;
import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import java.io.FileOutputStream;
import java.util.Arrays;

import static java.lang.Math.exp;
import static java.lang.Math.log10;

/**
 * Created by user on 2017-11-27.
 */

public class FragmentWaveGenerator extends Fragment implements View.OnClickListener {

    String LOG_TAG = "CrossTalkAnc_WaveGenerator";
    Button button_play, button_stop;
    boolean isPlaying = false;
    int CHANNEL_MONO = 1;
    int CHANNEL_STEREO = 2;
    double[] wave_freq = new double[4];
    double wave_gain_floating, wave_gain_db;
    int NUM_CHANNEL = CHANNEL_STEREO;

    private final int sampleRate = 44100;

    private final double sample[] = new double[sampleRate * NUM_CHANNEL];
    private final byte generatedSnd[] = new byte[2 * sampleRate * NUM_CHANNEL];
    public EditText et_wave1, et_wave2, et_wave3, et_wave4, et_gain;

    Context mContext;

    public FragmentWaveGenerator(Context context) {
        Log.d(LOG_TAG, "Constructor FragmentWaveGenerator");
        mContext = context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_wave_generator, container, false);

        button_play = rootView.findViewById(R.id.button_play);
        button_stop = rootView.findViewById(R.id.button_stop);

        et_wave1 = rootView.findViewById(R.id.et_wave1);
        et_wave2 = rootView.findViewById(R.id.et_wave2);
        et_wave3 = rootView.findViewById(R.id.et_wave3);
        et_wave4 = rootView.findViewById(R.id.et_wave4);
        et_gain =  rootView.findViewById(R.id.et_gain);

        button_play.setOnClickListener(this);
        button_stop.setOnClickListener(this);

        button_play.setEnabled(true);
        button_stop.setEnabled(false);

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
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

        if( false == et_gain.getText().toString().equals("") && false == et_gain.getText().toString().equals("0")) {
//            wave_gain_db = -1 * (Double.parseDouble(et_gain.getText().toString()));

            try {
                wave_gain_db = -1 * (Double.parseDouble(et_gain.getText().toString()));
            } catch (NumberFormatException nfe) {
                wave_gain_db = 0;
                Log.e(LOG_TAG,"play_start gain input format exception ");
            }
        }
        //Log.e(LOG_TAG,"play_start wave value test " + et_wave4.getText().toString().equals(""));

        Log.e(LOG_TAG,"play_start wave value test wave_freq " + wave_freq[0] + ", wave_freq " + wave_freq[1] + ", wave_freq "+ wave_freq[2] + ", wave_freq " + wave_freq[3]);
        Log.e(LOG_TAG,"play_start gain " + wave_gain_db);
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
        Log.e(LOG_TAG, "onClick()");

        switch (v.getId()) {
            case R.id.button_play:
                button_play.setEnabled(false);
                button_stop.setEnabled(true);
                play_start();
                break;

            case R.id.button_stop:
                button_play.setEnabled(true);
                button_stop.setEnabled(false);
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
/*
        String path = "file.txt";
        FileOutputStream output = new FileOutputStream(path);
        //int bufferSize = 1024;
        //byte[] buffer = new byte[bufferSize];
        //int len = 0;
        //while ((len = inputStream.read(buffer)) != -1) {
            output.write(buffer, 0, len);
        //}
*/

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

}

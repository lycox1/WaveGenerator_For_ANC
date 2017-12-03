package com.e4deen.crosstalkanc;

import android.Manifest;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import java.io.IOException;
import java.util.Arrays;

import com.e4deen.crosstalkanc.AudioStreamPlayer.State;

import static java.lang.Math.exp;
import static java.lang.Math.log10;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    String LOG_TAG = "CrossTalkAnc_Main";
    FragmentManager fm;
    FragmentTransaction fragmentTransaction;
    Button btn_flag_wave, btn_flag_file_player;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btn_flag_wave = (Button) findViewById(R.id.btn_frag_wave);
        btn_flag_file_player = (Button) findViewById(R.id.btn_frag_file);

        btn_flag_wave.setOnClickListener(this);
        btn_flag_file_player.setOnClickListener(this);

        btn_flag_wave.setEnabled(false);
        btn_flag_file_player.setEnabled(true);

        fm = getFragmentManager();
        fragmentTransaction = fm.beginTransaction();
        fragmentTransaction.replace(R.id.layout_main, new FragmentWaveGenerator(this));
        fragmentTransaction.commit();
        fm.executePendingTransactions();

        checkPermissions();
    }

    @Override
    public void onClick(View v) {
        Log.e(LOG_TAG,"onClick()");

        fm = getFragmentManager();
        fragmentTransaction = fm.beginTransaction();

        switch (v.getId()) {

            case R.id.btn_frag_wave:
                Log.e(LOG_TAG,"onClick() btn_frag_wave");
                btn_flag_wave.setEnabled(false);
                btn_flag_file_player.setEnabled(true);
                fragmentTransaction.replace(R.id.layout_main, new FragmentWaveGenerator(this));
                fragmentTransaction.commit();
                fm.executePendingTransactions();
                break;

            case R.id.btn_frag_file:
                Log.e(LOG_TAG,"onClick() btn_frag_wave");
                btn_flag_wave.setEnabled(true);
                btn_flag_file_player.setEnabled(false);
                fragmentTransaction.replace(R.id.layout_main, new FragmentFilePlayer(this));
                fragmentTransaction.commit();
                fm.executePendingTransactions();
                break;
        }
    }


    void checkPermissions() {
        Log.d(LOG_TAG, "checkPermissions()");
        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.MODIFY_AUDIO_SETTINGS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.MODIFY_AUDIO_SETTINGS}, 1);
        } else {
            Log.d(LOG_TAG, "checkPermissions() MODIFY_AUDIO_SETTINGS else case ");
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, 1);
        } else {
            Log.d(LOG_TAG, "checkPermissions() RECORD_AUDIO else case ");
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
        } else {
            Log.d(LOG_TAG, "checkPermissions() READ_EXTERNAL_STORAGE else case ");
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        } else {
            Log.d(LOG_TAG, "checkPermissions() WRITE_EXTERNAL_STORAGE else case ");
        }
    }

}


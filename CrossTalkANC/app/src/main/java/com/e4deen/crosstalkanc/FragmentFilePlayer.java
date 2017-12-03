package com.e4deen.crosstalkanc;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import java.io.IOException;

/**
 * Created by user on 2017-11-27.
 */

public class FragmentFilePlayer extends Fragment implements View.OnClickListener, OnAudioStreamInterface {

    String LOG_TAG = "CrossTalkAnc_FragFilePlayer";
    Button btn_start_file, btn_stop_file;
    AudioStreamPlayer mAudioPlayer = null;
    Context mContext;
    Activity mActivity;

    public FragmentFilePlayer(Context context) {
        Log.d(LOG_TAG, "Constructor FragmentFilePlayer");
        mContext = context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_file_player, container, false);

        btn_start_file = rootView.findViewById(R.id.btn_start_file);
        btn_stop_file = rootView.findViewById(R.id.btn_stop_file);

        btn_start_file.setOnClickListener(this);
        btn_stop_file.setOnClickListener(this);

        btn_start_file.setEnabled(true);
        btn_stop_file.setEnabled(false);

        btn_start_file.setOnClickListener(this);
        btn_stop_file.setOnClickListener(this);

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        mActivity = getActivity();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        releaseAudioPlayer();
    }

    @Override
    public void onClick(View v) {
        Log.e(LOG_TAG,"onClick()");

        switch (v.getId()) {

            case R.id.btn_start_file:
                Log.e(LOG_TAG,"onClick() playerPlay");
                btn_start_file.setEnabled(false);
                btn_stop_file.setEnabled(true);
                playerPlay();
                break;

            case R.id.btn_stop_file:
                Log.e(LOG_TAG,"onClick() playerStop");
                btn_start_file.setEnabled(true);
                btn_stop_file.setEnabled(false);
                playerStop();
                break;
        }
    }



    private void playerPause()
    {
        Log.d(LOG_TAG, "pause()");

        if (this.mAudioPlayer != null)
        {
            this.mAudioPlayer.pause();
        }
    }

    private void releaseAudioPlayer()
    {
        Log.d(LOG_TAG, "releaseAudioPlayer()");
        if (mAudioPlayer != null)
        {
            mAudioPlayer.stop();
            mAudioPlayer.release();
            mAudioPlayer = null;

        }
    }

    private void playerPlay()
    {
        Log.d(LOG_TAG, "play()");
        releaseAudioPlayer();

        mAudioPlayer = new AudioStreamPlayer();
        mAudioPlayer.setOnAudioStreamInterface(this);

//        String fileName = Environment.getExternalStorageDirectory().getAbsolutePath() + "/1000hz_441k_16bit_minus_20db.mp3";
        String fileName = Environment.getExternalStorageDirectory().getAbsolutePath() + "/mp3_07.mp3";

        mAudioPlayer.setUrlString(fileName);


        /*
        //----------- test -------------------------
        for(int t = 0; t < 100; t++) {
            chunk[t] = (byte)(t + 1);
        }
        applyGain(chunk);

        for(int i =0; i< 100; i++) {

              Log.d(LOG_TAG, "buffer() i = " + i + ", buffer[i*2] = " + chunk[i] );
        }
        //----------- test -------------------------
        */
        try
        {
            mAudioPlayer.play();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    /*
    byte[] chunk = new byte[100];

    public void applyGain(byte[] buffer) {
        //short[] convertChunk;

        for(int i =0; i<buffer.length; i++) {
            Log.d(LOG_TAG, "applyGain() i = " + i + ", buffer[i] = " + buffer[i] );
        }
        Log.d(LOG_TAG, "applyGain() buffer size " + buffer.length );

        short[] convertChunk = new short[buffer.length/2];

        for(int i =0; i<buffer.length/2; i++) {
            convertChunk[i] = (short)((short)buffer[i*2] + (short)(buffer[i*2 +1] << 8 ));
         //   Log.d(LOG_TAG, "applyGain() i = " + i + ", buffer[i*2] = " + (short)buffer[i*2] );
         //   Log.d(LOG_TAG, "applyGain() i = " + i + ", buffer[i*2 +1] << 8 = " + (buffer[i*2 +1] << 8) );
         //   Log.d(LOG_TAG, "applyGain() i = " + i + ", convertChunk[i] = " + convertChunk[i] );

        }

        for(int i =0; i<buffer.length/2; i++) {
            buffer[i*2] = (byte)((byte) (convertChunk[i] & 0x00ff) + 1);
            buffer[i*2 +1] = (byte)((byte)((convertChunk[i] & 0xff00) >>> 8) + 1);

         //   Log.d(LOG_TAG, "buffer() i = " + i + ", buffer[i*2] = " + (short)buffer[i*2] );
         //   Log.d(LOG_TAG, "buffer() i + = " + i + 1 + ", buffer[i*2 +1] " + buffer[i*2 +1] );
         //   Log.d(LOG_TAG, "applyGain() i = " + i + ", convertChunk[i] = " + convertChunk[i] );
        }
    }
    */

    private void playerReleaseAudioPlayer()
    {
        Log.d(LOG_TAG, "releaseAudioPlayer()");
        if (mAudioPlayer != null)
        {
            mAudioPlayer.stop();
            mAudioPlayer.release();
            mAudioPlayer = null;

        }
    }

    private void playerStop()
    {
        Log.d(LOG_TAG, "stop()");
        if (this.mAudioPlayer != null)
        {
            this.mAudioPlayer.stop();
        }
    }

    @Override
    public void onAudioPlayerStart(AudioStreamPlayer player)
    {
        Log.d(LOG_TAG, "onAudioPlayerStart()");
        mActivity.runOnUiThread(new Runnable()
        {

            @Override
            public void run()
            {
                updatePlayer(AudioStreamPlayer.State.Playing);
            }
        });
    }

    @Override
    public void onAudioPlayerStop(AudioStreamPlayer player)
    {
        Log.d(LOG_TAG, "onAudioPlayerStop()");
        mActivity.runOnUiThread(new Runnable()
        {

            @Override
            public void run()
            {
                updatePlayer(AudioStreamPlayer.State.Stopped);
            }
        });

    }

    @Override
    public void onAudioPlayerError(AudioStreamPlayer player)
    {
        Log.d(LOG_TAG, "onAudioPlayerError()");
        mActivity.runOnUiThread(new Runnable()
        {

            @Override
            public void run()
            {
                updatePlayer(AudioStreamPlayer.State.Stopped);
            }
        });

    }

    @Override
    public void onAudioPlayerBuffering(AudioStreamPlayer player)
    {
        Log.d(LOG_TAG, "onAudioPlayerBuffering()");
        mActivity.runOnUiThread(new Runnable()
        {

            @Override
            public void run()
            {
                updatePlayer(AudioStreamPlayer.State.Buffering);
            }
        });

    }

    @Override
    public void onAudioPlayerDuration(final int totalSec)
    {

        Log.d(LOG_TAG, "onAudioPlayerDuration()");
        /*
        mActivity.runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                if (totalSec > 0)
                {
                    int min = totalSec / 60;
                    int sec = totalSec % 60;

                    mTextDuration.setText(String.format("%02d:%02d", min, sec));

                    mSeekProgress.setMax(totalSec);
                }
            }

        });
        */
    }

    @Override
    public void onAudioPlayerCurrentTime(final int sec)
    {
/*
        Log.d(LOG_TAG, "onAudioPlayerCurrentTime()");
        mActivity.runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                if (!isSeekBarTouch)
                {
                    int m = sec / 60;
                    int s = sec % 60;

                    mTextCurrentTime.setText(String.format("%02d:%02d", m, s));

                    mSeekProgress.setProgress(sec);
                }
            }
        });
*/
    }

    @Override
    public void onAudioPlayerPause(AudioStreamPlayer player)
    {
        /*
        Log.d(LOG_TAG, "onAudioPlayerPause()");
        mActivity.runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                mPlayButton.setText("Play");
            }
        });
        */
    }


    private void updatePlayer(AudioStreamPlayer.State state)
    {
        Log.d(LOG_TAG, "updatePlayer() state " + state);

        switch (state)
        {
            case Stopped:
            {
                /*
                if (mProgressDialog != null)
                {
                    mProgressDialog.cancel();
                    mProgressDialog.dismiss();

                    mProgressDialog = null;
                }
                mPlayButton.setSelected(false);
                mPlayButton.setText("Play");

                mTextCurrentTime.setText("00:00");
                mTextDuration.setText("00:00");

                mSeekProgress.setMax(0);
                mSeekProgress.setProgress(0);
                */
                break;
            }
            case Prepare:
            case Buffering:
            {
                /*
                if (mProgressDialog == null)
                {
                    mProgressDialog = new ProgressDialog(this);
                }
                mProgressDialog.show();

                mPlayButton.setSelected(false);
                mPlayButton.setText("Play");

                mTextCurrentTime.setText("00:00");
                mTextDuration.setText("00:00");
                */
                break;
            }
            case Pause:
            {
                break;
            }
            case Playing:
            { /*
                if (mProgressDialog != null)
                {
                    mProgressDialog.cancel();
                    mProgressDialog.dismiss();

                    mProgressDialog = null;
                }
                mPlayButton.setSelected(true);
                mPlayButton.setText("Pause");
                break;
                */
            }
        }
    }

}

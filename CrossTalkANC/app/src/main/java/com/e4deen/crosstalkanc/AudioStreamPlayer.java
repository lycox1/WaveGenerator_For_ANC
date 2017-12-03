package com.e4deen.crosstalkanc;

/**
 * Created by sangwon4.lee on 2017-11-24.
 */

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import static java.lang.Math.exp;
import static java.lang.Math.log10;

public class AudioStreamPlayer
{
    private static final String LOG_TAG = "CrossTalkAnc_AudioStreamPlayer";

    private MediaExtractor mExtractor = null;
    private MediaCodec mMediaCodec = null;
    private AudioTrack mAudioTrack = null;

    double mGainLfloating, mGainRfloating, mGainLdB, mGainRdB = 0;
    private int mInputBufIndex = 0;

    private boolean isForceStop = false;
    private volatile boolean isPause = false;

    protected OnAudioStreamInterface mListener = null;

    public void setOnAudioStreamInterface(OnAudioStreamInterface listener)
    {
        this.mListener = listener;
    }

    public enum State
    {
        Stopped, Prepare, Buffering, Playing, Pause
    };

    State mState = State.Stopped;

    public State getState()
    {
        return mState;
    }

    private String mMediaPath;

    public void setUrlString(String mUrlString)
    {
        this.mMediaPath = mUrlString;
    }

    public AudioStreamPlayer()
    {
        mState = State.Stopped;
    }

    public void play() throws IOException
    {
        mState = State.Prepare;
        isForceStop = false;

        mAudioPlayerHandler.onAudioPlayerBuffering(AudioStreamPlayer.this);

        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                decodeLoop();
            }
        }).start();
    }

    private DelegateHandler mAudioPlayerHandler = new DelegateHandler();

    class DelegateHandler extends Handler
    {
        @Override
        public void handleMessage(Message msg)
        {
        }

        public void onAudioPlayerPlayerStart(AudioStreamPlayer player)
        {
            if (mListener != null)
            {
                mListener.onAudioPlayerStart(player);
            }
        }

        public void onAudioPlayerStop(AudioStreamPlayer player)
        {
            if (mListener != null)
            {
                mListener.onAudioPlayerStop(player);
            }
        }

        public void onAudioPlayerError(AudioStreamPlayer player)
        {
            if (mListener != null)
            {
                mListener.onAudioPlayerError(player);
            }
        }

        public void onAudioPlayerBuffering(AudioStreamPlayer player)
        {
            if (mListener != null)
            {
                mListener.onAudioPlayerBuffering(player);
            }
        }

        public void onAudioPlayerDuration(int totalSec)
        {
            if (mListener != null)
            {
                mListener.onAudioPlayerDuration(totalSec);
            }
        }

        public void onAudioPlayerCurrentTime(int sec)
        {
            if (mListener != null)
            {
                mListener.onAudioPlayerCurrentTime(sec);
            }
        }

        public void onAudioPlayerPause()
        {
            if(mListener != null)
            {
                mListener.onAudioPlayerPause(AudioStreamPlayer.this);
            }
        }
    };

    private void decodeLoop()
    {
        Log.d(LOG_TAG, "decodeLoop()");
        ByteBuffer[] codecInputBuffers;
        ByteBuffer[] codecOutputBuffers;

        mExtractor = new MediaExtractor();
        try
        {
            Log.d(LOG_TAG, "decodeLoop() mMediaPath : " + mMediaPath);
            mExtractor.setDataSource(this.mMediaPath);
        }
        catch (Exception e)
        {
            mAudioPlayerHandler.onAudioPlayerError(AudioStreamPlayer.this);
            return;
        }

        MediaFormat format = mExtractor.getTrackFormat(0);
        String mime = format.getString(MediaFormat.KEY_MIME);
        long duration = format.getLong(MediaFormat.KEY_DURATION);
        int totalSec = (int) (duration / 1000 / 1000);
        int min = totalSec / 60;
        int sec = totalSec % 60;

        mAudioPlayerHandler.onAudioPlayerDuration(totalSec);

        Log.d(LOG_TAG, "decodeLoop() Time = " + min + " : " + sec);
        Log.d(LOG_TAG, "decodeLoop() Duration = " + duration);

        try {
            mMediaCodec = MediaCodec.createDecoderByType(mime);
        } catch (IOException e) {
            e.printStackTrace();
        }
        mMediaCodec.configure(format, null, null, 0);
        mMediaCodec.start();
        codecInputBuffers = mMediaCodec.getInputBuffers();
        codecOutputBuffers = mMediaCodec.getOutputBuffers();

        int sampleRate = format.getInteger(MediaFormat.KEY_SAMPLE_RATE);

        Log.i(LOG_TAG, "decodeLoop() mime " + mime);
        Log.i(LOG_TAG, "decodeLoop() sampleRate " + sampleRate);

        mAudioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, sampleRate, AudioFormat.CHANNEL_OUT_STEREO,
                AudioFormat.ENCODING_PCM_16BIT, AudioTrack.getMinBufferSize(sampleRate, AudioFormat.CHANNEL_OUT_STEREO,
                AudioFormat.ENCODING_PCM_16BIT), AudioTrack.MODE_STREAM);

        mAudioTrack.play();
        mExtractor.selectTrack(0);

        final long kTimeOutUs = 10000;
        MediaCodec.BufferInfo info = new MediaCodec.BufferInfo();
        boolean sawInputEOS = false;
        int noOutputCounter = 0;
        int noOutputCounterLimit = 50;

        while (!sawInputEOS && noOutputCounter < noOutputCounterLimit && !isForceStop)
        {
            if (!sawInputEOS)
            {
                if(isPause)
                {
                    if(mState != State.Pause)
                    {
                        mState = State.Pause;

                        mAudioPlayerHandler.onAudioPlayerPause();
                    }
                    continue;
                }
                noOutputCounter++;
                if (isSeek)
                {
                    mExtractor.seekTo(seekTime * 1000 * 1000, MediaExtractor.SEEK_TO_CLOSEST_SYNC);
                    isSeek = false;
                }

                mInputBufIndex = mMediaCodec.dequeueInputBuffer(kTimeOutUs);
                if (mInputBufIndex >= 0)
                {
                    ByteBuffer dstBuf = codecInputBuffers[mInputBufIndex];

                    int sampleSize = mExtractor.readSampleData(dstBuf, 0);

                    long presentationTimeUs = 0;

                    if (sampleSize < 0)
                    {
                        Log.d(LOG_TAG, "saw input EOS.");
                        sawInputEOS = true;
                        sampleSize = 0;
                    }
                    else
                    {
                        presentationTimeUs = mExtractor.getSampleTime();

                        Log.d(LOG_TAG, "presentaionTime = " + (int) (presentationTimeUs / 1000 / 1000));

                        mAudioPlayerHandler.onAudioPlayerCurrentTime((int) (presentationTimeUs / 1000 / 1000));
                    }

                    mMediaCodec.queueInputBuffer(mInputBufIndex, 0, sampleSize, presentationTimeUs,
                            sawInputEOS ? MediaCodec.BUFFER_FLAG_END_OF_STREAM : 0);

                    if (!sawInputEOS)
                    {
                        mExtractor.advance();
                    }
                }
                else
                {
                    Log.e(LOG_TAG, "inputBufIndex " + mInputBufIndex);
                }
            }

            int res = mMediaCodec.dequeueOutputBuffer(info, kTimeOutUs);

            if (res >= 0)
            {
                if (info.size > 0)
                {
                    noOutputCounter = 0;
                }

                int outputBufIndex = res;
                ByteBuffer buf = codecOutputBuffers[outputBufIndex];

                byte[] chunk = new byte[info.size];
                buf.get(chunk);
                buf.clear();
                if (chunk.length > 0)
                {
                    applyGain(chunk);
                    mAudioTrack.write(chunk, 0, chunk.length);
                    if (this.mState != State.Playing)
                    {
                        mAudioPlayerHandler.onAudioPlayerPlayerStart(AudioStreamPlayer.this);
                    }
                    this.mState = State.Playing;
                }
                mMediaCodec.releaseOutputBuffer(outputBufIndex, false);
            }
            else if (res == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED)
            {
                codecOutputBuffers = mMediaCodec.getOutputBuffers();

                Log.d(LOG_TAG, "output buffers have changed.");
            }
            else if (res == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED)
            {
                MediaFormat oformat = mMediaCodec.getOutputFormat();

                Log.d(LOG_TAG, "output format has changed to " + oformat);
            }
            else
            {
                Log.d(LOG_TAG, "dequeueOutputBuffer returned " + res);
            }
        }

        Log.d(LOG_TAG, "stopping...");

        releaseResources(true);

        this.mState = State.Stopped;
        isForceStop = true;

        if (noOutputCounter >= noOutputCounterLimit)
        {
            mAudioPlayerHandler.onAudioPlayerError(AudioStreamPlayer.this);
        }
        else
        {
            mAudioPlayerHandler.onAudioPlayerStop(AudioStreamPlayer.this);
        }
    }

    int test_count = 0;
    String ori_path = "/sdcard/Test/ori.pcm";
    String mod_path = "/sdcard/Test/mod.pcm";

    public void applyGain(byte[] buffer) {
        //short[] convertChunk;

        Log.e(LOG_TAG, "applyGain() buffer size " + buffer.length );

        //mGainLfloating = 1;
        //mGainRfloating = 0.5;
        mGainLdB = -50;
        mGainRdB = 0;

        short[] convertChunk = new short[buffer.length / 2];
        double temp;
        for (int i = 0; i < buffer.length / 2; i++) {
            short low = (short) buffer[i *2];
            short high = (short)(( ((short) buffer[i *2 +1]) << 8) & 0xff00);
            //convertChunk[i] = (short) (buffer[i * 2] | (((short)buffer[i * 2 + 1] << 8) & 0xff00));
            convertChunk[i] = (short)((low & 0x00ff) | (high & 0xff00));

            mGainLfloating = dB2amp(mGainLdB);
            mGainRfloating = dB2amp(mGainRdB);

            // need to work for gain apply
            //convertChunk[i] = convertChunk[i] * ((double)32767 * mGainLfloating);

/*          // Just need for debug
            if (i < 10) {
                Log.e(LOG_TAG, "applyGain() i = " + i + ", buffer[i*2] = " + (short) buffer[i * 2]);
                Log.e(LOG_TAG, "applyGain() i = " + i + ", buffer[i*2 +1] = " + (short) buffer[i * 2 + 1]);
                Log.e(LOG_TAG, "applyGain() i = " + i + ", buffer[i*2 +1] << 8 = " + (((short)buffer[i * 2 + 1] << 8) & 0xff00));
                Log.e(LOG_TAG, "applyGain() i = " + i + ", convertChunk[i] = " + convertChunk[i]);
            }
*/
        }

        for (int i = 0; i < buffer.length / 2; i++) {
            buffer[i * 2] = (byte) ((byte) (convertChunk[i] & 0x00ff));
            buffer[i * 2 + 1] = (byte) ((byte) ((convertChunk[i] & 0xff00) >>> 8));
            //buffer[i * 2 + 1] = (byte) ((byte) ((convertChunk[i] & 0xff00) >> 8));

/*            // Just need for debug
            if (i < 10) {
                Log.e(LOG_TAG, "buffer() i = " + i + ", buffer[i*2] = " + buffer[i * 2]);
                Log.e(LOG_TAG, "buffer() i = " + i + ", buffer[i*2 +1] " + buffer[i * 2 + 1]);
                Log.e(LOG_TAG, "buffer() i = " + i + ", convertChunk[i] & 0xff00 " + (convertChunk[i] & 0xff00) );
            }
*/
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

    public void writeBufferToFile(String filePath ,byte[] buffer) {

        File file = new File(filePath);

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(file, true);

            for (int i = 0; i < buffer.length; i++) {
                fos.write(buffer[i]);
            }

            fos.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void release()
    {
        stop();
        releaseResources(false);
    }

    private void releaseResources(Boolean release)
    {
        if (mExtractor != null)
        {
            mExtractor.release();
            mExtractor = null;
        }

        if (mMediaCodec != null)
        {
            if (release)
            {
                mMediaCodec.stop();
                mMediaCodec.release();
                mMediaCodec = null;
            }

        }
        if (mAudioTrack != null)
        {
            mAudioTrack.flush();
            mAudioTrack.release();
            mAudioTrack = null;
        }
    }

    public void pause()
    {
        isPause = true;
    }

    public void stop()
    {
        isForceStop = true;
    }

    boolean isSeek = false;
    int seekTime = 0;

    public void seekTo(int progress)
    {
        isSeek = true;
        seekTime = progress;
    }

    public void pauseToPlay()
    {
        isPause = false;
    }

}

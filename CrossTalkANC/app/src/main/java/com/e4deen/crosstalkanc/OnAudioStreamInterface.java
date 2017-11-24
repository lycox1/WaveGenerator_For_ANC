package com.e4deen.crosstalkanc;

/**
 * Created by sangwon4.lee on 2017-11-24.
 */

public interface OnAudioStreamInterface
{
    public void onAudioPlayerStart(AudioStreamPlayer player);

    public void onAudioPlayerPause(AudioStreamPlayer player);

    public void onAudioPlayerStop(AudioStreamPlayer player);

    public void onAudioPlayerError(AudioStreamPlayer player);

    public void onAudioPlayerBuffering(AudioStreamPlayer player);

    public void onAudioPlayerDuration(int totalSec);

    public void onAudioPlayerCurrentTime(int sec);
}
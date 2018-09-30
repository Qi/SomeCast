package com.qi.somecastapp.service;

import android.support.v4.media.session.PlaybackStateCompat;

/**
 * Created by Qi Wu on 9/26/2018.
 */
public abstract class PlaybackInfoListener {

    public abstract void onPlaybackStateChange(PlaybackStateCompat state);

    public void onPlaybackCompleted() {
    }
}

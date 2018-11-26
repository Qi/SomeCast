package com.qi.somecastapp;

import android.view.View;

import com.qi.somecastapp.model.Episode;

/**
 * Created by Qi Wu on 9/24/2018.
 */
interface EpisodeClickListener {
    void onEpisodeClicked(Episode episode, View v);
}

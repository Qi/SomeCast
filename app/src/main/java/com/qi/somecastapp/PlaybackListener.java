package com.qi.somecastapp;

import com.qi.somecastapp.model.Episode;

/**
 * Created by Qi Wu on 9/24/2018.
 */
interface PlaybackListener {
    void onEpisodeClick(Episode episode);
}

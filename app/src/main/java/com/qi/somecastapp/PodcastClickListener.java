package com.qi.somecastapp;

import android.view.View;

import com.qi.somecastapp.model.Episode;

interface PodcastClickListener {
    void onPodcastClicked(String podcastJsonData);
    void onEpisodeClicked(Episode episode, View v);
}


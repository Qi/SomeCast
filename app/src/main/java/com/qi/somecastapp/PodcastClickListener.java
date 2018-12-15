package com.qi.somecastapp;

import android.view.View;

import com.qi.somecastapp.model.Episode;

import java.util.ArrayList;

interface PodcastClickListener {
    void onPodcastClicked(String podcastJsonData, boolean subscribed);
    void onEpisodeClicked(Episode episode, View v);
    void onEpisodeClicked(DownloadsFragment.Item episode, View v);
    void onEpisodeSet(ArrayList<Episode> data);
}


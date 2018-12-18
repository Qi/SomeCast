package com.qi.somecastapp;

import java.util.ArrayList;

interface PlaybackController {
    MediaServiceHelper getMediaServiceHelper();
    ArrayList<String> getDownloadedEpisodeList();
}

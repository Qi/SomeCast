package com.qi.somecastapp;

import android.Manifest;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.Volley;
import com.qi.somecastapp.database.SubscribedContract;
import com.qi.somecastapp.model.Episode;
import com.qi.somecastapp.model.Podcast;
import com.qi.somecastapp.service.DownloadService;
import com.qi.somecastapp.service.MediaPlaybackService;
import com.qi.somecastapp.utilities.JsonUtils;
import com.qi.somecastapp.utilities.NetworkUtils;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;


import java.util.ArrayList;
import java.util.List;

import static com.qi.somecastapp.utilities.SomePodcastAppConstants.KEY_EPISODE_META;

public class PodcastDetailActivity extends AppCompatActivity {


    private ImageButton skipBt;
    private ImageButton nextBt;
    private ImageButton replayBt;
    private ImageButton previousBt;
    private CustomSeekBar mSeekBarAudio;





    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_podcast_detail);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        skipBt = findViewById(R.id.skip_30_bt);
        nextBt = findViewById(R.id.next_bt);
        replayBt = findViewById(R.id.replay_10_bt);
        previousBt = findViewById(R.id.previous_bt);

    }










}

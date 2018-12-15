package com.qi.somecastapp;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.ImageButton;

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

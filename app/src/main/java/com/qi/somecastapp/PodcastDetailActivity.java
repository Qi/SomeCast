package com.qi.somecastapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.qi.somecastapp.model.Podcast;

import org.json.JSONException;
import org.json.JSONObject;

public class PodcastDetailActivity extends AppCompatActivity {

    private Podcast currentPodcast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_podcast_detail);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab_subscribe);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        Intent sourceIntent = getIntent();
        if (sourceIntent != null && sourceIntent.hasExtra(Intent.EXTRA_TEXT)) {
            try {
                currentPodcast = new Podcast(new JSONObject(sourceIntent.getStringExtra(Intent.EXTRA_TEXT)));
                setTitle(currentPodcast.getPodcastName());
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}

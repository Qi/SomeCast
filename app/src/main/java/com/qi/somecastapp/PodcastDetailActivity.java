package com.qi.somecastapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.Volley;
import com.qi.somecastapp.model.Podcast;
import com.qi.somecastapp.utilities.JsonUtils;
import com.qi.somecastapp.utilities.NetworkUtils;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

public class PodcastDetailActivity extends AppCompatActivity {

    private static final String TAG = PodcastDetailActivity.class.getSimpleName();
    private Podcast currentPodcast;
    private ImageView posterIv;
    private RequestQueue requestQueue;
    private EpisodeListAdapter episodeListAdapter;
    private RecyclerView episodeRv;

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
            posterIv = findViewById(R.id.iv_detail_poster);
            try {
                currentPodcast = new Podcast(new JSONObject(sourceIntent.getStringExtra(Intent.EXTRA_TEXT)));
                setTitle(currentPodcast.getPodcastName());
                Picasso.with(this).load(currentPodcast.getImagePath()).into(posterIv);
                requestQueue = Volley.newRequestQueue(this);
                LinearLayoutManager layoutManager = new LinearLayoutManager(this);
                layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
                layoutManager.setAutoMeasureEnabled(true);
                episodeRv = findViewById(R.id.rv_episode);
                episodeRv.setLayoutManager(layoutManager);
                episodeListAdapter = new EpisodeListAdapter();
                episodeRv.setAdapter(episodeListAdapter);

                Response.Listener<String> responseListener = new Response.Listener<String>(){
                    @Override
                    public void onResponse(String response) {
                        Log.d(TAG, response);
                        episodeListAdapter.setData(JsonUtils.parseEpisodes(response));
                    }
                };
                requestQueue.add(NetworkUtils.getPodcastMeta(currentPodcast.getId(), responseListener));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}

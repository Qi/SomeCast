package com.qi.somecastapp;

import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
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
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.Volley;
import com.qi.somecastapp.database.SubscribedContract;
import com.qi.somecastapp.model.Episode;
import com.qi.somecastapp.model.Podcast;
import com.qi.somecastapp.service.MediaPlaybackService;
import com.qi.somecastapp.utilities.JsonUtils;
import com.qi.somecastapp.utilities.NetworkUtils;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class PodcastDetailActivity extends AppCompatActivity implements PlaybackListener {

    private static final String TAG = PodcastDetailActivity.class.getSimpleName();
    private Podcast currentPodcast;
    private ImageView posterIv;
    private RequestQueue requestQueue;
    private EpisodeListAdapter episodeListAdapter;
    private RecyclerView episodeRv;
    private boolean subscribed;
    private MediaBrowserCompat mMediaBrowser;
    private MediaBrowserHelper mMediaBrowserHelper;
    private boolean mIsPlaying;


    private final MediaBrowserCompat.ConnectionCallback mConnectionCallbacks =
            new MediaBrowserCompat.ConnectionCallback() {
                @Override
                public void onConnected() {

                    // Get the token for the MediaSession
                    MediaSessionCompat.Token token = mMediaBrowser.getSessionToken();

                    // Create a MediaControllerCompat
                    MediaControllerCompat mediaController = null;
                    try {
                        mediaController = new MediaControllerCompat(PodcastDetailActivity.this, // Context
                                token);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }

                    // Save the controller
                    MediaControllerCompat.setMediaController(PodcastDetailActivity.this, mediaController);

                    // Finish building the UI
                    buildTransportControls();
                }

                @Override
                public void onConnectionSuspended() {
                    // The Service has crashed. Disable transport controls until it automatically reconnects
                }

                @Override
                public void onConnectionFailed() {
                    // The Service has refused our connection
                }
            };

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
                addFavorite();
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
                episodeListAdapter = new EpisodeListAdapter(this);
                episodeRv.setAdapter(episodeListAdapter);

                Response.Listener<String> responseListener = new Response.Listener<String>() {
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
        mMediaBrowser = new MediaBrowserCompat(this, new ComponentName(this, MediaPlaybackService.class), mConnectionCallbacks, null);
        mMediaBrowserHelper = new MediaBrowserConnection(this);
        mMediaBrowserHelper.registerCallback(new MediaBrowserListener());
    }

    private void addFavorite() {
        if (currentPodcast != null) {
            ContentValues contentValues = new ContentValues();
            contentValues.put(SubscribedContract.SubscribedEntry.COLUMN_PODCAST_ID, currentPodcast.getId());
            contentValues.put(SubscribedContract.SubscribedEntry.COLUMN_PODCAST_META, currentPodcast.getRawData());
            if (!subscribed) {
                Uri uri = getContentResolver().insert(SubscribedContract.SubscribedEntry.CONTENT_URI, contentValues);
                if (uri != null) {
                    Toast.makeText(getBaseContext(), currentPodcast.getPodcastName() + " subscribed.", Toast.LENGTH_SHORT).show();
                }

                subscribed = true;
            } else {
                Uri uri = SubscribedContract.SubscribedEntry.CONTENT_URI.buildUpon().appendPath(currentPodcast.getId()).build();
                int deleted = getContentResolver().delete(uri, null, null);
                if (deleted > 0) {
                    Toast.makeText(getBaseContext(), currentPodcast.getPodcastName() + " unsubscribed.", Toast.LENGTH_SHORT).show();
                }
                subscribed = false;
            }
//            updateFavImage();
        }
    }

    @Override
    public void onEpisodeClick(Episode episode) {

    }

    void buildTransportControls() {
//        // Grab the view for the play/pause button
//        mPlayPause = (ImageView) findViewById(R.id.play_pause);
//
//        // Attach a listener to the button
//        mPlayPause.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                // Since this is a play/pause button, you'll need to test the current state
//                // and choose the action accordingly
//
//                int pbState = MediaControllerCompat.getMediaController(PodcastDetailActivity.this).getPlaybackState().getState();
//                if (pbState == PlaybackStateCompat.STATE_PLAYING) {
//                    MediaControllerCompat.getMediaController(PodcastDetailActivity.this).getTransportControls().pause();
//                } else {
//                    MediaControllerCompat.getMediaController(PodcastDetailActivity.this).getTransportControls().play();
//                }
//            }
//        });

        MediaControllerCompat mediaController = MediaControllerCompat.getMediaController(PodcastDetailActivity.this);

        // Display the initial state
        MediaMetadataCompat metadata = mediaController.getMetadata();
        PlaybackStateCompat pbState = mediaController.getPlaybackState();

        // Register a Callback to stay in sync
        mediaController.registerCallback(controllerCallback);
    }

    private MediaControllerCompat.Callback controllerCallback =
            new MediaControllerCompat.Callback() {
                @Override
                public void onMetadataChanged(MediaMetadataCompat metadata) {
                }

                @Override
                public void onPlaybackStateChanged(PlaybackStateCompat state) {
                }
            };

    private class MediaBrowserConnection extends MediaBrowserHelper {
        public MediaBrowserConnection(Context context) {
            super(context, MediaPlaybackService.class);
        }

        @Override
        protected void onConnected(@NonNull MediaControllerCompat mediaController) {
//            mSeekBarAudio.setMediaController(mediaController);
        }

        @Override
        protected void onChildrenLoaded(@NonNull String parentId,
                                        @NonNull List<MediaBrowserCompat.MediaItem> children) {
            super.onChildrenLoaded(parentId, children);

            final MediaControllerCompat mediaController = getMediaController();

            // Queue up all media items for this simple sample.
            for (final MediaBrowserCompat.MediaItem mediaItem : children) {
                mediaController.addQueueItem(mediaItem.getDescription());
            }

            // Call prepare now so pressing play just works.
            mediaController.getTransportControls().prepare();
        }
    }

    private class MediaBrowserListener extends MediaControllerCompat.Callback {
        @Override
        public void onPlaybackStateChanged(PlaybackStateCompat playbackState) {
            mIsPlaying = playbackState != null &&
                    playbackState.getState() == PlaybackStateCompat.STATE_PLAYING;
//            mMediaControlsImage.setPressed(mIsPlaying);
        }

        @Override
        public void onMetadataChanged(MediaMetadataCompat mediaMetadata) {
            if (mediaMetadata == null) {
                return;
            }
//            mTitleTextView.setText(
//                    mediaMetadata.getString(MediaMetadataCompat.METADATA_KEY_TITLE));
//            mArtistTextView.setText(
//                    mediaMetadata.getString(MediaMetadataCompat.METADATA_KEY_ARTIST));
//            mAlbumArt.setImageBitmap(MusicLibrary.getAlbumBitmap(
//                    MainActivity.this,
//                    mediaMetadata.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID)));
        }

        @Override
        public void onSessionDestroyed() {
            super.onSessionDestroyed();
        }

        @Override
        public void onQueueChanged(List<MediaSessionCompat.QueueItem> queue) {
            super.onQueueChanged(queue);
        }
    }
}

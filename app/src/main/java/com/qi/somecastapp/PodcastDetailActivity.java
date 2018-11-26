package com.qi.somecastapp;

import android.Manifest;
import android.app.DownloadManager;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
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
import com.qi.somecastapp.service.MediaPlaybackService;
import com.qi.somecastapp.utilities.JsonUtils;
import com.qi.somecastapp.utilities.NetworkUtils;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class PodcastDetailActivity extends AppCompatActivity implements PlaybackListener{

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
    private ImageButton playPauseBt;
    private ImageButton skipBt;
    private ImageButton nextBt;
    private ImageButton replayBt;
    private ImageButton previousBt;
    private CustomSeekBar mSeekBarAudio;
    private ArrayList<Episode> episodes;
    private int nowPlayingIndex = 0;
    private DownloadManager downloadManager;
    ArrayList<Long> list = new ArrayList<>();
    private static final String CUSTOM_ACTION_REPLAY_TEN = "replay_10";
    private static final String CUSTOM_ACTION_FORWARD_THIRTY = "forward_30";
    private static final String CHANNEL_ID = "com.qi.somecastapp.download.channel";


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

                    // Register a Callback to stay in sync
                    mediaController.registerCallback(controllerCallback);                }

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
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(onComplete);
    }

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

        playPauseBt = findViewById(R.id.play_pause_bt);
        skipBt = findViewById(R.id.skip_30_bt);
        nextBt = findViewById(R.id.next_bt);
        replayBt = findViewById(R.id.replay_10_bt);
        previousBt = findViewById(R.id.previous_bt);

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
                        episodes = JsonUtils.parseEpisodes(response);
                        episodeListAdapter.setData(episodes);
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
        downloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
        registerReceiver(onComplete,
                new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
    }

    @Override
    public void onStart() {
        super.onStart();
        mMediaBrowserHelper.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
//        mSeekBarAudio.disconnectController();
        mMediaBrowserHelper.onStop();
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
    public void onEpisodeClicked(Episode episode, View v) {
        switch (v.getId()) {
            case R.id.tv_episode_title:
                nowPlayingIndex = episodes.indexOf(episode);
                mMediaBrowserHelper.getTransportControls().playFromUri(Uri.parse(episode.getAudioPath()), null);
                break;
            case R.id.bt_download:
                int targetIndex = episodes.indexOf(episode);
                if (haveStoragePermission(targetIndex)) {
                    startsDownload(targetIndex);
                }
                break;
        }
    }

    private boolean haveStoragePermission(int targetIndex) {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                Log.e(TAG,"Have permission");
                return true;
            } else {
                Log.e(TAG,"Asking for permission");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, targetIndex);
                return false;
            }
        }
        else {
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(grantResults[0]== PackageManager.PERMISSION_GRANTED){
            startsDownload(requestCode);
        }
    }

    private void startsDownload(int targetIndex){
        Uri audioPath =Uri.parse(episodes.get(targetIndex).getAudioPath());
        DownloadManager.Request request = new DownloadManager.Request(audioPath);
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE);
        request.setAllowedOverRoaming(false);
        request.setTitle(episodes.get(targetIndex).getTitle() + ".mp3");
        request.setDescription(episodes.get(targetIndex).getDescription());
        request.setVisibleInDownloadsUi(true);
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "/SomeCastApp/");
        long refId = downloadManager.enqueue(request);
        list.add(refId);
    }

    void buildTransportControls() {
//        // Grab the view for the play/pause button
//        mPlayPause = (ImageView) findViewById(R.id.play_pause)
        //TODO: Fix buildTransportControls()
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
                    int a = 1;
                }
            };

    public void requestPreviousTrack(View view) {
        int targetIndex = nowPlayingIndex == 0 ? nowPlayingIndex : nowPlayingIndex - 1 ;
        mMediaBrowserHelper.getTransportControls().playFromUri(Uri.parse(episodes.get(targetIndex).getAudioPath()), null);
        nowPlayingIndex = targetIndex;
    }

    public void replayTen(View view) {
        mMediaBrowserHelper.getTransportControls().sendCustomAction(CUSTOM_ACTION_REPLAY_TEN, null);
    }

    public void playPause(View view) {
        if (mIsPlaying) {
            mMediaBrowserHelper.getTransportControls().pause();
        } else {
            mMediaBrowserHelper.getTransportControls().play();
        }
    }

    public void skipThirty(View view) {
        mMediaBrowserHelper.getTransportControls().sendCustomAction(CUSTOM_ACTION_FORWARD_THIRTY, null);
    }

    public void requestNextTrack(View view) {
        if (nowPlayingIndex == episodes.size() - 1) {
            mMediaBrowserHelper.getTransportControls().stop();
        } else {
            mMediaBrowserHelper.getTransportControls().playFromUri(Uri.parse(episodes.get(nowPlayingIndex + 1).getAudioPath()), null);
            nowPlayingIndex = nowPlayingIndex + 1;
        }
    }

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
            if (playPauseBt != null)
                playPauseBt.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(),
                        mIsPlaying?R.drawable.ic_baseline_pause_24px:R.drawable.ic_baseline_play_arrow_24px));
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

    BroadcastReceiver onComplete = new BroadcastReceiver() {

        public void onReceive(Context ctxt, Intent intent) {

            // get the refid from the download manager
            long referenceId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);

            list.remove(referenceId);

            if (list.isEmpty())
            {
                Log.e("INSIDE", "" + referenceId);
                NotificationCompat.Builder mBuilder =
                        new NotificationCompat.Builder(PodcastDetailActivity.this, CHANNEL_ID)
                                .setSmallIcon(R.mipmap.ic_launcher)
                                .setContentTitle("GadgetSaint")
                                .setContentText("All Download completed");

                NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                notificationManager.notify(455, mBuilder.build());
            }

        }
    };
}

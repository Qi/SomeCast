package com.qi.somecastapp;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;

import com.qi.somecastapp.model.Episode;
import com.qi.somecastapp.service.DownloadService;
import com.qi.somecastapp.service.MyPodcastMediaService;

import java.util.ArrayList;
import java.util.List;

import static android.support.v4.media.MediaBrowserCompat.MediaItem.FLAG_PLAYABLE;
import static com.qi.somecastapp.utilities.SomePodcastAppConstants.KEY_EPISODE_META;

public class MainActivity extends AppCompatActivity implements PodcastClickListener, PlaybackController {

    private static final String TAG = MainActivity.class.getName();
    private static final String CUSTOM_ACTION_REPLAY_TEN = "replay_10";
    private static final String CUSTOM_ACTION_FORWARD_THIRTY = "forward_30";
    private static final int INITIAL_CHECK = -1;
    private Toolbar toolbar;
    private BottomNavigationView navigation;
    //    private MediaBrowserCompat mMediaBrowser;
    private MediaServiceHelper mMediaServiceHelper;
    private boolean mIsPlaying;
    private ImageButton playPauseBt;
    private Fragment mCurrentFragment;
    private List<MediaBrowserCompat.MediaItem> cachedChildren;
    private String cachedParentId;
    private ArrayList<Episode> episodes;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    //TODO: change icons
                    case R.id.navigation_home:
                        if (mCurrentFragment instanceof SubscriptionFragment)
                            return false;
                        toolbar.setTitle(R.string.title_home);
                        mCurrentFragment = new SubscriptionFragment();
                        loadFragment(mCurrentFragment);
                        return true;
                    case R.id.navigation_discover:
                        if (mCurrentFragment instanceof DiscoverFragment)
                            return false;
                        toolbar.setTitle(R.string.title_discover);
                        mCurrentFragment = new DiscoverFragment();
                        loadFragment(mCurrentFragment);
                        return true;
                    case R.id.navigation_downloads:
                        if (mCurrentFragment instanceof DownloadsFragment)
                            return false;
                        toolbar.setTitle(R.string.title_downloads);
                        if (haveStoragePermission(0)) {
                            mCurrentFragment = new DownloadsFragment();
                            loadFragment(mCurrentFragment);
                            return true;
                        }
                        return false;
                }

            return false;
        }
    };

//    private final MediaBrowserCompat.ConnectionCallback mConnectionCallbacks =
//            new MediaBrowserCompat.ConnectionCallback() {
//                @Override
//                public void onConnected() {
//
//                    // Get the token for the MediaSession
//                    MediaSessionCompat.Token token = mMediaBrowser.getSessionToken();
//
//                    // Create a MediaControllerCompat
//                    MediaControllerCompat mediaController = null;
//                    try {
//                        mediaController = new MediaControllerCompat(MainActivity.this, // Context
//                                token);
//                    } catch (RemoteException e) {
//                        e.printStackTrace();
//                    }
//
//                    // Save the controller
//                    MediaControllerCompat.setMediaController(MainActivity.this, mediaController);
//
//                    // Register a Callback to stay in sync
//                    mediaController.registerCallback(controllerCallback);                }
//
//                @Override
//                public void onConnectionSuspended() {
//                    // The Service has crashed. Disable transport controls until it automatically reconnects
//                }
//
//                @Override
//                public void onConnectionFailed() {
//                    // The Service has refused our connection
//                }
//            };

    private void loadFragment(Fragment fragment) {
        if (fragment instanceof DownloadsFragment) {
            if (cachedChildren != null) {
                ((DownloadsFragment) fragment).setFragmentData(cachedParentId, cachedChildren);
            }
        }
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.frame_container, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        toolbar = findViewById(R.id.toolbar);
        playPauseBt = findViewById(R.id.play_pause_bt);
        setSupportActionBar(toolbar);
        navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        mOnNavigationItemSelectedListener.onNavigationItemSelected(navigation.getMenu().getItem(0));
//        mMediaBrowser = new MediaBrowserCompat(this, new ComponentName(this, MyPodcastMediaService.class), mConnectionCallbacks, null);
        mMediaServiceHelper = new MainActivity.MediaBrowserConnection(this);
        mMediaServiceHelper.registerCallback(new MainActivity.MediaBrowserListener());
    }

    private boolean haveStoragePermission(int id) {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "Have permission");
                return true;
            } else {
                Log.d(TAG, "Asking for permission");
                if (id != INITIAL_CHECK)
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, id);
                return false;
            }
        } else {
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (requestCode == 0) {
                mOnNavigationItemSelectedListener.onNavigationItemSelected(navigation.getMenu().getItem(2));
            } else {
                //1 based index was used for targetIndex because 0 is taken by other purpose
                startsDownload(requestCode - 1);
            }
        } else {
            mOnNavigationItemSelectedListener.onNavigationItemSelected(navigation.getMenu().getItem(0));
        }
    }

    @Override
    public void onPodcastClicked(String podcastJsonData, boolean subscribed) {
        mCurrentFragment = PodcastDetailFragment.newInstance(podcastJsonData, subscribed);
        getSupportActionBar().hide();
        loadFragment(mCurrentFragment);
    }

    @Override
    public void onEpisodeClicked(Episode episode, View v) {
        switch (v.getId()) {
            case R.id.tv_episode_title:
                mMediaServiceHelper.playOnlineContent(episode);
                break;
            case R.id.bt_download:
                int targetIndex = episodes.indexOf(episode);
                //use 1 based index for targetIndex when requesting permission because 0 is taken by other purpose
                if (haveStoragePermission(targetIndex + 1)) {
                    startsDownload(targetIndex);
                }
                break;
        }
    }

    @Override
    public void onEpisodeClicked(DownloadsFragment.Item episode, View v) {

        if (episode.media.getFlags() != FLAG_PLAYABLE) {
            mMediaServiceHelper.subscribeNewRoot(episode.media.getMediaId());
        } else {
            mMediaServiceHelper.playLocalContent(episode);
        }

    }

    @Override
    public void onEpisodeSet(ArrayList<Episode> data) {
        episodes = data;
        mMediaServiceHelper.setOnlinePlaylist(episodes);
    }

    private void startsDownload(int targetIndex) {
        Intent intent = new Intent(this, DownloadService.class);
        intent.putExtra(KEY_EPISODE_META, episodes.get(targetIndex));
        startService(intent);
    }

    public void requestPreviousTrack(View view) {
        mMediaServiceHelper.requestPreviousTrack();
    }

    public void replayTen(View view) {
        mMediaServiceHelper.getTransportControls().sendCustomAction(CUSTOM_ACTION_REPLAY_TEN, null);
    }

    public void playPause(View view) {
        if (mIsPlaying) {
            mMediaServiceHelper.getTransportControls().pause();
        } else {
            mMediaServiceHelper.getTransportControls().play();
        }
    }

    public void skipThirty(View view) {
        mMediaServiceHelper.getTransportControls().sendCustomAction(CUSTOM_ACTION_FORWARD_THIRTY, null);
    }

    public void requestNextTrack(View view) {
        mMediaServiceHelper.requestNextTrack();
    }

    @Override
    public MediaServiceHelper getMediaServiceHelper() {
        return mMediaServiceHelper;
    }

    private class MediaBrowserConnection extends MediaServiceHelper {
        public MediaBrowserConnection(Context context) {
            super(context, MyPodcastMediaService.class, haveStoragePermission(INITIAL_CHECK));
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
            // Call prepare now so pressing play just works.
            mediaController.getTransportControls().prepare();
            cachedParentId = parentId;
            cachedChildren = children;

        }
    }

    private class MediaBrowserListener extends MediaControllerCompat.Callback {
        @Override
        public void onPlaybackStateChanged(PlaybackStateCompat playbackState) {
            mIsPlaying = playbackState != null &&
                    playbackState.getState() == PlaybackStateCompat.STATE_PLAYING;
            if (playPauseBt != null)
                playPauseBt.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(),
                        mIsPlaying ? R.drawable.ic_baseline_pause_24px : R.drawable.ic_baseline_play_arrow_24px));
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

    @Override
    public void onStart() {
        super.onStart();
        mMediaServiceHelper.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
//        mSeekBarAudio.disconnectController();
        mMediaServiceHelper.onStop();
    }

    void buildTransportControls() {
//        // Grab the view for the play/pause button
//        mPlayPause = (ImageView) findViewById(R.id.play_pause)
        //TODO: Fix buildTransportControls()
        MediaControllerCompat mediaController = MediaControllerCompat.getMediaController(MainActivity.this);

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

    @Override
    public void onBackPressed() {
        if (mCurrentFragment instanceof PodcastDetailFragment) super.onBackPressed();
        else finish();
    }
}

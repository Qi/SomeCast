package com.qi.somecastapp.service;

import android.app.Notification;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaBrowserServiceCompat;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;

import com.qi.somecastapp.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Qi Wu on 9/24/2018.
 */
public class MediaPlaybackService extends MediaBrowserServiceCompat {
    private static final String MY_MEDIA_ROOT_ID = "media_root_id";
    private static final String MY_EMPTY_MEDIA_ROOT_ID = "empty_root_id";
    private static final String LOG_TAG = "MediaPlaybackService";
    private static final String TAG = MediaPlaybackService.class.getSimpleName();
    private static final String CUSTOM_ACTION_REPLAY_TEN = "replay_10";
    private static final String CUSTOM_ACTION_FORWARD_THIRTY = "forward_30";

    private MediaSessionCompat mMediaSession;
    private PlaybackStateCompat.Builder mStateBuilder;
    private PlayerAdapter mPlayback;
    private boolean mServiceInStartedState;
    private MediaNotificationManager mMediaNotificationManager;

    @Override
    public void onCreate() {
        super.onCreate();

        // Create a MediaSessionCompat
        mMediaSession = new MediaSessionCompat(this, LOG_TAG);

        // Enable callbacks from MediaButtons and TransportControls
        mMediaSession.setFlags(
                MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS |
                        MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);

        // Set an initial PlaybackState with ACTION_PLAY, so media buttons can start the player
        mStateBuilder = new PlaybackStateCompat.Builder()
                .setActions(
                        PlaybackStateCompat.ACTION_PLAY |
                                PlaybackStateCompat.ACTION_PLAY_PAUSE);
        mStateBuilder.addCustomAction(new PlaybackStateCompat.CustomAction.Builder(
                CUSTOM_ACTION_REPLAY_TEN, "Replay 10", R.drawable.ic_baseline_replay_10_24px)
                .setExtras(null)
                .build());
        mStateBuilder.addCustomAction(new PlaybackStateCompat.CustomAction.Builder(
                CUSTOM_ACTION_FORWARD_THIRTY, "Forward 30", R.drawable.ic_baseline_forward_30_24px)
                .setExtras(null)
                .build());
        mMediaSession.setPlaybackState(mStateBuilder.build());

        // MySessionCallback() has methods that handle callbacks from a media controller
        mMediaSession.setCallback(new MySessionCallback());

        // Set the session's token so that client activities can communicate with it.
        setSessionToken(mMediaSession.getSessionToken());
        mMediaNotificationManager = new MediaNotificationManager(this);
        mPlayback = new MediaPlayerAdapter(this, new MediaPlayerListener());

    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        stopSelf();
    }

    @Override
    public void onDestroy() {
        mMediaNotificationManager.onDestroy();
        mPlayback.stop();
        mMediaSession.release();
        Log.d(TAG, "onDestroy: MediaPlayerAdapter stopped, and MediaSession released");
    }

    @Nullable
    @Override
    public BrowserRoot onGetRoot(@NonNull String clientPackageName, int clientUid, @Nullable Bundle rootHints) {
        return new BrowserRoot("root", null);
    }

    @Override
    public void onLoadChildren(@NonNull String parentId, @NonNull Result<List<MediaBrowserCompat.MediaItem>> result) {
        List<MediaBrowserCompat.MediaItem> ret = new ArrayList<>();
        result.sendResult(ret);
    }

    public class MySessionCallback extends MediaSessionCompat.Callback {
        private final List<MediaSessionCompat.QueueItem> mPlaylist = new ArrayList<>();
        private int mQueueIndex = -1;
        private MediaMetadataCompat mPreparedMedia;
        private Uri targetUri;

        @Override
        public void onAddQueueItem(MediaDescriptionCompat description) {
            mPlaylist.add(new MediaSessionCompat.QueueItem(description, description.hashCode()));
            mQueueIndex = (mQueueIndex == -1) ? 0 : mQueueIndex;
            mMediaSession.setQueue(mPlaylist);
        }

        @Override
        public void onRemoveQueueItem(MediaDescriptionCompat description) {
            mPlaylist.remove(new MediaSessionCompat.QueueItem(description, description.hashCode()));
            mQueueIndex = (mPlaylist.isEmpty()) ? -1 : mQueueIndex;
            mMediaSession.setQueue(mPlaylist);
        }

        @Override
        public void onPrepare() {
            if (mQueueIndex < 0 && mPlaylist.isEmpty()) {
                // Nothing to play.
                return;
            }

//            final String mediaId = mPlaylist.get(mQueueIndex).getDescription().getMediaId();
//            mPreparedMedia = MusicLibrary.getMetadata(MediaPlaybackService.this, mediaId);
//            mMediaSession.setMetadata(mPreparedMedia);

            if (!mMediaSession.isActive()) {
                mMediaSession.setActive(true);
            }
        }

        @Override
        public void onPlay() {
//            if (!isReadyToPlay()) {
//                // Nothing to play.
//                return;
//            }

//            if (mPreparedMedia == null) {
//                onPrepare();
//            }
//
//            mPlayback.playFromMedia(mPreparedMedia);
//            Log.d(TAG, "onPlayFromMediaId: MediaSession active");
            if (targetUri != null) mPlayback.playFromUrl(targetUri.toString());
        }

        @Override
        public void onPause() {
            mPlayback.pause();
        }

        @Override
        public void onPlayFromUri(Uri uri, Bundle extras) {
            targetUri = uri;
            if (targetUri != null) mPlayback.playFromUrl(targetUri.toString());
        }

        @Override
        public void onStop() {
            mPlayback.stop();
            mMediaSession.setActive(false);
        }

        @Override
        public void onSkipToNext() {
            mQueueIndex = (++mQueueIndex % mPlaylist.size());
            mPreparedMedia = null;
            onPlay();
        }

        @Override
        public void onSkipToPrevious() {
            mQueueIndex = mQueueIndex > 0 ? mQueueIndex - 1 : mPlaylist.size() - 1;
            mPreparedMedia = null;
            onPlay();
        }

        @Override
        public void onSeekTo(long pos) {
            mPlayback.seekTo(pos);
        }

        @Override
        public void onCustomAction(String action, Bundle extras) {
            switch (action) {
                case CUSTOM_ACTION_REPLAY_TEN:
                    mPlayback.replayTenSeconds();
                    break;
                case CUSTOM_ACTION_FORWARD_THIRTY:
                    mPlayback.forwardThirtySeconds();
                default:
                    break;
            }
        }

        private boolean isReadyToPlay() {
            return (!mPlaylist.isEmpty());
        }
    }

    public class MediaPlayerListener extends PlaybackInfoListener {

        private final ServiceManager mServiceManager;

        MediaPlayerListener() {
            mServiceManager = new ServiceManager();
        }

        @Override
        public void onPlaybackStateChange(PlaybackStateCompat state) {
            // Report the state to the MediaSession.
            mMediaSession.setPlaybackState(state);

            // Manage the started state of this service.
            switch (state.getState()) {
                case PlaybackStateCompat.STATE_PLAYING:
                    mServiceManager.moveServiceToStartedState(state);
                    break;
                case PlaybackStateCompat.STATE_PAUSED:
                    mServiceManager.updateNotificationForPause(state);
                    break;
                case PlaybackStateCompat.STATE_STOPPED:
                    mServiceManager.moveServiceOutOfStartedState(state);
                    break;
            }
        }

        class ServiceManager {

            private void moveServiceToStartedState(PlaybackStateCompat state) {
                Notification notification =
                        mMediaNotificationManager.getNotification(
                                mPlayback.getCurrentMedia(), state, getSessionToken());

                if (!mServiceInStartedState) {
                    ContextCompat.startForegroundService(
                            MediaPlaybackService.this,
                            new Intent(MediaPlaybackService.this, MediaPlaybackService.class));
                    mServiceInStartedState = true;
                }

                startForeground(MediaNotificationManager.NOTIFICATION_ID, notification);
            }

            private void updateNotificationForPause(PlaybackStateCompat state) {
                stopForeground(false);
                Notification notification =
                        mMediaNotificationManager.getNotification(
                                mPlayback.getCurrentMedia(), state, getSessionToken());
                mMediaNotificationManager.getNotificationManager()
                        .notify(MediaNotificationManager.NOTIFICATION_ID, notification);
            }

            private void moveServiceOutOfStartedState(PlaybackStateCompat state) {
                stopForeground(true);
                stopSelf();
                mServiceInStartedState = false;
            }
        }

    }
}

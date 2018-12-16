package com.qi.somecastapp.service;

import android.app.Notification;
import android.content.Intent;
import android.media.session.PlaybackState;
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
import com.qi.somecastapp.model.Episode;
import com.qi.somecastapp.utilities.EnumPlaybackMode;

import java.util.ArrayList;
import java.util.List;

import static com.qi.somecastapp.utilities.SomePodcastAppConstants.KEY_EPISODE_META;

/**
 * Created by Qi Wu on 9/24/2018.
 */
public class MyPodcastMediaService extends MediaBrowserServiceCompat {
    public static final String ROOT_ID = "media_root_id";
    private static final String MY_EMPTY_MEDIA_ROOT_ID = "empty_root_id";
    private static final String ALBUMS_ID = "podcast_album";
    private static final String FOLDERS_ID = "podcast_folder";
    private static final String LOG_TAG = MyPodcastMediaService.class.getName();
    private static final String TAG = MyPodcastMediaService.class.getSimpleName();
    private static final String CUSTOM_ACTION_REPLAY_TEN = "replay_10";
    private static final String CUSTOM_ACTION_FORWARD_THIRTY = "forward_30";

    private MediaSessionCompat mMediaSession;
    private PlaybackStateCompat.Builder mStateBuilder;
    private PlayerAdapter mPlayback;
    private boolean mServiceInStartedState;
    private MediaNotificationManager mMediaNotificationManager;
    private String mLastCategory;
    List<MediaBrowserCompat.MediaItem> mRootItems = new ArrayList<>();
    private DataModel mDataModel;

    @Override
    public void onCreate() {
        super.onCreate();

        mDataModel = new DataModel(this);
        addRootItems();
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
        mPlayback = new MediaPlayerAdapter(this, new MediaPlayerListener(), mDataModel);

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
        return new BrowserRoot(ROOT_ID, null);
    }

    @Override
    public void onLoadChildren(@NonNull String parentId, @NonNull Result<List<MediaBrowserCompat.MediaItem>> result) {
//        switch (parentId) {
//            case ROOT_ID:
//                result.sendResult(mRootItems);
//                mLastCategory = parentId;
//                break;
//            case ALBUMS_ID:
//                mDataModel.onQueryByAlbum(parentId, result);
//                mLastCategory = parentId;
//                break;
//            case FOLDERS_ID:
//                mDataModel.onQueryByFolder(parentId, result);
//                mLastCategory = parentId;
//                break;
//            default:
//                mDataModel.onQueryByKey(mLastCategory, "%Podcasts%", result);
//        }
        mDataModel.onQueryByKey(mLastCategory, "%Podcasts%", result);
    }

    private void addRootItems() {
        MediaDescriptionCompat albums = new MediaDescriptionCompat.Builder()
                .setMediaId(ALBUMS_ID)
                .setTitle("Albums")
//                .setIconUri(Utils.getUriForResource(this, R.drawable.ic_album))
                .build();
        mRootItems.add(new MediaBrowserCompat.MediaItem(albums, MediaBrowserCompat.MediaItem.FLAG_BROWSABLE));
        MediaDescriptionCompat folders = new MediaDescriptionCompat.Builder()
                .setMediaId(FOLDERS_ID)
                .setTitle("Folders")
//                .setIconUri(Utils.getUriForResource(this, R.drawable.ic_folder))
                .build();
        mRootItems.add(new MediaBrowserCompat.MediaItem(folders, MediaBrowserCompat.MediaItem.FLAG_BROWSABLE));
    }

    public class MySessionCallback extends MediaSessionCompat.Callback {
        private EnumPlaybackMode currentMode;
        private List<MediaSessionCompat.QueueItem> mPlaylist = new ArrayList<>();
        private int mQueueIndex = -1;
        private MediaMetadataCompat mPreparedMedia;
        private Uri targetUri;
        private final PlaybackStateCompat mErrorState;

        private MySessionCallback() {
            mErrorState = new PlaybackStateCompat.Builder()
                    .setState(PlaybackState.STATE_ERROR, 0, 0)
                    .build();
        }

        @Override
        public void onPrepare() {
            if (mQueueIndex < 0 && mPlaylist.isEmpty()) {
                // Nothing to play.
                return;
            }

//            final String mediaId = mPlaylist.get(mQueueIndex).getDescription().getMediaId();
//            mPreparedMedia = MusicLibrary.getMetadata(MyPodcastMediaService.this, mediaId);
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
            if (currentMode == EnumPlaybackMode.ONLINE &&targetUri != null) {
                mPlayback.playFromUrl(targetUri.toString(), null);
            } else if (currentMode == EnumPlaybackMode.LOCAL) {
                mPlayback.play();
            }

        }

        @Override
        public void onPlayFromMediaId(String mediaId, Bundle extras) {
            startPlayback(mediaId);
            currentMode = EnumPlaybackMode.LOCAL;
        }

        @Override
        public void onPause() {
            mPlayback.pause();
        }

        @Override
        public void onPlayFromUri(Uri uri, Bundle extras) {
            targetUri = uri;
            if (targetUri != null) {
                MediaMetadataCompat meta = null;
                if (extras != null) {
                    Episode episode = extras.getParcelable("123456");
                    meta = new MediaMetadataCompat.Builder()
                            .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, episode.getId())
                            .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, episode.getPodcastName())
                            .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, episode.getLength())
                            .putString(MediaMetadataCompat.METADATA_KEY_TITLE, episode.getTitle())
                            .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, episode.getPodcastName())
                            .build();
                }
                mPlayback.playFromUrl(targetUri.toString(), meta);
                currentMode = EnumPlaybackMode.ONLINE;
            }
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

        private void startPlayback(String targetId) {
            Log.d(TAG, "startPlayback()");

            List<MediaSessionCompat.QueueItem> queue = mDataModel.getQueue();
            int idx = 0;
            int foundIdx = -1;
            for (MediaSessionCompat.QueueItem item : queue) {
                if (item.getDescription().getMediaId().equals(targetId)) {
                    foundIdx = idx;
                    break;
                }
                idx++;
            }
            if (foundIdx == -1) {
                mMediaSession.setPlaybackState(mErrorState);
                return;
            }
            mPlaylist = new ArrayList<>(queue);
            mQueueIndex = foundIdx;
            MediaSessionCompat.QueueItem current = mPlaylist.get(mQueueIndex);
            String path = current.getDescription().getExtras().getString(DataModel.PATH_KEY);
            MediaMetadataCompat metadata = mDataModel.getMetadata(current.getDescription().getMediaId());
            updateSessionQueueState();
            mPlayback.playFromMedia(path, metadata);
        }

        private void updateSessionQueueState() {
            mMediaSession.setQueueTitle("playlist");
            mMediaSession.setQueue(mPlaylist);
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
                            MyPodcastMediaService.this,
                            new Intent(MyPodcastMediaService.this, MyPodcastMediaService.class));
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

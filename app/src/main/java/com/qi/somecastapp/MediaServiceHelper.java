package com.qi.somecastapp;

import android.content.ComponentName;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaBrowserServiceCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;

import com.qi.somecastapp.model.Episode;
import com.qi.somecastapp.service.MyPodcastMediaService;
import com.qi.somecastapp.utilities.EnumApplicationMode;
import com.qi.somecastapp.utilities.EnumPlaybackMode;

import java.util.ArrayList;
import java.util.List;

import static com.qi.somecastapp.utilities.SomePodcastAppConstants.KEY_EPISODE_ALBUM;
import static com.qi.somecastapp.utilities.SomePodcastAppConstants.KEY_EPISODE_ARTIST;
import static com.qi.somecastapp.utilities.SomePodcastAppConstants.KEY_EPISODE_DURATION;
import static com.qi.somecastapp.utilities.SomePodcastAppConstants.KEY_EPISODE_ID;
import static com.qi.somecastapp.utilities.SomePodcastAppConstants.KEY_EPISODE_META;
import static com.qi.somecastapp.utilities.SomePodcastAppConstants.KEY_EPISODE_TITLE;

/**
 * Created by Qi Wu on 9/26/2018.
 */
class MediaServiceHelper {
    private static final String TAG = MediaServiceHelper.class.getSimpleName();

    private final Context mContext;
    private final Class<? extends MediaBrowserServiceCompat> mMediaBrowserServiceClass;

    private final List<MediaControllerCompat.Callback> mCallbackList = new ArrayList<>();

    private final MediaBrowserConnectionCallback mMediaBrowserConnectionCallback;
    private final MediaControllerCallback mMediaControllerCallback;
    private final MediaBrowserSubscriptionCallback mMediaBrowserSubscriptionCallback;

    private MediaBrowserCompat mMediaBrowser;

    private EnumPlaybackMode currentMode;

    private boolean haveStoragePermission;

    private ArrayList<Episode> onlinePlaylist;

    private int nowPlayingIndex = 0;

    @Nullable
    private MediaControllerCompat mMediaController;

    public MediaServiceHelper(Context context,
                              Class<? extends MediaBrowserServiceCompat> serviceClass,
                              boolean haveStoragePermission) {
        mContext = context;
        mMediaBrowserServiceClass = serviceClass;
        this.haveStoragePermission = haveStoragePermission;
        mMediaBrowserConnectionCallback = new MediaBrowserConnectionCallback();
        mMediaControllerCallback = new MediaControllerCallback();
        mMediaBrowserSubscriptionCallback = new MediaBrowserSubscriptionCallback();
    }

    public void onStart() {
        if (mMediaBrowser == null) {
            mMediaBrowser =
                    new MediaBrowserCompat(
                            mContext,
                            new ComponentName(mContext, mMediaBrowserServiceClass),
                            mMediaBrowserConnectionCallback,
                            null);
            mMediaBrowser.connect();
        }
        Log.d(TAG, "onStart: Creating MediaBrowser, and connecting");
    }

    public void onStop() {
        if (mMediaController != null) {
            mMediaController.unregisterCallback(mMediaControllerCallback);
            mMediaController = null;
        }
        if (mMediaBrowser != null && mMediaBrowser.isConnected()) {
            mMediaBrowser.disconnect();
            mMediaBrowser = null;
        }
        resetState();
        Log.d(TAG, "onStop: Releasing MediaController, Disconnecting from MediaBrowser");
    }

    /**
     * Called after connecting with a {@link MediaBrowserServiceCompat}.
     * <p>
     * Override to perform processing after a connection is established.
     *
     * @param mediaController {@link MediaControllerCompat} associated with the connected
     *                        MediaSession.
     */
    protected void onConnected(@NonNull MediaControllerCompat mediaController) {
    }

    /**
     * Called after loading a browsable {@link MediaBrowserCompat.MediaItem}
     *
     * @param parentId The media ID of the parent item.
     * @param children List (possibly empty) of child items.
     */
    protected void onChildrenLoaded(@NonNull String parentId,
                                    @NonNull List<MediaBrowserCompat.MediaItem> children) {
    }

    /**
     * Called when the {@link MediaBrowserServiceCompat} connection is lost.
     */
    protected void onDisconnected() {
    }

    @NonNull
    protected final MediaControllerCompat getMediaController() {
        if (mMediaController == null) {
            throw new IllegalStateException("MediaController is null!");
        }
        return mMediaController;
    }

    /**
     * The internal state of the app needs to revert to what it looks like when it started before
     * any connections to the {@link MyPodcastMediaService} happens via the {@link MediaSessionCompat}.
     */
    private void resetState() {
        performOnAllCallbacks(new CallbackCommand() {
            @Override
            public void perform(@NonNull MediaControllerCompat.Callback callback) {
                callback.onPlaybackStateChanged(null);
            }
        });
        Log.d(TAG, "resetState: ");
    }

    public MediaControllerCompat.TransportControls getTransportControls() {
        if (mMediaController == null) {
            Log.d(TAG, "getTransportControls: MediaController is null!");
            throw new IllegalStateException("MediaController is null!");
        }
        return mMediaController.getTransportControls();
    }

    public void registerCallback(MediaControllerCompat.Callback callback) {
        if (callback != null) {
            mCallbackList.add(callback);

            // Update with the latest metadata/playback state.
            if (mMediaController != null) {
                final MediaMetadataCompat metadata = mMediaController.getMetadata();
                if (metadata != null) {
                    callback.onMetadataChanged(metadata);
                }

                final PlaybackStateCompat playbackState = mMediaController.getPlaybackState();
                if (playbackState != null) {
                    callback.onPlaybackStateChanged(playbackState);
                }
            }
        }
    }

    private void performOnAllCallbacks(@NonNull CallbackCommand command) {
        for (MediaControllerCompat.Callback callback : mCallbackList) {
            if (callback != null) {
                command.perform(callback);
            }
        }
    }

    public void subscribeNewRoot(String mediaId) {
        mMediaBrowser.subscribe(mediaId, mMediaBrowserSubscriptionCallback);
    }

    public void setOnlinePlaylist(ArrayList<Episode> data) {
        onlinePlaylist = data;
    }

    public void playOnlineContent(Episode episode) {
        nowPlayingIndex = onlinePlaylist.indexOf(episode);
        Bundle bundle = new Bundle();
        bundle.putString(KEY_EPISODE_TITLE, episode.getTitle());
        bundle.putString(KEY_EPISODE_ALBUM, episode.getPodcastName());
        bundle.putLong(KEY_EPISODE_DURATION, episode.getLength());
        bundle.putString(KEY_EPISODE_ARTIST, episode.getPodcastName());
        bundle.putString(KEY_EPISODE_ID, episode.getId());
        getTransportControls().playFromUri(Uri.parse(episode.getAudioPath()), bundle);
        currentMode = EnumPlaybackMode.ONLINE;
    }

    public void requestPreviousTrack() {
        if (currentMode == EnumPlaybackMode.ONLINE) {
            int targetIndex = nowPlayingIndex == 0 ? nowPlayingIndex : nowPlayingIndex - 1 ;
            getTransportControls().playFromUri(Uri.parse(onlinePlaylist.get(targetIndex).getAudioPath()), null);
            nowPlayingIndex = targetIndex;
        } else {
            getTransportControls().skipToPrevious();
        }
    }

    public void requestNextTrack() {
        if (currentMode == EnumPlaybackMode.ONLINE) {
            if (nowPlayingIndex == onlinePlaylist.size() - 1) {
                getTransportControls().stop();
            } else {
                getTransportControls().playFromUri(Uri.parse(onlinePlaylist.get(nowPlayingIndex + 1).getAudioPath()), null);
                nowPlayingIndex = nowPlayingIndex + 1;
            }
        } else {
            getTransportControls().skipToNext();
        }
    }

    public void playLocalContent(String mediaId) {
        getTransportControls().playFromMediaId(mediaId, null);
        currentMode = EnumPlaybackMode.LOCAL;
    }

    /**
     * Helper for more easily performing operations on all listening clients.
     */
    private interface CallbackCommand {
        void perform(@NonNull MediaControllerCompat.Callback callback);
    }

    // Receives callbacks from the MediaBrowser when it has successfully connected to the
    // MediaBrowserService (MusicService).
    private class MediaBrowserConnectionCallback extends MediaBrowserCompat.ConnectionCallback {

        // Happens as a result of onStart().
        @Override
        public void onConnected() {
            try {
                // Get a MediaController for the MediaSession.
                mMediaController =
                        new MediaControllerCompat(mContext, mMediaBrowser.getSessionToken());
                mMediaController.registerCallback(mMediaControllerCallback);

                // Sync existing MediaSession state to the UI.
                mMediaControllerCallback.onMetadataChanged(mMediaController.getMetadata());
                mMediaControllerCallback.onPlaybackStateChanged(
                        mMediaController.getPlaybackState());

                MediaServiceHelper.this.onConnected(mMediaController);
            } catch (RemoteException e) {
                Log.d(TAG, String.format("onConnected: Problem: %s", e.toString()));
                throw new RuntimeException(e);
            }

            if (haveStoragePermission) mMediaBrowser.subscribe("%Podcasts%", mMediaBrowserSubscriptionCallback);
        }

        @Override
        public void onConnectionSuspended() {
            Log.d(TAG, "mConnectionCallbacks.onConnectionSuspended");
        }

        @Override
        public void onConnectionFailed() {
            Log.d(TAG, "mConnectionCallbacks.onConnectionFailed");
        }
    }



    // Receives callbacks from the MediaBrowser when the MediaBrowserService has loaded new media
    // that is ready for playback.
    public class MediaBrowserSubscriptionCallback extends MediaBrowserCompat.SubscriptionCallback {

        @Override
        public void onChildrenLoaded(@NonNull String parentId,
                                     @NonNull List<MediaBrowserCompat.MediaItem> children) {
            MediaServiceHelper.this.onChildrenLoaded(parentId, children);
        }
    }

    // Receives callbacks from the MediaController and updates the UI state,
    // i.e.: Which is the current item, whether it's playing or paused, etc.
    private class MediaControllerCallback extends MediaControllerCompat.Callback {

        @Override
        public void onMetadataChanged(final MediaMetadataCompat metadata) {
            performOnAllCallbacks(new CallbackCommand() {
                @Override
                public void perform(@NonNull MediaControllerCompat.Callback callback) {
                    callback.onMetadataChanged(metadata);
                }
            });
        }

        @Override
        public void onPlaybackStateChanged(@Nullable final PlaybackStateCompat state) {
            performOnAllCallbacks(new CallbackCommand() {
                @Override
                public void perform(@NonNull MediaControllerCompat.Callback callback) {
                    callback.onPlaybackStateChanged(state);
                }
            });
        }

        // This might happen if the MusicService is killed while the Activity is in the
        // foreground and onStart() has been called (but not onStop()).
        @Override
        public void onSessionDestroyed() {
            resetState();
            onPlaybackStateChanged(null);

            MediaServiceHelper.this.onDisconnected();
        }
    }
}

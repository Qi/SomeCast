package com.qi.somecastapp;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ListFragment;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.qi.somecastapp.model.Episode;

import java.util.ArrayList;
import java.util.List;

import static com.qi.somecastapp.service.MyPodcastMediaService.ROOT_ID;

/**
 * A fragment representing a list of Items.
 * <p/>
 */
public class DownloadsFragment extends ListFragment {
    private static final String TAG = "DownloadsFragment";

    // Hints
    public static final String HINT_DISPLAY = "com.example.android.musicbrowserdemo.DISPLAY";

    // For args
    public static final String ARG_ID = "id";

    private Adapter mAdapter;
    private List<Item> mItems = new ArrayList();
    private String mId;
    private PodcastClickListener mListener;

    public static class Item {
        final MediaBrowserCompat.MediaItem media;

        Item(MediaBrowserCompat.MediaItem m) {
            this.media = m;
        }
    }

    public DownloadsFragment() {
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.d(TAG, "onActivityCreated -- " + hashCode());
        mAdapter = new Adapter();
        setListAdapter(mAdapter);
        ((PlaybackController)getContext()).getMediaServiceHelper().registerCallback(this.new MediaBrowserListener());
        // Get our arguments
        final Bundle args = getArguments();
        if (args != null) {
            mId = args.getString(ARG_ID);
        } else {
            mId = ROOT_ID;
        }

        // A hint about who we are, so the service can customize the results if it wants to.
        final Bundle rootHints = new Bundle();
        rootHints.putBoolean(HINT_DISPLAY, true);
        ((AppCompatActivity)getActivity()).getSupportActionBar().show();
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        final Item item = mItems.get(position);
        Log.i("DownloadsFragment", "Item clicked: " + position + " -- "
                + mAdapter.getItem(position).media.getMediaId());
        mListener.onEpisodeClicked(item, v);
    }

    void setFragmentData(@NonNull String parentId, @NonNull List<MediaBrowserCompat.MediaItem> children) {
        Log.d(TAG, "onChildrenLoaded parentUri=" + parentId
                + " children= " + children);
        mItems.clear();
        final int N = children.size();
        for (int i=0; i<N; i++) {
            mItems.add(new Item(children.get(i)));
        }
        if (mAdapter != null)
            mAdapter.notifyDataSetChanged();
    }

    private class Adapter extends BaseAdapter {
        private String nowPlayingEpisodeId;
        private final LayoutInflater mInflater;

        Adapter() {
            super();

            final Context context = getActivity();
            mInflater = LayoutInflater.from(context);
        }

        @Override
        public int getCount() {
            return mItems.size();
        }

        @Override
        public Item getItem(int position) {
            return mItems.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public int getItemViewType(int position) {
            return 1;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = mInflater.inflate(android.R.layout.simple_list_item_1, parent, false);
            }

            final TextView tv = (TextView)convertView;
            final Item item = mItems.get(position);
            tv.setText(item.media.getDescription().getTitle());
            if (isNowPlayingEpisode(item.media.getDescription().getMediaId())){
                tv.setTypeface(null, Typeface.BOLD_ITALIC);
            } else {
                tv.setTypeface(null, Typeface.NORMAL);
            }
            return convertView;
        }

        @Override
        public int getViewTypeCount() {
            return 1;
        }

        private void setNowPlayingEpisodeId(String mediaId) {
            nowPlayingEpisodeId = mediaId;
            notifyDataSetChanged();
        }

        private boolean isNowPlayingEpisode(String id) {
            return id.equalsIgnoreCase(nowPlayingEpisodeId);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof PodcastClickListener) {
            mListener = (PodcastClickListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement PodcastClickListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    class MediaBrowserListener extends MediaControllerCompat.Callback{
        @Override
        public void onMetadataChanged(MediaMetadataCompat mediaMetadata) {
            if (mediaMetadata == null) {
                return;
            }
            mAdapter.setNowPlayingEpisodeId(mediaMetadata.getDescription().getMediaId());
        }
    }
}

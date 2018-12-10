package com.qi.somecastapp;

import android.content.ComponentName;
import android.content.Context;
import android.media.browse.MediaBrowser;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.ListFragment;
import android.support.v4.media.MediaBrowserCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.qi.somecastapp.service.MediaPlaybackService;

import java.util.ArrayList;
import java.util.List;

import static android.support.v4.media.MediaBrowserCompat.MediaItem.FLAG_PLAYABLE;
import static com.qi.somecastapp.service.MediaPlaybackService.ROOT_ID;

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
    private MediaBrowserCompat mBrowser;

    private static class Item {
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

        mBrowser = new MediaBrowserCompat(getActivity(), new ComponentName(getActivity(), MediaPlaybackService.class), mConnectionCallbacks, rootHints);
    }

    @Override
    public void onStart() {
        super.onStart();
        mBrowser.connect();
    }

    @Override
    public void onStop() {
        super.onStop();
        mBrowser.disconnect();
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        final Item item = mItems.get(position);
        Log.i("DownloadsFragment", "Item clicked: " + position + " -- "
                + mAdapter.getItem(position).media.getMediaId());

        if (item.media.getFlags() != FLAG_PLAYABLE) {
            final DownloadsFragment fragment = new DownloadsFragment();

            final Bundle args = new Bundle();
            args.putString(ARG_ID, item.media.getMediaId());
            fragment.setArguments(args);

            getFragmentManager().beginTransaction()
                    .replace(R.id.frame_container, fragment)
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                    .addToBackStack(null)
                    .commit();
        } else {

        }

    }

    final MediaBrowserCompat.ConnectionCallback mConnectionCallbacks
            = new MediaBrowserCompat.ConnectionCallback() {
        @Override
        public void onConnected() {
            Log.d(TAG, "mConnectionCallbacks.onConnected");
            if (mId == null) {
                mId = mBrowser.getRoot();
            }
            mBrowser.subscribe(mId, new MediaBrowserCompat.SubscriptionCallback() {
                @Override
                public void onChildrenLoaded(@NonNull String parentId, @NonNull List<MediaBrowserCompat.MediaItem> children) {
                    Log.d(TAG, "onChildrenLoaded parentUri=" + parentId
                            + " children= " + children);
                    mItems.clear();
                    final int N = children.size();
                    for (int i=0; i<N; i++) {
                        mItems.add(new Item(children.get(i)));
                    }
                    mAdapter.notifyDataSetChanged();
                }
            });
        }

        @Override
        public void onConnectionSuspended() {
            Log.d(TAG, "mConnectionCallbacks.onConnectionSuspended");
        }

        @Override
        public void onConnectionFailed() {
            Log.d(TAG, "mConnectionCallbacks.onConnectionFailed");
        }
    };

    private class Adapter extends BaseAdapter {
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

            return convertView;
        }

        @Override
        public int getViewTypeCount() {
            return 1;
        }
    }
}

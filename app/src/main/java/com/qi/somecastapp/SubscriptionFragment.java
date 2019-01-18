package com.qi.somecastapp;

import android.app.LoaderManager;
import android.content.AsyncTaskLoader;
import android.content.Context;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.qi.somecastapp.database.SubscribedContract;
import com.qi.somecastapp.model.Podcast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * A fragment representing a list of Items.
 * <p/>
 */
public class SubscriptionFragment extends Fragment implements LoaderManager.LoaderCallbacks<ArrayList<Object>>{

    public static final int EPISODE_SCREEN = 0;
    public static final int PODCAST_SCREEN = 1;

    private static final String ARG_COLUMN_COUNT = "column-count";
    private static final String ARG_SCREEN_TYPE = "screen_type";
    // TODO: Customize parameters
    private int mColumnCount = 3;
    RecyclerView subscriptionRv;
    private PodcastClickListener mListener;
    private PodcastListAdapter adapter;


    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public SubscriptionFragment() {
    }

    // TODO: Customize parameter initialization
    @SuppressWarnings("unused")
    public static SubscriptionFragment newInstance(int screenType) {
        SubscriptionFragment fragment = new SubscriptionFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SCREEN_TYPE, screenType);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mColumnCount = getArguments().getInt(ARG_COLUMN_COUNT);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_subscription, container, false);
        subscriptionRv = rootView.findViewById(R.id.rv_subscription);
        subscriptionRv.setHasFixedSize(true);
        LinearLayoutManager layoutManager;
        layoutManager = new GridLayoutManager(getContext(), 4);
        adapter = new PodcastListAdapter(mListener);
        subscriptionRv.setLayoutManager(layoutManager);
        subscriptionRv.setAdapter(adapter);
        ((AppCompatActivity)getActivity()).getSupportActionBar().show();
        getActivity().getLoaderManager().initLoader(0, null, this).forceLoad();
        return rootView;
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

    @Override
    public Loader<ArrayList<Object>> onCreateLoader(int id, Bundle args) {
        return new FetchSubscriptionTask(getContext());
    }

    @Override
    public void onLoadFinished(Loader<ArrayList<Object>> loader, ArrayList<Object> data) {
        ArrayList<Podcast> realPodcasts = new ArrayList<>();
        for (Object p : data) {
            ((Podcast)p).setSubscribed(true);
            realPodcasts.add((Podcast) p);
        }
        adapter.setData(realPodcasts);
    }

    @Override
    public void onLoaderReset(Loader<ArrayList<Object>> loader) {

    }

    private static class FetchSubscriptionTask extends AsyncTaskLoader<ArrayList<Object>> {


        public FetchSubscriptionTask(Context context) {
            super(context);
        }

        @Override
        public ArrayList<Object> loadInBackground() {
                ArrayList<Object> podcasts = new ArrayList<>();
                Cursor cursor = getContext().getContentResolver().query(SubscribedContract.SubscribedEntry.CONTENT_URI, null, null, null, null);
                if (cursor != null) {
                    int indexForJson = cursor.getColumnIndex(SubscribedContract.SubscribedEntry.COLUMN_PODCAST_META);
                    while (cursor.moveToNext()) {
                        try {
                            JSONObject json = new JSONObject(cursor.getString(indexForJson));
                            Podcast temp = new Podcast(json);
                            podcasts.add(temp);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    cursor.close();
                }
                return podcasts;

        }

        @Override
        public void deliverResult(ArrayList<Object> data) {
            super.deliverResult(data);
        }
    }
}

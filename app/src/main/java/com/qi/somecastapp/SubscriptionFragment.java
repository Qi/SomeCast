package com.qi.somecastapp;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.qi.somecastapp.model.Episode;
import com.qi.somecastapp.utilities.FetchSubscriptionTask;

/**
 * A fragment representing a list of Items.
 * <p/>
 */
public class SubscriptionFragment extends Fragment{

    public static final int EPISODE_SCREEN = 0;
    public static final int PODCAST_SCREEN = 1;

    private static final String ARG_COLUMN_COUNT = "column-count";
    private static final String ARG_SCREEN_TYPE = "screen_type";
    // TODO: Customize parameters
    private int mColumnCount = 3;
    RecyclerView subscriptionRv;
    private PodcastClickListener mListener;


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
        RecyclerView.Adapter adapter;
        layoutManager = new GridLayoutManager(getContext(), 4);
        adapter = new PodcastListAdapter(mListener);
        new FetchSubscriptionTask(adapter, getContext()).execute(PODCAST_SCREEN);
        subscriptionRv.setLayoutManager(layoutManager);
        subscriptionRv.setAdapter(adapter);
        ((AppCompatActivity)getActivity()).getSupportActionBar().show();
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
}

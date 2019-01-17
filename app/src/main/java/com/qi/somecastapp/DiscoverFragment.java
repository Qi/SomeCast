package com.qi.somecastapp;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.Volley;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.mancj.materialsearchbar.MaterialSearchBar;
import com.qi.somecastapp.utilities.JsonUtils;
import com.qi.somecastapp.utilities.NetworkUtils;

import java.util.List;

import static android.widget.LinearLayout.VERTICAL;

/**
 * A fragment representing a list of Items.
 */
public class DiscoverFragment extends Fragment implements MaterialSearchBar.OnSearchActionListener {

    // TODO: Customize parameter argument names
    private static final String ARG_COLUMN_COUNT = "column-count";
    private static final String TAG = DiscoverFragment.class.getSimpleName();
    // TODO: Customize parameters
    private int mColumnCount = 1;
    private RequestQueue requestQueue;
    private GenreListAdapter genreAdapter;
    private PodcastClickListener mListener;
    private PodcastListAdapter searchResultAdapter;
    private RecyclerView recyclerView;
    private MaterialSearchBar searchBar;
    private FirebaseAnalytics mFirebaseAnalytics;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public DiscoverFragment() {
    }

    // TODO: Customize parameter initialization
    @SuppressWarnings("unused")
    public static DiscoverFragment newInstance(int columnCount) {
        DiscoverFragment fragment = new DiscoverFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_COLUMN_COUNT, columnCount);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mColumnCount = getArguments().getInt(ARG_COLUMN_COUNT);
        }
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(getContext());
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "API called!");
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
        requestQueue = Volley.newRequestQueue(getContext());
        Response.Listener<String> responseListener = new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d(TAG, response);
                genreAdapter.setData(JsonUtils.parseGenres(response));
            }
        };
        requestQueue.add(NetworkUtils.getGenreList(responseListener));
        genreAdapter = new GenreListAdapter(requestQueue, mListener);
        searchResultAdapter = new PodcastListAdapter(mListener, PodcastListAdapter.VERTICAL);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_discover_list, container, false);
        recyclerView = view.findViewById(R.id.rv_genre_list);

        // Set the adapter
        Context context = view.getContext();
        RecyclerView.LayoutManager layoutManager;
        if (mColumnCount <= 1) {
            layoutManager = new LinearLayoutManager(context);
            recyclerView.setLayoutManager(layoutManager);
        } else {
            layoutManager = new GridLayoutManager(context, mColumnCount);
            recyclerView.setLayoutManager(layoutManager);
        }
        recyclerView.setAdapter(genreAdapter);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(), VERTICAL);
        recyclerView.addItemDecoration(dividerItemDecoration);
        ((AppCompatActivity) getActivity()).getSupportActionBar().show();
        searchBar = view.findViewById(R.id.searchBar);
        searchBar.setHint("Search Podcast");
        searchBar.setSpeechMode(false);
        //enable searchbar callbacks
        searchBar.setOnSearchActionListener(this);
        searchBar.setCardViewElevation(10);
        return view;
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

    public boolean onBackPressed() {
        if(recyclerView.getAdapter() instanceof PodcastListAdapter) {
            recyclerView.setAdapter(genreAdapter);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void onSearchStateChanged(boolean enabled) {
        if (!enabled) onBackPressed();
    }

    @Override
    public void onSearchConfirmed(CharSequence text) {
        recyclerView.setAdapter(searchResultAdapter);
        Response.Listener<String> responseListener = new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d(TAG, response);
                searchResultAdapter.setData(JsonUtils.parsePodcastInGenre(response));
            }
        };
        requestQueue.add(NetworkUtils.searchPodcast(text.toString(), responseListener));
    }

    @Override
    public void onButtonClicked(int buttonCode) {

    }
}

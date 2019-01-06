package com.qi.somecastapp;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
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
import com.qi.somecastapp.utilities.JsonUtils;
import com.qi.somecastapp.utilities.NetworkUtils;

/**
 * A fragment representing a list of Items.
 */
public class DiscoverFragment extends Fragment {

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
        Button searchBt = view.findViewById(R.id.search_button);
        final TextView searchText = view.findViewById(R.id.search_box);
        searchBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                recyclerView.setAdapter(searchResultAdapter);
                Response.Listener<String> responseListener = new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d(TAG, response);
                        searchResultAdapter.setData(JsonUtils.parsePodcastInGenre(response));
                    }
                };
                requestQueue.add(NetworkUtils.searchPodcast(searchText.getText().toString(), responseListener));
            }
        });

        // Set the adapter
        Context context = view.getContext();

        if (mColumnCount <= 1) {
            recyclerView.setLayoutManager(new LinearLayoutManager(context));
        } else {
            recyclerView.setLayoutManager(new GridLayoutManager(context, mColumnCount));
        }
        recyclerView.setAdapter(genreAdapter);

        ((AppCompatActivity) getActivity()).getSupportActionBar().show();
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
}

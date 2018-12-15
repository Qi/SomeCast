package com.qi.somecastapp;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.Volley;
import com.qi.somecastapp.database.SubscribedContract;
import com.qi.somecastapp.model.Episode;
import com.qi.somecastapp.model.Podcast;
import com.qi.somecastapp.utilities.CheckSubscriptionStatusTask;
import com.qi.somecastapp.utilities.JsonUtils;
import com.qi.somecastapp.utilities.NetworkUtils;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link PodcastClickListener} interface
 * to handle interaction events.
 * Use the {@link PodcastDetailFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PodcastDetailFragment extends Fragment {
    private static final String TAG = PodcastDetailFragment.class.getSimpleName();
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_DATA = "jsonData";
    private static final String ARG_SUBSCRIBED = "isSubscribed";

    // TODO: Rename and change types of parameters
    private String mPodcastJsonData;

    private PodcastClickListener mListener;

    public void setSubscribed(boolean subscribed) {
        this.subscribed = subscribed;
        updateFavImage(subscribed);
    }

    private boolean subscribed = false;
    private Podcast currentPodcast;
    private EpisodeListAdapter episodeListAdapter;
    private ArrayList<Episode> episodes;
    private FloatingActionButton fab;
    private Toolbar toolbar;


    public PodcastDetailFragment() {
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param subscribed
     * @return A new instance of fragment PodcastDetailFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static PodcastDetailFragment newInstance(String param1, boolean subscribed) {
        PodcastDetailFragment fragment = new PodcastDetailFragment();
        Bundle args = new Bundle();
        args.putString(ARG_DATA, param1);
        args.putBoolean(ARG_SUBSCRIBED, subscribed);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mPodcastJsonData = getArguments().getString(ARG_DATA);
            subscribed = getArguments().getBoolean(ARG_SUBSCRIBED);
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_podcast_detail, container, false);
        fab = view.findViewById(R.id.fab_subscribe);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addFavorite();
            }
        });
        toolbar = view.findViewById(R.id.toolbar);
        if (mPodcastJsonData != null) {
            ImageView posterIv = view.findViewById(R.id.iv_detail_poster);
            try {
                currentPodcast = new Podcast(new JSONObject(mPodcastJsonData));
                Picasso.with(getContext()).load(currentPodcast.getImagePath()).into(posterIv);
                RequestQueue requestQueue = Volley.newRequestQueue(getContext());
                LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
                layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
                layoutManager.setAutoMeasureEnabled(true);
                RecyclerView episodeRv = view.findViewById(R.id.rv_episode);
                episodeRv.setLayoutManager(layoutManager);
                episodeListAdapter = new EpisodeListAdapter(mListener);
                episodeRv.setAdapter(episodeListAdapter);
                toolbar.setTitle(currentPodcast.getPodcastName());
                Response.Listener<String> responseListener = new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d(TAG, response);
                        episodes = JsonUtils.parseEpisodes(response, currentPodcast.getPodcastName());
                        episodeListAdapter.setData(episodes);
                    }
                };
                requestQueue.add(NetworkUtils.getPodcastMeta(currentPodcast.getId(), responseListener));
                if (!subscribed) {
                    (new CheckSubscriptionStatusTask(getContext(), this)).execute(currentPodcast.getId());
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        updateFavImage(subscribed);
        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof PodcastClickListener) {
            mListener = (PodcastClickListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    private void addFavorite() {
        if (currentPodcast != null) {
            ContentValues contentValues = new ContentValues();
            contentValues.put(SubscribedContract.SubscribedEntry.COLUMN_PODCAST_ID, currentPodcast.getId());
            contentValues.put(SubscribedContract.SubscribedEntry.COLUMN_PODCAST_META, currentPodcast.getRawData());
            if (!subscribed) {
                Uri uri = getActivity().getContentResolver().insert(SubscribedContract.SubscribedEntry.CONTENT_URI, contentValues);
                if (uri != null) {
                    Toast.makeText(getContext(), currentPodcast.getPodcastName() + " subscribed.", Toast.LENGTH_SHORT).show();
                }

                subscribed = true;
            } else {
                Uri uri = SubscribedContract.SubscribedEntry.CONTENT_URI.buildUpon().appendPath(currentPodcast.getId()).build();
                int deleted = getActivity().getContentResolver().delete(uri, null, null);
                if (deleted > 0) {
                    Toast.makeText(getContext(), currentPodcast.getPodcastName() + " unsubscribed.", Toast.LENGTH_SHORT).show();
                }
                subscribed = false;
            }
            updateFavImage(subscribed);
        }
    }

    private void updateFavImage(boolean subscribed) {
        if (fab != null) {
            //TODO: updateFavImage fab.setImageResource get resource
//            fab.setImageResource(subscribed? );
        }
    }

}

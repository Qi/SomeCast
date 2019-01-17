package com.qi.somecastapp;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.qi.somecastapp.model.Genre;
import com.qi.somecastapp.utilities.JsonUtils;
import com.qi.somecastapp.utilities.NetworkUtils;

import java.util.ArrayList;

/**
 * Created by Qi Wu on 7/4/2018.
 */
class GenreListAdapter extends RecyclerView.Adapter<GenreListAdapter.GenreHolder>{
    private Context mContext;
    private ArrayList<Genre> genreList;
    private RequestQueue mRequestQueue;
    private PodcastClickListener mClickListener;
    private final String TAG = this.getClass().getSimpleName();
    private FirebaseAnalytics mFirebaseAnalytics;

    public GenreListAdapter(RequestQueue requestQueue, PodcastClickListener clickListener) {
        mRequestQueue = requestQueue;
        mClickListener = clickListener;
    }

    @Override
    public GenreHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        mContext = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View parentView = inflater.inflate(R.layout.genre_view_holder, parent, false);
        return new GenreHolder(parentView);
    }

    void setData(ArrayList<Genre> genres) {
        genreList = genres;
        notifyDataSetChanged();
    }

    @Override
    public void onBindViewHolder(final GenreHolder holder, int position) {
        if(genreList != null) {
            holder.genreName.setText(genreList.get(position).getName());

            if (holder.adapter.getItemCount() == 0) {
                mFirebaseAnalytics = FirebaseAnalytics.getInstance(mContext);
                Bundle bundle = new Bundle();
                bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "API called!");
                mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
                Response.Listener<String> responseListener = new Response.Listener<String>(){
                    @Override
                    public void onResponse(String response) {
                        Log.d(TAG, response);
                        holder.adapter.setData(JsonUtils.parsePodcastInGenre(response));
                    }
                };
                mRequestQueue.add(NetworkUtils.getPodcastList(genreList.get(position).getId(), responseListener));
            }
        }
    }

    @Override
    public int getItemCount() {
        if (genreList != null) return genreList.size();
        else return 0;
    }

    class GenreHolder extends RecyclerView.ViewHolder{
        TextView genreName;
        RecyclerView podcastRv;
        PodcastListAdapter adapter;

        GenreHolder(View itemView) {
            super(itemView);
            genreName = itemView.findViewById(R.id.tv_genre_name);
            podcastRv = itemView.findViewById(R.id.rv_podcast_list_in_genre);
            GridLayoutManager layoutManager = new GridLayoutManager(mContext, 4);
            podcastRv.setHasFixedSize(true);
            podcastRv.setLayoutManager(layoutManager);
            adapter = new PodcastListAdapter(mClickListener);
            podcastRv.setAdapter(adapter);
        }
    }
}

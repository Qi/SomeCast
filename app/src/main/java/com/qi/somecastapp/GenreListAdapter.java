package com.qi.somecastapp;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
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
    private final String TAG = this.getClass().getSimpleName();

    public GenreListAdapter(RequestQueue requestQueue) {
        mRequestQueue = requestQueue;
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
            LinearLayoutManager layoutManager = new LinearLayoutManager(itemView.getContext());
            layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
            podcastRv.setLayoutManager(layoutManager);
            adapter = new PodcastListAdapter();
            podcastRv.setAdapter(adapter);
        }
    }
}

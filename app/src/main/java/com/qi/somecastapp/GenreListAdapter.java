package com.qi.somecastapp;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.qi.somecastapp.model.Genre;

import java.util.ArrayList;

/**
 * Created by Qi Wu on 7/4/2018.
 */
class GenreListAdapter extends RecyclerView.Adapter<GenreListAdapter.GenreHolder>{
    private Context mContext;
    private ArrayList<Genre> genreList;

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
    public void onBindViewHolder(GenreHolder holder, int position) {
        if(genreList != null) {
            holder.genraName.setText(genreList.get(position).getName());
        }
    }

    @Override
    public int getItemCount() {
        if (genreList != null) return genreList.size();
        else return 0;
    }

    public class GenreHolder extends RecyclerView.ViewHolder{
        TextView genraName;

        public GenreHolder(View itemView) {
            super(itemView);
            genraName = itemView.findViewById(R.id.tv_genre_name);
        }
    }
}

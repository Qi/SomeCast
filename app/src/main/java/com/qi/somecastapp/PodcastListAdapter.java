package com.qi.somecastapp;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.qi.somecastapp.model.Podcast;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Created by Qi Wu on 6/23/2018.
 */
public class PodcastListAdapter extends RecyclerView.Adapter<PodcastListAdapter.Holder>{
    private Context mContext;

    private ArrayList<Podcast> podcastList;

    @Override
    public Holder onCreateViewHolder(ViewGroup parent, int viewType) {
        mContext = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View parentView = inflater.inflate(R.layout.podcast_view_holder, parent, false);
        int width = parent.getMeasuredWidth();
        parentView.setMinimumWidth(width/3);

        return new Holder(parentView);
    }

    @Override
    public void onBindViewHolder(Holder holder, int position) {
        if(podcastList != null) {
            Picasso.with(mContext).load(podcastList.get(position).getImagePath()).into(holder.mPosterImageView);
            holder.showNameTv.setText(podcastList.get(position).getPodcastName());
        }
    }

    @Override
    public int getItemCount() {
        if (podcastList != null) return podcastList.size();
        else return 0;
    }

    public void setData(ArrayList<Podcast> data) {
        this.podcastList = data;
        notifyDataSetChanged();
    }

    class Holder extends RecyclerView.ViewHolder implements View.OnClickListener{
        final AppCompatImageView mPosterImageView;
        TextView showNameTv;
        private Context mContext;

        Holder(View itemView) {
            super(itemView);
            mPosterImageView = itemView.findViewById(R.id.iv_poster);
            showNameTv = itemView.findViewById(R.id.tv_show_name);
            mContext = itemView.getContext();
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            Intent intent = new Intent(mContext, PodcastDetailActivity.class);
            intent.putExtra(Intent.EXTRA_TEXT, podcastList.get(getAdapterPosition()).getRawData());
            mContext.startActivity(intent);
        }
    }
}

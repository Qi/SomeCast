package com.qi.somecastapp;

import android.app.DownloadManager;
import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.qi.somecastapp.model.Episode;

import java.util.ArrayList;

/**
 * Created by Qi Wu on 7/5/2018.
 */
class EpisodeListAdapter extends RecyclerView.Adapter<EpisodeListAdapter.Holder>{
    private ArrayList<Episode> episodeList;
    private Context mContext;
    private PlaybackListener mPlaybackListener;

    public EpisodeListAdapter(PlaybackListener mPlaybackListener) {
        this.mPlaybackListener = mPlaybackListener;
    }

    @Override
    public Holder onCreateViewHolder(ViewGroup parent, int viewType) {
        mContext = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View parentView = inflater.inflate(R.layout.episode_view_holder, parent, false);
        return new Holder(parentView);
    }

    @Override
    public void onBindViewHolder(Holder holder, int position) {
        if (episodeList != null) {
            holder.title.setText(episodeList.get(position).getTitle());
        }
    }

    @Override
    public int getItemCount() {
        if (episodeList != null) return episodeList.size();
        else return 0;
    }

    public void setData(ArrayList<Episode> data) {
        this.episodeList = data;
        notifyDataSetChanged();
    }

    public class Holder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView title;
        Button downloadBt;
        public Holder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.tv_episode_title);
            downloadBt = itemView.findViewById(R.id.bt_download);
            title.setOnClickListener(this);
            downloadBt.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            mPlaybackListener.onEpisodeClicked(episodeList.get(getAdapterPosition()), v);
        }
    }
}

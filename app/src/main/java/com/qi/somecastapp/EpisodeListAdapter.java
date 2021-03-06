package com.qi.somecastapp;

import android.content.Context;
import android.graphics.Typeface;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.qi.somecastapp.model.Episode;

import java.util.ArrayList;

/**
 * Created by Qi Wu on 7/5/2018.
 */
class EpisodeListAdapter extends RecyclerView.Adapter<EpisodeListAdapter.Holder>{
    private ArrayList<Episode> episodeList;
    private Context mContext;
    private PodcastClickListener mClickListener;
    private String nowPlayingEpisodeId;

    public EpisodeListAdapter(PodcastClickListener clickListener) {
        this.mClickListener = clickListener;
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
            if(isNowPlayingEpisode(episodeList.get(position))){
                holder.title.setTypeface(null, Typeface.BOLD_ITALIC);
            }
            if(isDownloadedEpisode(episodeList.get(position)) != -1){
                holder.downloadBt.setVisibility(View.INVISIBLE);
            }
        }
    }

    private boolean isNowPlayingEpisode(Episode episode) {
        return episode.getId().equalsIgnoreCase(nowPlayingEpisodeId);
    }

    private int isDownloadedEpisode(Episode episode) {
        ArrayList<String> pathList = ((PlaybackController) mContext).getDownloadedEpisodeList();
        if (pathList == null) return -1;
        for (String path : pathList) {
            if (path.contains(episode.getId())) return pathList.indexOf(path);
        }
        return -1;
    }

    @Override
    public int getItemCount() {
        if (episodeList != null) return episodeList.size();
        else return 0;
    }

    public void setData(ArrayList<Episode> data) {
        this.episodeList = data;
        notifyDataSetChanged();
        mClickListener.onEpisodeSet(data);
    }

    void setNowPlayingEpisodeId(String mediaId) {
        nowPlayingEpisodeId = mediaId;
        notifyDataSetChanged();
    }

    public class Holder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView title;
        ImageButton downloadBt;
        public Holder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.tv_episode_title);
            downloadBt = itemView.findViewById(R.id.bt_download);
            title.setOnClickListener(this);
            downloadBt.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (isDownloadedEpisode(episodeList.get(getAdapterPosition())) == -1) {
                mClickListener.onEpisodeClicked(episodeList.get(getAdapterPosition()), v);
            } else {
                mClickListener.onEpisodeClicked(isDownloadedEpisode(episodeList.get(getAdapterPosition())));
            }
        }
    }
}

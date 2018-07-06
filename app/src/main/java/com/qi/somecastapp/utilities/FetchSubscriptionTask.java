package com.qi.somecastapp.utilities;

import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.support.v7.widget.RecyclerView;

import com.qi.somecastapp.PodcastListAdapter;
import com.qi.somecastapp.database.SubscribedContract;
import com.qi.somecastapp.model.Podcast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import static com.qi.somecastapp.SubscriptionActivity.PODCAST_SCREEN;

/**
 * Created by Qi Wu on 7/6/2018.
 */
public class FetchSubscriptionTask extends AsyncTask<Integer, Void, ArrayList<Object>> {

    RecyclerView.Adapter targetAdapter;
    Context mContext;

    public FetchSubscriptionTask(RecyclerView.Adapter adapter, Context context) {
        targetAdapter = adapter;
        mContext = context;
    }

    @Override
    protected ArrayList<Object> doInBackground(Integer... integers) {
        if (integers[0] == PODCAST_SCREEN) {
            ArrayList<Object> podcasts = new ArrayList<>();
            Cursor cursor = mContext.getContentResolver().query(SubscribedContract.SubscribedEntry.CONTENT_URI, null, null, null, null);
            if (cursor != null) {
                int indexForJson = cursor.getColumnIndex(SubscribedContract.SubscribedEntry.COLUMN_PODCAST_META);
                while (cursor.moveToNext()) {
                    try {
                        JSONObject json = new JSONObject(cursor.getString(indexForJson));
                        Podcast temp = new Podcast(json);
                        podcasts.add(temp);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                cursor.close();
            }
            return podcasts;
        }
        return null;
    }

    @Override
    protected void onPostExecute(ArrayList<Object> podcasts) {
        if (targetAdapter instanceof PodcastListAdapter) {
            ArrayList<Podcast> realPodcasts = new ArrayList<>();
            for (Object p : podcasts)
                realPodcasts.add((Podcast) p);
            ((PodcastListAdapter) targetAdapter).setData(realPodcasts);
        }
    }
}

package com.qi.somecastapp.utilities;

import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;

import com.qi.somecastapp.PodcastDetailFragment;
import com.qi.somecastapp.database.SubscribedContract;

public class CheckSubscriptionStatusTask extends AsyncTask<String, Void, Boolean> {

    private Context mContext;
    private PodcastDetailFragment fragment;

    public CheckSubscriptionStatusTask(Context context, PodcastDetailFragment fragment) {
        mContext = context;
        this.fragment = fragment;
    }

    @Override
    protected Boolean doInBackground(String... strings) {
        Cursor cursor = mContext.getContentResolver().query(SubscribedContract.SubscribedEntry.CONTENT_URI, null, SubscribedContract.SubscribedEntry.COLUMN_PODCAST_ID + "= ?", strings, null);
        if (cursor != null && cursor.getCount() > 0) return true;
        return false;
    }

    @Override
    protected void onPostExecute(Boolean aBoolean) {
        fragment.setSubscribed(aBoolean);
    }
}

package com.qi.somecastapp.widget;

import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.qi.somecastapp.R;
import com.qi.somecastapp.database.SubscribedContract;
import com.qi.somecastapp.model.Podcast;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;


public class SubscriptionWidgetService extends RemoteViewsService {
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        int widgetId = intent.getData() != null ?
                Integer.valueOf(intent.getData().getSchemeSpecificPart())
                : AppWidgetManager.INVALID_APPWIDGET_ID;
        return new ListRemoteViewsFactory(this.getApplicationContext(), widgetId);    }
}

class ListRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory{

    private Context mContext;
    private BroadcastReceiver mIntentListener;
    private int widgetId;
    private ArrayList<Podcast> list = new ArrayList<>();


    ListRemoteViewsFactory(Context mContext, int id) {
        this.mContext = mContext;
        widgetId = id;
    }

    @Override
    public void onCreate() {
    }

    @Override
    public void onDataSetChanged() {
        list = querySubscription();
    }

    private ArrayList<Podcast> querySubscription() {
        ArrayList<Podcast> podcasts = new ArrayList<>();
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

    @Override
    public void onDestroy() {
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public RemoteViews getViewAt(int i) {
        RemoteViews views = new RemoteViews(mContext.getPackageName(), R.layout.podcast_widget_holder);
        views.setTextViewText(R.id.tv_show_name, list.get(i).getPodcastName());
        try {
            Bitmap b = Picasso.with(mContext).load(list.get(i).getImagePath()).get();
            views.setImageViewBitmap(R.id.iv_poster, b);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return views;
    }

    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

}
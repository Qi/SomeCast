package com.qi.somecastapp.database;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by Qi Wu on 7/6/2018.
 */
public class SubscribedContract {

    public static final String AUTHORITY = "com.qi.somecast.subscribed";

    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + AUTHORITY);

    public static final String PATH_SUBSCRIBED = "subscribed";

    public static final class SubscribedEntry implements BaseColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_SUBSCRIBED).build();
        public static final String TABLE_NAME = "subscribed";
        public static final String COLUMN_PODCAST_ID = "podcast";
        public static final String COLUMN_PODCAST_META= "JSON";
    }
}

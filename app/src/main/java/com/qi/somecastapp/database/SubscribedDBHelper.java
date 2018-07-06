package com.qi.somecastapp.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Qi Wu on 7/6/2018.
 */
public class SubscribedDBHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "subscribed.db";
    private static final int VERSION = 1;

    SubscribedDBHelper(Context context) {
        super(context, DATABASE_NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        final String CREATE_TABLE = "CREATE TABLE "  + SubscribedContract.SubscribedEntry.TABLE_NAME + " (" +
                SubscribedContract.SubscribedEntry._ID                + " INTEGER PRIMARY KEY, " +
                SubscribedContract.SubscribedEntry.COLUMN_PODCAST_META  + " TEXT NOT NULL, " +
                SubscribedContract.SubscribedEntry.COLUMN_PODCAST_ID    + " INTEGER NOT NULL);";

        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + SubscribedContract.SubscribedEntry.TABLE_NAME);
        onCreate(db);
    }
}

package com.qi.somecastapp.database;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import static com.qi.somecastapp.database.SubscribedContract.SubscribedEntry.TABLE_NAME;

/**
 * Created by Qi Wu on 7/6/2018.
 */
public class SubscribedContentProvider extends ContentProvider {

    private static final int SUBSCRIPTIONS = 100;
    private static final int SUBSCRIPTIONS_WITH_ID = 101;
    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private SubscribedDBHelper dbHelper;

    private static UriMatcher buildUriMatcher() {
        UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(SubscribedContract.AUTHORITY, SubscribedContract.PATH_SUBSCRIBED, SUBSCRIPTIONS);
        uriMatcher.addURI(SubscribedContract.AUTHORITY, SubscribedContract.PATH_SUBSCRIBED + "/*", SUBSCRIPTIONS_WITH_ID);
        return uriMatcher;
    }

    @Override
    public boolean onCreate() {
        dbHelper = new SubscribedDBHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        final SQLiteDatabase db = dbHelper.getReadableDatabase();
        int match = sUriMatcher.match(uri);
        Cursor cursor;
        switch (match) {
            case SUBSCRIPTIONS:
                cursor =  db.query(TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            case SUBSCRIPTIONS_WITH_ID:
                cursor =  db.query(TABLE_NAME,
                        projection,
                        SubscribedContract.SubscribedEntry.COLUMN_PODCAST_ID + " = ? ",
                        new String[]{String.valueOf(uri.getLastPathSegment())},
                        null,
                        null,
                        sortOrder);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        return cursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        final SQLiteDatabase db = dbHelper.getWritableDatabase();
        int match = sUriMatcher.match(uri);
        Uri returnUri;
        switch (match) {
            case SUBSCRIPTIONS:
                long id = db.insert(SubscribedContract.SubscribedEntry.TABLE_NAME, null, values);
                if ( id > 0 ) {
                    returnUri = ContentUris.withAppendedId(SubscribedContract.SubscribedEntry.CONTENT_URI, id);
                } else {
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                }
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        final SQLiteDatabase db = dbHelper.getWritableDatabase();
        int match = sUriMatcher.match(uri);
        int deleted;
        switch (match) {
            case SUBSCRIPTIONS_WITH_ID:
                String id = uri.getPathSegments().get(1);
                deleted = db.delete(SubscribedContract.SubscribedEntry.TABLE_NAME, SubscribedContract.SubscribedEntry.COLUMN_PODCAST_ID + "=?", new String[]{id});
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        if (deleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return deleted;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
}

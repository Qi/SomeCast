package com.qi.somecastapp.utilities;

import android.net.Uri;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.qi.somecastapp.BuildConfig;


import java.util.HashMap;
import java.util.Map;

/**
 * Created by PZ8MLS on 8/22/2017.
 */

public final class NetworkUtils {

    private static final String TAG = NetworkUtils.class.getSimpleName();

    public static final String BASE_GENRE_URL =
            "https://listennotes.p.mashape.com/api/v1/genres";
    public static final String BASE_PODCAST_URL =
            "https://listennotes.p.mashape.com/api/v1/best_podcasts?";

    private final static String KEY_PARAM = "X-Mashape-Key";
    private final static String LANG_PARAM = "language";
    private final static String PAGE_PRAM = "page";
    private final static String GENRE_ID_PRAM = "genre_id";
    private final static String ACCEPT = "Accept";
    private final static String ACCEPT_PARMA = "Accept";
    private static final String lang = "en-US";


    public static StringRequest getGenreList(Response.Listener<String> responseListener) {
        String uri = Uri.parse(BASE_GENRE_URL)
                .buildUpon()
                .build().toString();
        return getStringRequest(uri, responseListener);
    }

//    "https://listennotes.p.mashape.com/api/v1/best_podcasts?genre_id=125&page=2"
    public static StringRequest getPodcastList(int id, Response.Listener<String> responseListener) {
        String uri = Uri.parse(BASE_PODCAST_URL).buildUpon()
                .appendQueryParameter(GENRE_ID_PRAM, Integer.toString(id))
                .appendQueryParameter(PAGE_PRAM, "1")
                .build().toString();
        return getStringRequest(uri, responseListener);
    }

    private static StringRequest getStringRequest(String uri, Response.Listener<String> responseListener) {

        Response.ErrorListener errorListener = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("VolleyError", error.toString());
            }
        };

        StringRequest stringRequest = new StringRequest(Request.Method.GET, uri, responseListener, errorListener) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put(KEY_PARAM, BuildConfig.SOME_CAST_KEY);
                params.put(ACCEPT, ACCEPT_PARMA);
                return params;
            }
        };
        return stringRequest;
    }
}

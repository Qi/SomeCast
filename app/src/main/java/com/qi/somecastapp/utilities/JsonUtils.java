package com.qi.somecastapp.utilities;

import com.qi.somecastapp.model.Episode;
import com.qi.somecastapp.model.Genre;
import com.qi.somecastapp.model.Podcast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by Qi Wu on 8/22/2017.
 */

public class JsonUtils {
    public static ArrayList<Genre> parseGenres(String response) {
        try {
            JSONArray json = new JSONObject(response).getJSONArray("genres");
            ArrayList<Genre> genres = new ArrayList<>();
//            for (int i = 0; i < 10; i++) { //too much quires, fuck
            for (int i = 0; i < 3; i++) {
                //ignore parent_id
                genres.add(new Genre(json.getJSONObject(i).getString("name"), json.getJSONObject(i).getInt("id"), 0));
            }
            return genres;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static ArrayList<Podcast> parsePodcastInGenre(String response) {
        try {
            JSONObject responseJson = new JSONObject(response);
            JSONArray json;
            if (responseJson.has("channels")) {
                json = responseJson.getJSONArray("channels");
            } else {
                json = responseJson.getJSONArray("results");
            }
            ArrayList<Podcast> genres = new ArrayList<>();
            for (int i = 0; i < json.length(); i++) {
                genres.add(new Podcast(json.getJSONObject(i)));
            }
            return genres;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static ArrayList<Episode> parseEpisodes(String response, String podcastName, String podcastArt) {
        try {
            JSONArray json = new JSONObject(response).getJSONArray("episodes");
            ArrayList<Episode> episodes = new ArrayList<>();
            for (int i = 0; i < json.length(); i++) {
                episodes.add(new Episode(json.getJSONObject(i), podcastName, podcastArt));
            }
            return episodes;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }
}

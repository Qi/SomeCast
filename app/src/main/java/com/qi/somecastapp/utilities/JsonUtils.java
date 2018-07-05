package com.qi.somecastapp.utilities;

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
    //    public static void parseMovies(String jsonString, ArrayList<Movie> movies) {
//        try {
//            JSONObject obj = new JSONObject(jsonString);
//            JSONArray movieJsonArray = obj.getJSONArray("results");
//            for (int i = 0; i < movieJsonArray.length(); i++) {
//                Movie m = new Movie(movieJsonArray.getJSONObject(i));
//                movies.add(m);
//            }
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//    }
//
//    public static ArrayList<String> parseTrailers(String jsonString) {
//        ArrayList<String> trailerKeys = new ArrayList<>();
//        try {
//            JSONObject obj = new JSONObject(jsonString);
//            JSONArray trailers = obj.getJSONArray("results");
//            for (int i = 0; i < trailers.length(); i++) {
//                trailerKeys.add(trailers.getJSONObject(i).getString("key"));
//            }
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//        return trailerKeys;
//    }
//
//    public static boolean parseReviews(String jsonString, ArrayList<Review> reviews) {
//        ArrayList<String> trailerKeys = new ArrayList<>();
//        try {
//            JSONObject obj = new JSONObject(jsonString);
//            int totalPages = obj.getInt("total_pages");
//            int currentPage = obj.getInt("page");
//            JSONArray reviewsJsonArray = obj.getJSONArray("results");
//            for (int i = 0; i < reviewsJsonArray.length(); i++) {
//                Review review = new Review(reviewsJsonArray.getJSONObject(i).getString("author"), reviewsJsonArray.getJSONObject(i).getString("content"));
//                reviews.add(review);
//            }
//            return totalPages == currentPage;
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//        return true;
//    }
    public static ArrayList<Genre> parseGenres(String response) {
        try {
            JSONArray json = new JSONObject(response).getJSONArray("genres");
            ArrayList<Genre> genres = new ArrayList<>();
            for (int i = 0; i < json.length(); i++) {
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
            JSONArray json = new JSONObject(response).getJSONArray("channels");
            ArrayList<Podcast> genres = new ArrayList<>();
            for (int i = 0; i < json.length(); i++) {
                //ignore parent_id
                genres.add(new Podcast(json.getJSONObject(i)));
            }
            return genres;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }
}

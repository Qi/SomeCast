package com.qi.somecastapp.model;

import org.json.JSONException;
import org.json.JSONObject; /**
 * Created by Qi Wu on 6/23/2018.
 */
public class Podcast {
    private String podcastName;
    private String imagePath;

    public Podcast(JSONObject jsonObject) {
        try {
            podcastName = jsonObject.getString("title");
            imagePath = jsonObject.getString("image");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public String getPodcastName() {
        return podcastName;
    }

    public String getImagePath() {
        return imagePath;
    }
}

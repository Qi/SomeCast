package com.qi.somecastapp.model;

import org.json.JSONException;
import org.json.JSONObject; /**
 * Created by Qi Wu on 6/23/2018.
 */
public class Podcast {
    private String podcastName;
    private String imagePath;
    private String id;
    private String rawData;
    private boolean subscribed;

    public Podcast(JSONObject jsonObject) {
        try {
            rawData = jsonObject.toString();
            podcastName = jsonObject.getString("title");
            imagePath = jsonObject.getString("image");
            id = jsonObject.getString("id");
            subscribed = false;
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

    public String getId() {
        return id;
    }

    public String getRawData() {
        return rawData;
    }

    public boolean isSubscribed() {
        return subscribed;
    }

    public void setSubscribed(boolean subscribed) {
        this.subscribed = subscribed;
    }
}

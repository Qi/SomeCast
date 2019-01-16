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
    private String TAG = "Podcast";

    public Podcast(JSONObject jsonObject) {
        rawData = jsonObject.toString();
        try {
            if (jsonObject.has("title"))
                podcastName = jsonObject.getString("title");
            else
                podcastName = jsonObject.getString("title_original");
            imagePath = jsonObject.getString("image");
            id = jsonObject.getString("id");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        subscribed = false;
    }

    public Podcast() {

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

    public void setPodcastName(String name) {
        podcastName = name;
    }
}

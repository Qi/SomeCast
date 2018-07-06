package com.qi.somecastapp.model;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Qi Wu on 7/5/2018.
 */
public class Episode {
    private String title;
    private long date;
    private String description;
    private int length;
    private String id;
    private String audioPath;

    public Episode(JSONObject json) {
        try {
            title = json.getString("title");
            date = json.getLong("pub_date_ms");
            description = json.getString("description");
            length = json.getInt("audio_length");
            id = json.getString("id");
            audioPath = json.getString("audio");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public String getTitle() {
        return title;
    }

    public long getDate() {
        return date;
    }

    public String getDescription() {
        return description;
    }

    public long getLength() {
        return length;
    }

    public String getId() {
        return id;
    }

    public String getAudioPath() {
        return audioPath;
    }
}

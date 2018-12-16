package com.qi.somecastapp.model;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Qi Wu on 7/5/2018.
 */
public class Episode implements Parcelable {
    private String title;
    private long date;
    private String description;
    private int length;
    private String id;
    private String audioPath;
    private String podcastName;

    public Episode(JSONObject json, String podcastName) {
        try {
            title = json.getString("title");
            date = json.getLong("pub_date_ms");
            description = json.getString("description");
            length = json.getInt("audio_length");
            id = json.getString("id");
            audioPath = json.getString("audio");
            this.podcastName = podcastName;
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public Episode createFromParcel(Parcel in) {
            return new Episode(in);
        }

        public Episode[] newArray(int size) {
            return new Episode[size];
        }
    };

    public Episode(Parcel in) {
        title = in.readString();
        date = in.readLong();
        description = in.readString();
        length = in.readInt();
        id = in.readString();
        audioPath = in.readString();
        podcastName = in.readString();
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

    public String getPodcastName() {
        return podcastName;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public String toString() {
        return "Episode{" +
                "title='" + title + '\'' +
                ", date=" + date +
                ", description='" + description + '\'' +
                ", length=" + length +
                ", id='" + id + '\'' +
                ", audioPath='" + audioPath + '\'' +
                '}';
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(title);
        dest.writeLong(date);
        dest.writeString(description);
        dest.writeInt(length);
        dest.writeString(id);
        dest.writeString(audioPath);
        dest.writeString(podcastName);
    }
}

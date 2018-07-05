package com.qi.somecastapp.model;

/**
 * Created by Qi Wu on 7/4/2018.
 */
public class Genre {
    private String name;
    private int id;
    private int parentId;

    public String getName() {
        return name;
    }

    public int getId() {
        return id;
    }

    public int getParentId() {
        return parentId;
    }

    public Genre(String name, int id, int parentId) {
        this.name = name;
        this.id = id;
        this.parentId = parentId;
    }
}

package com.duy.project_cuoiki_calories.models;

public class SavedRecipeModel {
    private String id;
    private String content;
    private long timestamp;

    public SavedRecipeModel() {
        // Required for Firebase
    }

    public SavedRecipeModel(String id, String content, long timestamp) {
        this.id = id;
        this.content = content;
        this.timestamp = timestamp;
    }

    public String getId() {
        return id;
    }

    public String getContent() {
        return content;
    }

    public long getTimestamp() {
        return timestamp;
    }
}

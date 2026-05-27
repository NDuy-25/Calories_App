package com.duy.project_cuoiki_calories.models;

public class ChatMessage {
    public static final int TYPE_USER = 0;
    public static final int TYPE_BOT = 1;

    private String message;
    private int type;
    private boolean isSaved;

    public ChatMessage(String message, int type) {
        this.message = message;
        this.type = type;
        this.isSaved = false;
    }

    public String getMessage() {
        return message;
    }

    public int getType() {
        return type;
    }

    public boolean isSaved() {
        return isSaved;
    }

    public void setSaved(boolean saved) {
        isSaved = saved;
    }
}

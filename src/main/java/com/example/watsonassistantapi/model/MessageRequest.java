package com.example.watsonassistantapi.model;

import java.io.Serializable;

public class MessageRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    private String text;
    private String recipientId;

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getRecipientId() {
        return recipientId;
    }

    public void setRecipientId(String recipientId) {
        this.recipientId = recipientId;
    }
}

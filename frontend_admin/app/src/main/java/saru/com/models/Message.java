package saru.com.models;

import com.google.firebase.Timestamp;

public class Message {
    private String content;
    private String senderID;
    private Timestamp timestamp;

    public Message() {
        // Firestore requires a public, no-argument constructor
    }

    public Message(String content, String senderID, Timestamp timestamp) {
        this.content = content;
        this.senderID = senderID;
        this.timestamp = timestamp;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getSenderID() {
        return senderID;
    }

    public void setSenderID(String senderID) {
        this.senderID = senderID;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }
}
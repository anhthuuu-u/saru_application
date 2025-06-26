package saru.com.models;

import com.google.firebase.Timestamp;

public class Message {
    private String messageID;
    private String customerID;
    private String messageContent;
    private Timestamp timestamp;
    private String sender;

    public Message() {}

    public Message(String messageID, String customerID, String messageContent, Timestamp timestamp, String sender) {
        this.messageID = messageID;
        this.customerID = customerID;
        this.messageContent = messageContent;
        this.timestamp = timestamp;
        this.sender = sender;
    }

    public String getMessageID() { return messageID; }
    public void setMessageID(String messageID) { this.messageID = messageID; }
    public String getCustomerID() { return customerID; }
    public void setCustomerID(String customerID) { this.customerID = customerID; }
    public String getMessageContent() { return messageContent; }
    public void setMessageContent(String messageContent) { this.messageContent = messageContent; }
    public Timestamp getTimestamp() { return timestamp; }
    public void setTimestamp(Timestamp timestamp) { this.timestamp = timestamp; }
    public String getSender() { return sender; }
    public void setSender(String sender) { this.sender = sender; }
}
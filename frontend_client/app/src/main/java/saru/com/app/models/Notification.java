package saru.com.app.models;

import com.google.firebase.Timestamp;
import java.io.Serializable;

public class Notification implements Serializable {
    private String accountID;
    private String notiID;
    private Timestamp notiTime; // Thay đổi từ String sang Timestamp
    private String noti_content;
    private String id;
    private String notiTitle;

    public Notification() {
    }

    public Notification(String accountID, String notiID, Timestamp notiTime, String noti_content, String id, String notiTitle) {
        this.accountID = accountID;
        this.notiID = notiID;
        this.notiTime = notiTime;
        this.noti_content = noti_content;
        this.id = id;
        this.notiTitle = notiTitle;
    }

    public String getAccountID() {
        return accountID;
    }

    public void setAccountID(String accountID) {
        this.accountID = accountID;
    }

    public String getNotiID() {
        return notiID;
    }

    public void setNotiID(String notiID) {
        this.notiID = notiID;
    }

    public Timestamp getNotiTime() {
        return notiTime;
    }

    public void setNotiTime(Timestamp notiTime) {
        this.notiTime = notiTime;
    }

    public String getNoti_content() {
        return noti_content;
    }

    public void setNoti_content(String noti_content) {
        this.noti_content = noti_content;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNotiTitle() {
        return notiTitle;
    }

    public void setNotiTitle(String notiTitle) {
        this.notiTitle = notiTitle;
    }
}
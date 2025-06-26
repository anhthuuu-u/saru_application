package saru.com.models;

import java.io.Serializable;

public class FAQ implements Serializable {
    private String faqID;
    private String content;
    private String title;

    public FAQ() {}

    public FAQ(String faqID, String content, String title) {
        this.faqID = faqID;
        this.content = content;
        this.title = title;
    }

    public String getFaqID() { return faqID; }
    public void setFaqID(String faqID) { this.faqID = faqID; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
}
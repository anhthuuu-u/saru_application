package saru.com.models;

import java.io.Serializable;

public class FAQ implements Serializable {
    private String faqID;
    private String title;
    private String content;

    public FAQ() {
    }

    public FAQ(String faqID, String title, String content) {
        this.faqID = faqID;
        this.title = title;
        this.content = content;
    }

    // Getters
    public String getFaqID() {
        return faqID;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    // Setters
    public void setFaqID(String faqID) {
        this.faqID = faqID;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setContent(String content) {
        this.content = content;
    }
}

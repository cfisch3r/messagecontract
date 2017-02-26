package de.agiledojo.messagecontract;

import java.util.Date;

public class Message {
    private String userId;

    private String appId;

    private String contentType;

    private String contentEncoding;

    private Date timeStamp;

    private String body;


    public Message(String userId, String appId, String contentType, String contentEncoding, Date timeStamp, String body) {
        this.userId = userId;
        this.appId = appId;
        this.contentType = contentType;
        this.contentEncoding = contentEncoding;
        this.timeStamp = timeStamp;
        this.body = body;
    }

    public String getUserId() {
        return userId;
    }

    public String getAppId() {
        return appId;
    }

    public String getContentType() {
        return contentType;
    }

    public String getContentEncoding() {
        return contentEncoding;
    }

    public Date getTimeStamp() {
        return timeStamp;
    }

    public String getBody() {
        return body;
    }
}

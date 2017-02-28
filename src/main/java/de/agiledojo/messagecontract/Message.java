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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Message message = (Message) o;

        if (userId != null ? !userId.equals(message.userId) : message.userId != null) return false;
        if (appId != null ? !appId.equals(message.appId) : message.appId != null) return false;
        if (contentType != null ? !contentType.equals(message.contentType) : message.contentType != null) return false;
        if (contentEncoding != null ? !contentEncoding.equals(message.contentEncoding) : message.contentEncoding != null)
            return false;
        if (timeStamp != null ? !timeStamp.equals(message.timeStamp) : message.timeStamp != null) return false;
        return body != null ? body.equals(message.body) : message.body == null;

    }

    @Override
    public String toString() {
        return "Message{" +
                "userId='" + userId + '\'' +
                ", appId='" + appId + '\'' +
                ", contentType='" + contentType + '\'' +
                ", contentEncoding='" + contentEncoding + '\'' +
                ", timeStamp=" + timeStamp +
                ", body='" + body + '\'' +
                '}';
    }

    @Override
    public int hashCode() {
        int result = userId != null ? userId.hashCode() : 0;
        result = 31 * result + (appId != null ? appId.hashCode() : 0);
        result = 31 * result + (contentType != null ? contentType.hashCode() : 0);
        result = 31 * result + (contentEncoding != null ? contentEncoding.hashCode() : 0);
        result = 31 * result + (timeStamp != null ? timeStamp.hashCode() : 0);
        result = 31 * result + (body != null ? body.hashCode() : 0);
        return result;
    }
}

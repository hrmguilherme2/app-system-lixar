package com.spark.redis.demo.model;

// Transaction.java

// Group.java
public class Group {
   //Create a model with timestamp,user_id,event,flyer_id,merchant_id
    private String timestamp;
    private String user_id;
    private String event;
    private String flyer_id;
    private String merchant_id;

    public Group(String timestamp, String user_id, String event, String flyer_id, String merchant_id) {
        this.timestamp = timestamp;
        this.user_id = user_id;
        this.event = event;
        this.flyer_id = flyer_id;
        this.merchant_id = merchant_id;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String value) {
        this.timestamp = value;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String value) {
        this.user_id = value;
    }

    public String getEvent() {
        return event;
    }

    public void setEvent(String value) {
        this.event = value;
    }

    public String getFlyer_id() {
        return flyer_id;
    }

    public void setFlyer_id(String value) {
        this.flyer_id = value;
    }

    public String getMerchant_id() {
        return merchant_id;
    }

    public void setMerchant_id(String value) {
        this.merchant_id = value;
    }


}

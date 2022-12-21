package com.spark.redis.demo.model;

public class userInformation {
    // user_id,avg_time_on_flyer_per_user_hours
    private String user_id;
    private Double avg_time_on_flyer_per_user_hours;

    public userInformation(String user_id, Double avg_time_on_flyer_per_user_hours) {
        this.user_id = user_id;
        this.avg_time_on_flyer_per_user_hours = avg_time_on_flyer_per_user_hours;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String value) {
        this.user_id = value;
    }

    public Double getAvg_time_on_flyer_per_user_hours() {
        return avg_time_on_flyer_per_user_hours;
    }

    public void setAvg_time_on_flyer_per_user_hours(Double value) {
        this.avg_time_on_flyer_per_user_hours = value;
    }

    @Override
    public String toString() {
        return "[user_id=" + user_id + ", avg_time_on_flyer_per_user_hours=" + avg_time_on_flyer_per_user_hours + "]";
    }

}

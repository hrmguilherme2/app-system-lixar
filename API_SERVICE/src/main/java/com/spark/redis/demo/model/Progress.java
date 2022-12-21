package com.spark.redis.demo.model;

public class Progress {
    private static Progress instance = null;
    private Boolean progress;

    public Boolean getProgress() {
        return progress;
    }

    public void setProgress(Boolean value) {
        this.progress = value;
    }
    public static Progress getInstance(){
        if(instance==null){
            instance = new Progress();
        }
        return instance;
    }
}

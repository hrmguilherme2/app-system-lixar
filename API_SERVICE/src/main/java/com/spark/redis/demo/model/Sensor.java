package com.spark.redis.demo.model;

public class Sensor {
    private String sensorId;
    private String name;

    public Sensor(String sensorId, String name) {
this.sensorId = sensorId;
this.name = name;

    }

    public String getSensorId() {
        return sensorId;
    }

    public void setSensorId(String value) {
        this.sensorId = value;
    }

    public String getName() {
        return name;
    }

    public void setName(String value) {
        this.name = value;
    }

}

package com.traffic.monitoring.stream;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Traffic data model (same as producer)
 */
public class TrafficData {
    
    @JsonProperty("latitude")
    private double latitude;
    
    @JsonProperty("longitude")
    private double longitude;
    
    @JsonProperty("current_speed")
    private double currentSpeed;
    
    @JsonProperty("free_flow_speed")
    private double freeFlowSpeed;
    
    @JsonProperty("current_travel_time")
    private int currentTravelTime;
    
    @JsonProperty("free_flow_travel_time")
    private int freeFlowTravelTime;
    
    @JsonProperty("confidence")
    private double confidence;
    
    @JsonProperty("road_closure")
    private boolean roadClosure;
    
    @JsonProperty("timestamp")
    private long timestamp;
    
    // Getters and setters
    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }
    
    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }
    
    public double getCurrentSpeed() { return currentSpeed; }
    public void setCurrentSpeed(double currentSpeed) { this.currentSpeed = currentSpeed; }
    
    public double getFreeFlowSpeed() { return freeFlowSpeed; }
    public void setFreeFlowSpeed(double freeFlowSpeed) { this.freeFlowSpeed = freeFlowSpeed; }
    
    public int getCurrentTravelTime() { return currentTravelTime; }
    public void setCurrentTravelTime(int currentTravelTime) { this.currentTravelTime = currentTravelTime; }
    
    public int getFreeFlowTravelTime() { return freeFlowTravelTime; }
    public void setFreeFlowTravelTime(int freeFlowTravelTime) { this.freeFlowTravelTime = freeFlowTravelTime; }
    
    public double getConfidence() { return confidence; }
    public void setConfidence(double confidence) { this.confidence = confidence; }
    
    public boolean isRoadClosure() { return roadClosure; }
    public void setRoadClosure(boolean roadClosure) { this.roadClosure = roadClosure; }
    
    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    
    @JsonIgnore
    public double getCongestionRatio() {
        if (freeFlowSpeed == 0) return 0;
        return 1.0 - (currentSpeed / freeFlowSpeed);
    }
    
    @JsonIgnore
    public int getDelay() {
        return currentTravelTime - freeFlowTravelTime;
    }
}

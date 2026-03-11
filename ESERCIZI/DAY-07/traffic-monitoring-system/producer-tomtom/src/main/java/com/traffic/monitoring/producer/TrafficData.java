package com.traffic.monitoring.producer;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Traffic data model representing TomTom traffic flow information
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
    
    // Builder pattern
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private final TrafficData data = new TrafficData();
        
        public Builder latitude(double latitude) {
            data.latitude = latitude;
            return this;
        }
        
        public Builder longitude(double longitude) {
            data.longitude = longitude;
            return this;
        }
        
        public Builder currentSpeed(double currentSpeed) {
            data.currentSpeed = currentSpeed;
            return this;
        }
        
        public Builder freeFlowSpeed(double freeFlowSpeed) {
            data.freeFlowSpeed = freeFlowSpeed;
            return this;
        }
        
        public Builder currentTravelTime(int currentTravelTime) {
            data.currentTravelTime = currentTravelTime;
            return this;
        }
        
        public Builder freeFlowTravelTime(int freeFlowTravelTime) {
            data.freeFlowTravelTime = freeFlowTravelTime;
            return this;
        }
        
        public Builder confidence(double confidence) {
            data.confidence = confidence;
            return this;
        }
        
        public Builder roadClosure(boolean roadClosure) {
            data.roadClosure = roadClosure;
            return this;
        }
        
        public Builder timestamp(long timestamp) {
            data.timestamp = timestamp;
            return this;
        }
        
        public TrafficData build() {
            return data;
        }
    }
    
    // Getters
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }
    public double getCurrentSpeed() { return currentSpeed; }
    public double getFreeFlowSpeed() { return freeFlowSpeed; }
    public int getCurrentTravelTime() { return currentTravelTime; }
    public int getFreeFlowTravelTime() { return freeFlowTravelTime; }
    public double getConfidence() { return confidence; }
    public boolean isRoadClosure() { return roadClosure; }
    public long getTimestamp() { return timestamp; }
    
    /**
     * Calculate traffic congestion ratio
     * @return ratio between 0 (no congestion) and 1 (high congestion)
     */
    @JsonIgnore
    public double getCongestionRatio() {
        if (freeFlowSpeed == 0) return 0;
        return 1.0 - (currentSpeed / freeFlowSpeed);
    }
    
    /**
     * Calculate delay in seconds
     */
    @JsonIgnore
    public int getDelay() {
        return currentTravelTime - freeFlowTravelTime;
    }
    
    @Override
    public String toString() {
        return String.format("TrafficData[lat=%.4f, lon=%.4f, speed=%.1f/%.1f km/h, congestion=%.2f]",
                latitude, longitude, currentSpeed, freeFlowSpeed, getCongestionRatio());
    }
}

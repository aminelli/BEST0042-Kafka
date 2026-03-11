package com.traffic.monitoring.stream;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Traffic alert model
 */
public class TrafficAlert {
    
    @JsonProperty("latitude")
    private double latitude;
    
    @JsonProperty("longitude")
    private double longitude;
    
    @JsonProperty("severity")
    private String severity; // INFO, WARNING, CRITICAL
    
    @JsonProperty("congestion_ratio")
    private double congestionRatio;
    
    @JsonProperty("current_speed")
    private double currentSpeed;
    
    @JsonProperty("free_flow_speed")
    private double freeFlowSpeed;
    
    @JsonProperty("delay")
    private int delay;
    
    @JsonProperty("road_closure")
    private boolean roadClosure;
    
    @JsonProperty("timestamp")
    private long timestamp;
    
    @JsonProperty("message")
    private String message;
    
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private final TrafficAlert alert = new TrafficAlert();
        
        public Builder latitude(double latitude) {
            alert.latitude = latitude;
            return this;
        }
        
        public Builder longitude(double longitude) {
            alert.longitude = longitude;
            return this;
        }
        
        public Builder severity(String severity) {
            alert.severity = severity;
            return this;
        }
        
        public Builder congestionRatio(double congestionRatio) {
            alert.congestionRatio = congestionRatio;
            return this;
        }
        
        public Builder currentSpeed(double currentSpeed) {
            alert.currentSpeed = currentSpeed;
            return this;
        }
        
        public Builder freeFlowSpeed(double freeFlowSpeed) {
            alert.freeFlowSpeed = freeFlowSpeed;
            return this;
        }
        
        public Builder delay(int delay) {
            alert.delay = delay;
            return this;
        }
        
        public Builder roadClosure(boolean roadClosure) {
            alert.roadClosure = roadClosure;
            return this;
        }
        
        public Builder timestamp(long timestamp) {
            alert.timestamp = timestamp;
            return this;
        }
        
        public Builder message(String message) {
            alert.message = message;
            return this;
        }
        
        public TrafficAlert build() {
            return alert;
        }
    }
    
    // Getters and setters
    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }
    
    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }
    
    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }
    
    public double getCongestionRatio() { return congestionRatio; }
    public void setCongestionRatio(double congestionRatio) { this.congestionRatio = congestionRatio; }
    
    public double getCurrentSpeed() { return currentSpeed; }
    public void setCurrentSpeed(double currentSpeed) { this.currentSpeed = currentSpeed; }
    
    public double getFreeFlowSpeed() { return freeFlowSpeed; }
    public void setFreeFlowSpeed(double freeFlowSpeed) { this.freeFlowSpeed = freeFlowSpeed; }
    
    public int getDelay() { return delay; }
    public void setDelay(int delay) { this.delay = delay; }
    
    public boolean isRoadClosure() { return roadClosure; }
    public void setRoadClosure(boolean roadClosure) { this.roadClosure = roadClosure; }
    
    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}

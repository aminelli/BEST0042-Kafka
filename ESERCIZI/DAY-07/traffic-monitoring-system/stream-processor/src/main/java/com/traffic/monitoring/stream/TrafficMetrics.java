package com.traffic.monitoring.stream;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Aggregated traffic metrics over a time window
 */
public class TrafficMetrics {
    
    @JsonProperty("location_key")
    private String locationKey;
    
    @JsonProperty("window_start")
    private long windowStart;
    
    @JsonProperty("window_end")
    private long windowEnd;
    
    @JsonProperty("count")
    private int count;
    
    @JsonProperty("avg_current_speed")
    private double avgCurrentSpeed;
    
    @JsonProperty("avg_free_flow_speed")
    private double avgFreeFlowSpeed;
    
    @JsonProperty("avg_congestion_ratio")
    private double avgCongestionRatio;
    
    @JsonProperty("total_delay")
    private int totalDelay;
    
    @JsonProperty("max_congestion_ratio")
    private double maxCongestionRatio;
    
    @JsonProperty("road_closures")
    private int roadClosures;
    
    // Accumulators (not serialized)
    private transient double sumCurrentSpeed = 0;
    private transient double sumFreeFlowSpeed = 0;
    private transient double sumCongestionRatio = 0;
    
    public TrafficMetrics() {
        this.count = 0;
        this.maxCongestionRatio = 0;
        this.roadClosures = 0;
    }
    
    /**
     * Update metrics with new traffic data point
     */
    public TrafficMetrics update(TrafficData data) {
        count++;
        
        sumCurrentSpeed += data.getCurrentSpeed();
        sumFreeFlowSpeed += data.getFreeFlowSpeed();
        
        double congestionRatio = data.getCongestionRatio();
        sumCongestionRatio += congestionRatio;
        
        if (congestionRatio > maxCongestionRatio) {
            maxCongestionRatio = congestionRatio;
        }
        
        totalDelay += data.getDelay();
        
        if (data.isRoadClosure()) {
            roadClosures++;
        }
        
        // Recalculate averages
        avgCurrentSpeed = sumCurrentSpeed / count;
        avgFreeFlowSpeed = sumFreeFlowSpeed / count;
        avgCongestionRatio = sumCongestionRatio / count;
        
        return this;
    }
    
    // Getters and setters
    public String getLocationKey() { return locationKey; }
    public void setLocationKey(String locationKey) { this.locationKey = locationKey; }
    
    public long getWindowStart() { return windowStart; }
    public void setWindowStart(long windowStart) { this.windowStart = windowStart; }
    
    public long getWindowEnd() { return windowEnd; }
    public void setWindowEnd(long windowEnd) { this.windowEnd = windowEnd; }
    
    public int getCount() { return count; }
    public void setCount(int count) { this.count = count; }
    
    public double getAvgCurrentSpeed() { return avgCurrentSpeed; }
    public void setAvgCurrentSpeed(double avgCurrentSpeed) { this.avgCurrentSpeed = avgCurrentSpeed; }
    
    public double getAvgFreeFlowSpeed() { return avgFreeFlowSpeed; }
    public void setAvgFreeFlowSpeed(double avgFreeFlowSpeed) { this.avgFreeFlowSpeed = avgFreeFlowSpeed; }
    
    public double getAvgCongestionRatio() { return avgCongestionRatio; }
    public void setAvgCongestionRatio(double avgCongestionRatio) { this.avgCongestionRatio = avgCongestionRatio; }
    
    public int getTotalDelay() { return totalDelay; }
    public void setTotalDelay(int totalDelay) { this.totalDelay = totalDelay; }
    
    public double getMaxCongestionRatio() { return maxCongestionRatio; }
    public void setMaxCongestionRatio(double maxCongestionRatio) { this.maxCongestionRatio = maxCongestionRatio; }
    
    public int getRoadClosures() { return roadClosures; }
    public void setRoadClosures(int roadClosures) { this.roadClosures = roadClosures; }
}

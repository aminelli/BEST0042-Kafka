package com.netmon.common.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.util.List;

/**
 * Network telemetry data for Type A devices (Routers)
 */
public record RouterTelemetry(
    @JsonProperty("deviceId") String deviceId,
    @JsonProperty("timestamp") @JsonFormat(shape = JsonFormat.Shape.STRING) Instant timestamp,
    @JsonProperty("deviceType") String deviceType,
    @JsonProperty("metrics") RouterMetrics metrics,
    @JsonProperty("location") String location
) {
    
    public record RouterMetrics(
        @JsonProperty("bytesIn") long bytesIn,
        @JsonProperty("bytesOut") long bytesOut,
        @JsonProperty("packetsIn") long packetsIn,
        @JsonProperty("packetsOut") long packetsOut,
        @JsonProperty("errorRate") double errorRate,
        @JsonProperty("interfaces") List<NetworkInterface> interfaces
    ) {}
    
    public record NetworkInterface(
        @JsonProperty("name") String name,
        @JsonProperty("status") String status,
        @JsonProperty("speed") int speed
    ) {}
}

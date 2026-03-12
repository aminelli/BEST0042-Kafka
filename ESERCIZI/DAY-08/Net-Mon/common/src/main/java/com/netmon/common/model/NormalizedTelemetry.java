package com.netmon.common.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.util.Map;

/**
 * Normalized telemetry data
 */
public record NormalizedTelemetry(
    @JsonProperty("normalizedId") String normalizedId,
    @JsonProperty("originalDeviceId") String originalDeviceId,
    @JsonProperty("deviceType") String deviceType,
    @JsonProperty("timestamp") @JsonFormat(shape = JsonFormat.Shape.STRING) Instant timestamp,
    @JsonProperty("location") String location,
    @JsonProperty("metrics") NormalizedMetrics metrics,
    @JsonProperty("rawData") Map<String, Object> rawData,
    @JsonProperty("processingTimestamp") @JsonFormat(shape = JsonFormat.Shape.STRING) Instant processingTimestamp,
    @JsonProperty("version") String version
) {
    
    public record NormalizedMetrics(
        @JsonProperty("totalBytesTransferred") Long totalBytesTransferred,
        @JsonProperty("totalPackets") Long totalPackets,
        @JsonProperty("errorRate") Double errorRate,
        @JsonProperty("utilization") Double utilization,
        @JsonProperty("activeConnections") Integer activeConnections
    ) {}
}

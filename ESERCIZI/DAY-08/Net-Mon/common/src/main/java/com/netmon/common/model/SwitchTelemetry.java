package com.netmon.common.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

/**
 * Network telemetry data for Type B devices (Switches)
 */
public record SwitchTelemetry(
    @JsonProperty("id") String id,
    @JsonProperty("ts") long ts,
    @JsonProperty("type") String type,
    @JsonProperty("stats") SwitchStats stats,
    @JsonProperty("site") String site,
    @JsonProperty("firmware") String firmware
) {
    
    public record SwitchStats(
        @JsonProperty("totalTraffic") long totalTraffic,
        @JsonProperty("activeConnections") int activeConnections,
        @JsonProperty("droppedPackets") int droppedPackets,
        @JsonProperty("utilization") double utilization,
        @JsonProperty("vlanStats") Map<String, VlanStats> vlanStats
    ) {}
    
    public record VlanStats(
        @JsonProperty("traffic") long traffic,
        @JsonProperty("devices") int devices
    ) {}
}

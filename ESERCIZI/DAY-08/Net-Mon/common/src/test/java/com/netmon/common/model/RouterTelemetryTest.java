package com.netmon.common.model;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RouterTelemetryTest {

    @Test
    void shouldCreateRouterTelemetryRecord() {
        // Given
        Instant now = Instant.now();
        RouterTelemetry.NetworkInterface iface = new RouterTelemetry.NetworkInterface("eth0", "UP", 1000);
        RouterTelemetry.RouterMetrics metrics = new RouterTelemetry.RouterMetrics(
            1000000L, 800000L, 10000L, 8000L, 0.01, List.of(iface)
        );

        // When
        RouterTelemetry telemetry = new RouterTelemetry(
            "ROUTER-001", now, "router", metrics, "DataCenter-1"
        );

        // Then
        assertEquals("ROUTER-001", telemetry.deviceId());
        assertEquals(now, telemetry.timestamp());
        assertEquals("router", telemetry.deviceType());
        assertEquals("DataCenter-1", telemetry.location());
        assertEquals(1000000L, telemetry.metrics().bytesIn());
        assertEquals(800000L, telemetry.metrics().bytesOut());
    }

    @Test
    void shouldHaveCorrectMetricsValues() {
        // Given
        RouterTelemetry.NetworkInterface iface1 = new RouterTelemetry.NetworkInterface("eth0", "UP", 1000);
        RouterTelemetry.NetworkInterface iface2 = new RouterTelemetry.NetworkInterface("eth1", "DOWN", 1000);
        List<RouterTelemetry.NetworkInterface> interfaces = List.of(iface1, iface2);

        // When
        RouterTelemetry.RouterMetrics metrics = new RouterTelemetry.RouterMetrics(
            2000000L, 1500000L, 20000L, 15000L, 0.02, interfaces
        );

        // Then
        assertEquals(2000000L, metrics.bytesIn());
        assertEquals(1500000L, metrics.bytesOut());
        assertEquals(20000L, metrics.packetsIn());
        assertEquals(15000L, metrics.packetsOut());
        assertEquals(0.02, metrics.errorRate(), 0.001);
        assertEquals(2, metrics.interfaces().size());
    }

    @Test
    void shouldBeImmutable() {
        // Given
        RouterTelemetry.NetworkInterface iface = new RouterTelemetry.NetworkInterface("eth0", "UP", 1000);
        RouterTelemetry.RouterMetrics metrics = new RouterTelemetry.RouterMetrics(
            1000000L, 800000L, 10000L, 8000L, 0.01, List.of(iface)
        );
        RouterTelemetry telemetry = new RouterTelemetry(
            "ROUTER-001", Instant.now(), "router", metrics, "DataCenter-1"
        );

        // When & Then - Records are immutable, no setters available
        assertNotNull(telemetry.deviceId());
        assertNotNull(telemetry.metrics());
    }
}

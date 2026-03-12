package com.netmon.common.util;

import com.netmon.common.model.RouterTelemetry;
import com.netmon.common.model.SwitchTelemetry;
import com.netmon.common.model.NormalizedTelemetry;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class JsonUtilTest {

    @Test
    void shouldSerializeRouterTelemetryToJson() {
        // Given
        RouterTelemetry.NetworkInterface iface = new RouterTelemetry.NetworkInterface("eth0", "UP", 1000);
        RouterTelemetry.RouterMetrics metrics = new RouterTelemetry.RouterMetrics(
            1000000L, 800000L, 10000L, 8000L, 0.01, List.of(iface)
        );
        RouterTelemetry telemetry = new RouterTelemetry(
            "ROUTER-001", Instant.parse("2026-03-12T10:00:00Z"), "router", metrics, "DataCenter-1"
        );

        // When
        String json = JsonUtil.toJson(telemetry);

        // Then
        assertNotNull(json);
        assertTrue(json.contains("ROUTER-001"));
        assertTrue(json.contains("DataCenter-1"));
        assertTrue(json.contains("eth0"));
    }

    @Test
    void shouldDeserializeJsonToRouterTelemetry() {
        // Given
        String json = """
            {
                "deviceId": "ROUTER-001",
                "timestamp": "2026-03-12T10:00:00Z",
                "deviceType": "router",
                "metrics": {
                    "bytesIn": 1000000,
                    "bytesOut": 800000,
                    "packetsIn": 10000,
                    "packetsOut": 8000,
                    "errorRate": 0.01,
                    "interfaces": [
                        {"name": "eth0", "status": "UP", "speed": 1000}
                    ]
                },
                "location": "DataCenter-1"
            }
            """;

        // When
        RouterTelemetry telemetry = JsonUtil.fromJson(json, RouterTelemetry.class);

        // Then
        assertNotNull(telemetry);
        assertEquals("ROUTER-001", telemetry.deviceId());
        assertEquals("router", telemetry.deviceType());
        assertEquals("DataCenter-1", telemetry.location());
        assertEquals(1000000L, telemetry.metrics().bytesIn());
    }

    @Test
    void shouldSerializeSwitchTelemetryToJson() {
        // Given
        Map<String, SwitchTelemetry.VlanStats> vlanStats = Map.of(
            "vlan10", new SwitchTelemetry.VlanStats(500000L, 45)
        );
        SwitchTelemetry.SwitchStats stats = new SwitchTelemetry.SwitchStats(
            2000000L, 250, 10, 65.5, vlanStats
        );
        SwitchTelemetry telemetry = new SwitchTelemetry(
            "SWITCH-042", System.currentTimeMillis(), "switch", stats, "Office-1", "v2.3.1"
        );

        // When
        String json = JsonUtil.toJson(telemetry);

        // Then
        assertNotNull(json);
        assertTrue(json.contains("SWITCH-042"));
        assertTrue(json.contains("Office-1"));
        assertTrue(json.contains("vlan10"));
    }

    @Test
    void shouldDeserializeJsonToSwitchTelemetry() {
        // Given
        String json = """
            {
                "id": "SWITCH-042",
                "ts": 1710239445123,
                "type": "switch",
                "stats": {
                    "totalTraffic": 2000000,
                    "activeConnections": 250,
                    "droppedPackets": 10,
                    "utilization": 65.5,
                    "vlanStats": {
                        "vlan10": {"traffic": 500000, "devices": 45}
                    }
                },
                "site": "Office-1",
                "firmware": "v2.3.1"
            }
            """;

        // When
        SwitchTelemetry telemetry = JsonUtil.fromJson(json, SwitchTelemetry.class);

        // Then
        assertNotNull(telemetry);
        assertEquals("SWITCH-042", telemetry.id());
        assertEquals("switch", telemetry.type());
        assertEquals("Office-1", telemetry.site());
        assertEquals(2000000L, telemetry.stats().totalTraffic());
    }

    @Test
    void shouldSerializeNormalizedTelemetryToJson() {
        // Given
        NormalizedTelemetry.NormalizedMetrics metrics = new NormalizedTelemetry.NormalizedMetrics(
            1800000L, 18000L, 0.01, 65.5, 250
        );
        NormalizedTelemetry telemetry = new NormalizedTelemetry(
            "uuid-123", "DEVICE-001", "router", 
            Instant.parse("2026-03-12T10:00:00Z"), "DataCenter-1",
            metrics, Map.of("raw", "data"),
            Instant.parse("2026-03-12T10:00:01Z"), "1.0"
        );

        // When
        String json = JsonUtil.toJson(telemetry);

        // Then
        assertNotNull(json);
        assertTrue(json.contains("uuid-123"));
        assertTrue(json.contains("DEVICE-001"));
    }

    @Test
    void shouldHandleNullValuesInJson() {
        // Given
        NormalizedTelemetry.NormalizedMetrics metrics = new NormalizedTelemetry.NormalizedMetrics(
            1800000L, null, null, 65.5, null
        );
        NormalizedTelemetry telemetry = new NormalizedTelemetry(
            "uuid-123", "DEVICE-001", "router",
            Instant.now(), "DataCenter-1", metrics, Map.of(),
            Instant.now(), "1.0"
        );

        // When
        String json = JsonUtil.toJson(telemetry);
        NormalizedTelemetry deserialized = JsonUtil.fromJson(json, NormalizedTelemetry.class);

        // Then
        assertNotNull(json);
        assertNotNull(deserialized);
        assertNull(deserialized.metrics().totalPackets());
        assertNull(deserialized.metrics().errorRate());
    }

    @Test
    void shouldConvertToJsonBytes() {
        // Given
        RouterTelemetry.NetworkInterface iface = new RouterTelemetry.NetworkInterface("eth0", "UP", 1000);
        RouterTelemetry.RouterMetrics metrics = new RouterTelemetry.RouterMetrics(
            1000000L, 800000L, 10000L, 8000L, 0.01, List.of(iface)
        );
        RouterTelemetry telemetry = new RouterTelemetry(
            "ROUTER-001", Instant.now(), "router", metrics, "DataCenter-1"
        );

        // When
        byte[] bytes = JsonUtil.toJsonBytes(telemetry);

        // Then
        assertNotNull(bytes);
        assertTrue(bytes.length > 0);
    }

    @Test
    void shouldThrowExceptionForInvalidJson() {
        // Given
        String invalidJson = "{ invalid json }";

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            JsonUtil.fromJson(invalidJson, RouterTelemetry.class);
        });
    }
}

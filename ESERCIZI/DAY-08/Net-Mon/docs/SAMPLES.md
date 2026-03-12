# Network Monitoring System - Test Data Samples

## Router Telemetry Sample (Type A)

```json
{
  "deviceId": "ROUTER-001",
  "timestamp": "2026-03-12T10:30:45.123Z",
  "deviceType": "router",
  "metrics": {
    "bytesIn": 1024567,
    "bytesOut": 892345,
    "packetsIn": 15234,
    "packetsOut": 13421,
    "errorRate": 0.02,
    "interfaces": [
      {"name": "eth0", "status": "UP", "speed": 1000},
      {"name": "eth1", "status": "UP", "speed": 1000}
    ]
  },
  "location": "DataCenter-1"
}
```

## Switch Telemetry Sample (Type B)

```json
{
  "id": "SWITCH-042",
  "ts": 1710239445123,
  "type": "switch",
  "stats": {
    "totalTraffic": 2048934,
    "activeConnections": 245,
    "droppedPackets": 12,
    "utilization": 67.5,
    "vlanStats": {
      "vlan10": {"traffic": 500000, "devices": 45},
      "vlan20": {"traffic": 350000, "devices": 32}
    }
  },
  "site": "Office-Building-2",
  "firmware": "v2.3.1"
}
```

## Normalized Telemetry Sample

```json
{
  "normalizedId": "550e8400-e29b-41d4-a716-446655440000",
  "originalDeviceId": "ROUTER-001",
  "deviceType": "router",
  "timestamp": "2026-03-12T10:30:45.123Z",
  "location": "DataCenter-1",
  "metrics": {
    "totalBytesTransferred": 1916912,
    "totalPackets": 28655,
    "errorRate": 0.02,
    "utilization": null,
    "activeConnections": null
  },
  "rawData": {
    "deviceId": "ROUTER-001",
    "timestamp": "2026-03-12T10:30:45.123Z",
    "deviceType": "router",
    "metrics": {
      "bytesIn": 1024567,
      "bytesOut": 892345,
      "packetsIn": 15234,
      "packetsOut": 13421,
      "errorRate": 0.02,
      "interfaces": [
        {"name": "eth0", "status": "UP", "speed": 1000},
        {"name": "eth1", "status": "UP", "speed": 1000}
      ]
    },
    "location": "DataCenter-1"
  },
  "processingTimestamp": "2026-03-12T10:30:47.456Z",
  "version": "1.0"
}
```

## Kibana Query Examples

### Count by device type
```json
{
  "aggs": {
    "device_types": {
      "terms": {
        "field": "deviceType"
      }
    }
  }
}
```

### Average utilization over time
```json
{
  "aggs": {
    "utilization_over_time": {
      "date_histogram": {
        "field": "timestamp",
        "interval": "1m"
      },
      "aggs": {
        "avg_utilization": {
          "avg": {
            "field": "metrics.utilization"
          }
        }
      }
    }
  }
}
```

### Top devices by traffic
```json
{
  "size": 0,
  "aggs": {
    "top_devices": {
      "terms": {
        "field": "originalDeviceId",
        "size": 10,
        "order": {
          "total_bytes": "desc"
        }
      },
      "aggs": {
        "total_bytes": {
          "sum": {
            "field": "metrics.totalBytesTransferred"
          }
        }
      }
    }
  }
}
```

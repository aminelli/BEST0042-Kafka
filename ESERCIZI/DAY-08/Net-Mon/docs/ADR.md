# Architecture Decision Records (ADR)

## ADR-001: Java Records for Data Models

**Status:** Accepted

**Context:** Need immutable data structures for telemetry data

**Decision:** Use Java 21 Records for all data models

**Consequences:**
- ✅ Immutable by default
- ✅ Concise syntax
- ✅ Built-in equals/hashCode/toString
- ⚠️ Requires Java 21+

---

## ADR-002: Spark Structured Streaming over Kafka Streams

**Status:** Accepted

**Context:** Need to normalize data from multiple Kafka topics

**Decision:** Use Apache Spark Structured Streaming instead of Kafka Streams

**Consequences:**
- ✅ Better for complex transformations
- ✅ Unified batch/streaming API
- ✅ Advanced windowing and watermarking
- ⚠️ Higher resource consumption
- ⚠️ Additional complexity

---

## ADR-003: Rolling Daily Indices in Elasticsearch

**Status:** Accepted

**Context:** Need efficient time-series data storage

**Decision:** Use daily rolling indices with pattern `network-telemetry-YYYY.MM.DD`

**Consequences:**
- ✅ Better query performance for time-based queries
- ✅ Easy data retention management
- ✅ Simplified backup/restore
- ⚠️ Requires index template configuration

---

## ADR-004: Bulk Indexing with Manual Commit

**Status:** Accepted

**Context:** Need high-throughput indexing to Elasticsearch

**Decision:** Use bulk indexing with manual Kafka offset commits

**Consequences:**
- ✅ Higher throughput
- ✅ Better resource utilization
- ✅ Exactly-once semantics
- ⚠️ Requires careful error handling
- ⚠️ Increased code complexity

---

## ADR-005: Virtual Threads for Concurrent Processing

**Status:** Accepted

**Context:** Need lightweight concurrency for producers

**Decision:** Use Java 21 Virtual Threads

**Consequences:**
- ✅ Lightweight threading model
- ✅ Simplified concurrent code
- ✅ Better resource utilization
- ⚠️ Requires Java 21+
- ⚠️ Limited debugging tools support

---

## ADR-006: Docker Multi-Stage Builds

**Status:** Accepted

**Context:** Need optimized Docker images

**Decision:** Use multi-stage builds with separate build and runtime stages

**Consequences:**
- ✅ Smaller final images
- ✅ Faster deployment
- ✅ Better layer caching
- ⚠️ Longer initial build time

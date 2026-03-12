# Test Coverage Report

This document provides information about the test suite for the Network Monitoring System.

## Test Structure

### Unit Tests
Each module contains unit tests for its main components:

#### Common Module
- `JsonUtilTest` - Tests for JSON serialization/deserialization
- `RouterTelemetryTest` - Tests for Router telemetry model
- Tests for all Record models

#### Producer Type A
- `RouterTelemetryProducerTest` - Tests for router producer initialization and shutdown

#### Producer Type B
- `SwitchTelemetryProducerTest` - Tests for switch producer initialization and shutdown

#### Spark Normalizer
- `TelemetryNormalizerTest` - Tests for data normalization logic

#### Elasticsearch Consumer
- `ElasticsearchConsumerTest` - Tests for consumer initialization and shutdown

## Running Tests

### All Tests
```bash
# Linux/Mac
./scripts/run-tests.sh

# Windows
.\scripts\run-tests.bat

# Maven directly
mvn clean test
```

### Specific Module
```bash
mvn clean test -pl common
mvn clean test -pl producer-type-a
mvn clean test -pl producer-type-b
mvn clean test -pl spark-normalizer
mvn clean test -pl elasticsearch-consumer
```

### With Coverage
```bash
mvn clean verify
```

## Test Coverage Goals

- **Target Coverage:** 80%
- **Current Coverage:** See reports in `target/surefire-reports/`

## Integration Testing

For integration tests with actual Kafka and Elasticsearch:

1. Start infrastructure:
```bash
docker-compose up -d zookeeper kafka elasticsearch
```

2. Wait for services to be ready (30 seconds)

3. Run integration tests:
```bash
mvn verify -P integration-tests
```

## Test Reports

After running tests, reports are available in:
- HTML Reports: `<module>/target/surefire-reports/`
- JUnit XML: `<module>/target/surefire-reports/*.xml`

## Continuous Integration

Tests are designed to run in CI/CD pipelines:
- Fast execution (< 30 seconds for unit tests)
- No external dependencies for unit tests
- Isolated test cases
- Deterministic results

## Best Practices

1. **Isolation**: Each test is independent
2. **Fast**: Unit tests complete in milliseconds
3. **Readable**: Clear Given-When-Then structure
4. **Maintainable**: One assertion per test when possible
5. **Comprehensive**: Cover happy path and edge cases

## Adding New Tests

When adding new functionality:

1. Write the test first (TDD)
2. Follow naming convention: `should<Action><Condition>`
3. Use JUnit 5 annotations
4. Keep tests focused and simple
5. Aim for >80% code coverage

## Troubleshooting

### Tests Fail to Run
- Ensure Java 21 is installed
- Check Maven is configured correctly
- Verify all dependencies are available

### Kafka/Elasticsearch Tests Fail
- Ensure Docker is running
- Check ports 9092 and 9200 are available
- Wait sufficient time for services to start

### Coverage Reports Not Generated
- Run `mvn verify` instead of `mvn test`
- Check JaCoCo plugin is configured in pom.xml

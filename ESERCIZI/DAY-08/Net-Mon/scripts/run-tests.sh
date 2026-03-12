#!/bin/bash

# Run all tests for Network Monitoring System

echo "======================================"
echo "Running Tests - Network Monitoring System"
echo "======================================"
echo ""

# Run unit tests
echo "🧪 Running unit tests..."
mvn clean test

if [ $? -eq 0 ]; then
    echo "✅ Unit tests passed!"
else
    echo "❌ Unit tests failed!"
    exit 1
fi

echo ""
echo "======================================"
echo "✅ All tests completed successfully!"
echo "======================================"
echo ""

# Generate test coverage report
echo "📊 Generating test coverage report..."
mvn verify

if [ $? -eq 0 ]; then
    echo "✅ Coverage report generated!"
    echo ""
    echo "Test reports available in:"
    echo "  - common/target/surefire-reports/"
    echo "  - producer-type-a/target/surefire-reports/"
    echo "  - producer-type-b/target/surefire-reports/"
    echo "  - spark-normalizer/target/surefire-reports/"
    echo "  - elasticsearch-consumer/target/surefire-reports/"
else
    echo "⚠️ Coverage report generation failed"
fi

echo ""

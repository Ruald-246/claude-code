#!/bin/bash

echo "Starting Mock Authentication API..."
echo "Server will run on http://localhost:8080"
echo "Press Ctrl+C to stop"
echo ""

cd mock-api
kotlin -classpath "$(find ~/.gradle/caches -name '*.jar' | tr '\n' ':')" src/main/kotlin/com/example/mockapi/Application.kt 2>/dev/null || {
    echo "Using Java to compile and run..."
    javac -d build/classes src/main/kotlin/com/example/mockapi/Application.kt 2>/dev/null || {
        echo "Please use Gradle to run: ./gradlew :mock-api:run"
        exit 1
    }
}

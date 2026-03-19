#!/bin/bash
export $(grep '.*' .env | xargs)

echo "Running migrations before startup..."
clojure -M:migrate

echo "Starting server..."
$JAVA_CMD -jar target/source-be-standalone.jar

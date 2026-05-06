#!/usr/bin/env bash

export $(grep '.*' .env | xargs)

echo "Running migrations..."
clojure -M:migrate "$@"

#!/usr/bin/env bash
# export $(grep '.*' .env | xargs)

clojure -M:migrate

java -jar target/source-be-standalone.jar

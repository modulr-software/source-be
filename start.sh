#!/usr/bin/env bash
export $(grep '.*' .env | xargs)

export JAVA_CMD="/home/merv/.jenv/shims/java"
clojure -M:migrate

/home/merv/.jenv/shims/java -jar target/source-be-standalone.jar

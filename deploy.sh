#!/usr/bin/env bash

systemctl stop source-be.service

export $(grep '.*' .env | xargs)

echo "Pulling latest changes..."
git pull
echo "Starting compilation..."
export JAVA_CMD="/home/merv/.jenv/shims/java"
clojure -T:build uber

systemctl start source-be.service

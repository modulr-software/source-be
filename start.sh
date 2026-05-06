#!/bin/bash

cd /home/deploy/source-be-deploy
export $(grep '.*' .env | xargs)

./migrate.sh up

echo "Starting backend server..."
/home/deploy/.jenv/shims/java -jar source-be-standalone.jar

#!/bin/bash

cd /home/merv/Developer/source-be-deploy

echo "Pulling changes..."
git pull

echo "Starting compilation..."
export $(grep '.*' .env | xargs)
clojure -T:build uber

echo "Creating archive..."
mkdir source-be-deploy
cp start.sh migrate.sh source-be.service deps.edn target/source-be-standalone.jar source-be-deploy
cp -r resources src source-be-deploy
cp prod.env source-be-deploy/.env
tar -czf source-be-deploy.tar.gz ./source-be-deploy

echo "Copying archive..."
sshpass -e scp -r /home/merv/Developer/source-be-deploy/source-be-deploy.tar.gz deploy@api.wearesource.earth:/home/deploy
echo "Done!"

echo "Setting up prod server..."
sshpass -e ssh deploy@api.wearesource.earth "
	echo "Extracting archive..."
	tar -xzf source-be-deploy.tar.gz

	echo "Restarting service..."
	sudo systemctl restart source-fe.service
"

echo "Cleaning up..."
rm -rf source-be-deploy
rm source-be-deploy.tar.gz

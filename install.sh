#!/bin/bash

echo "Installing service..."
chmod +x deploy.sh
chmod +x install_runner.sh
chmod +x migrate.sh

./deploy.sh

sshpass -e ssh deploy@api.wearesource.earth "
	cd /home/deploy/source-be-deploy
	chmod +x start.sh

	sudo mv source-be.service /etc/systemd/system
	sudo systemctl daemon-reload
	sudo systemctl enable source-be.service
	sudo systemctl start source-be.service
"

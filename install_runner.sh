#!/bin/bash

mkdir -p ~/.config/source/actions-runner
cd ~/.config/source/actions-runner

curl -o actions-runner-linux-x64-2.334.0.tar.gz -L https://github.com/actions/runner/releases/download/v2.334.0/actions-runner-linux-x64-2.334.0.tar.gz

tar xzf ./actions-runner-linux-x64-2.334.0.tar.gz

./config.sh --url https://github.com/modulr-software/source --token AMFR4MIUDHJ2SIGAWODFXYDJ7HEWO

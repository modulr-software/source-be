#!/bin/bash

mkdir -p ~/.config/source-be/actions-runner
cd ~/.config/source-be/actions-runner

curl -o actions-runner-linux-x64-2.334.0.tar.gz -L https://github.com/actions/runner/releases/download/v2.334.0/actions-runner-linux-x64-2.334.0.tar.gz

tar xzf ./actions-runner-linux-x64-2.334.0.tar.gz

./config.sh --url https://github.com/modulr-software/source-be --token AMFR4ML2O5TLDW2O5JR6KCTJ7H7U2

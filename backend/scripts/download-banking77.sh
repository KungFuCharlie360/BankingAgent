#!/usr/bin/env sh
set -eu
base="src/main/resources/sample-data/banking77"
mkdir -p "$base"
curl -L --fail https://raw.githubusercontent.com/PolyAI-LDN/task-specific-datasets/master/banking_data/train.csv -o "$base/train.csv"
curl -L --fail https://raw.githubusercontent.com/PolyAI-LDN/task-specific-datasets/master/banking_data/test.csv -o "$base/test.csv"

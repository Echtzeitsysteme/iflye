#!/bin/bash

set -e

# Set env vars
source env.sh

export OUTPUT="idyve-output/"
mkdir -p $OUTPUT

# Config
export DATE=$(date +"%FT%H-%M-%S")
export JAR="idyve.jar"
export ARGS="mdvne 4 two-tier 2 $OUTPUT 600 0 NEVER"

# Extract ibex-patterns.xmi file
unzip -o $JAR "*/ibex-patterns.xmi"
rsync -a ./src-gen/* ./
rm -r ./src-gen

# Execute the program itself and save its output to logfile
mkdir -p logs

java -Xmx32g -jar $JAR $ARGS 2>&1 | tee "./logs/$DATE.log"
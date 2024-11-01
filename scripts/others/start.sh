#!/bin/bash

set -e

# Set env vars
source env.sh

# Config
export DATE=$(date +%Y-%m-%d"_"%H-%M-%S)
export JAR="iflye.jar"
export ARGS="-a pm -o total-comm-a -e emoflon_wo_update -l 2 -s resources/two-tier-4-pods/snet.json -v resources/40-vnets/vnets.json -c metrics/$DATE.csv -i 600"

# Make sure that folder for hipe-network exists
mkdir -p bin
mkdir -p metrics
mkdir -p resources

# Get resources from scenario project
if ! [[ "$(find ./resources -maxdepth 3 -type f -iname \*.json)" ]];
then 
    rsync -a ../vne.scenarios/resources .
fi

# Extract hipe-network.xmi file
unzip -o $JAR "*/hipe-network.xmi"
rsync -a ./rules ./bin
rm -r ./rules

# Execute the program itself and save its output to logfile
mkdir -p logs
java -Xmx32g -jar $JAR $ARGS 2>&1 | tee "./logs/$DATE.log"

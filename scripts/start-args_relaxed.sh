#!/bin/bash

set -e

function setup {
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

    mkdir -p logs
}

function run {
    # Execute the program itself and save its output to logfile
    java -Xmx120g -jar $JAR $ARGS 2>&1 | tee "./logs/$RUN_NAME.log"
}

# Set env vars
source env.sh

# Config
export JAR="iflye.jar"

setup

# Example arguments:
# pm two-tier-4-pods 4 1 3
# $1 $2              $3 $4 $5
# pm two-tier-4-pods auto 1 3

export a=$1 # algorithm
export s=$2 # scenario
export l=$3 # maximum path length
export k=$4 # k fastest paths to generate
export r=$5 # number of runs

for ((i=1;i<=$r;i++));
do
    # Without memory measurement
    export RUN_NAME="${a}_${s}_l${l}_k${k}_relaxed_scaled_run${i}"
    export ARGS="-a $a -o total-comm-c -e emoflon_wo_update -l $l -k $k -s resources/$s/snet.json -v resources/$s/vnets.json -c ./metrics/$RUN_NAME.csv -i 600 --ilpopttol 0.01 --ilpobjscaling 0.001"
    echo "#"
    echo "# => Using ARGS: $ARGS"
    echo "#"
    run
done

echo "#"
echo "# => Arg script done."
echo "#"

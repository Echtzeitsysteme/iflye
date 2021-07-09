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
    java -Xmx32g -jar $JAR $ARGS 2>&1 | tee "./logs/$RUN_NAME.log"
}

# Set env vars
source env.sh

# Config
export JAR="iflye.jar"
algorithms=(pm pm-update ilp)
scenarios=(two-tier-4-pods two-tier-8-pods two-tier-12-pods fat-tree-4-pods fat-tree-8-pods)
path_lengths=(2 3 4)
k_paths=(1 2)

setup

for a in "${algorithms[@]}"
do
    for s in "${scenarios[@]}"
    do
        for l in "${path_lengths[@]}"
        do
            for k in "${k_paths[@]}"
            do
                export RUN_NAME="${a}_${s}_l${l}_k${k}"
                export ARGS="-a $a -o total-comm-c -e emoflon_wo_update -l $l -k $k -s resources/$s/snet.json -v resources/$s/vnets.json -c ./metrics/$RUN_NAME.csv -i 600"
                echo "#"
                echo "# => Using ARGS: $ARGS"
                echo "#"
                run
            done
        done
    done
done

echo "#"
echo "# => Sweep script done."
echo "#"

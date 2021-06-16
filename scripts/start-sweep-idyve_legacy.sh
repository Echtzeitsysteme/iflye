#!/bin/bash

set -e

function setup {
    # Extract hipe-network.xmi file
    unzip -o $JAR "*/ibex-patterns.xmi"
    rsync -a ./src-gen/* ./
    rm -r ./src-gen

    mkdir -p logs
}

function run {
    # Execute the program itself and save its output to logfile
    java -Xmx32g -jar $JAR $ARGS 2>&1 | tee "./logs/$RUN_NAME.log"
}

# Set env vars
source env.sh

# Config
export JAR="idyve.jar"
export OUTPUT="idyve-output/"
algorithms=(mdvne)
scenarios=(two-tier fat-tree)
path_lengths=(2 3)
pod_size=(4)

setup

for a in "${algorithms[@]}"
do
    for s in "${scenarios[@]}"
    do
        for l in "${path_lengths[@]}"
        do
            for p in "${pod_size[@]}"
            do
                export RUN_NAME="${a}_${s}-${p}-pods_l${l}_k1"
                mkdir -p $OUTPUT$RUN_NAME
                export ARGS="$a $p $s $l $OUTPUT$RUN_NAME/"
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

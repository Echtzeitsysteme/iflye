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
    java -Xmx120g -jar $JAR $ARGS 2>&1 | tee "./logs/$RUN_NAME.log"
}

# Set env vars
source env.sh

# Config
export JAR="idyve.jar"
export OUTPUT="idyve-output/"

setup

# Example arguments:
# mdvne two-tier 4 12 3
# $1    $2       $3 $4 $5

export a=$1 # algorithm
export s=$2 # scenario type
export l=$3 # maximum path length
export p=$4 # number of racks/pods
export r=$5 # number of runs

for ((i=1;i<=$r;i++));
do
    # Without memory measurement
    export RUN_NAME="${a}_${s}-${p}-pods_l${l}_k1_run${i}"
    mkdir -p $OUTPUT$RUN_NAME
    export ARGS="$a $p $s $l $OUTPUT$RUN_NAME/ 600 0 NEVER 40"
    echo "#"
    echo "# => Using ARGS: $ARGS"
    echo "#"
    run

    # With memory measurement
    export RUN_NAME="${a}_${s}-${p}-pods_l${l}_k1_run${i}_mem"
    mkdir -p $OUTPUT$RUN_NAME
    export ARGS="$a $p $s $l $OUTPUT$RUN_NAME/ 600 0 NEVER 40 memmeasurement"
    echo "#"
    echo "# => Using ARGS: $ARGS"
    echo "#"
    run
done

echo "#"
echo "# => Arg script done."
echo "#"

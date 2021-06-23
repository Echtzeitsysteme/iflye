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
    java -Xmx64g -jar $JAR $ARGS 2>&1 | tee "./logs/$RUN_NAME.log"
}

# Set env vars
source env.sh

# Config
export JAR="idyve.jar"
export OUTPUT="idyve-output/"

setup

# Example arguments:
# mdvne two-tier 4 12
# $1    $2       $3 $4

export a=$1
export s=$2
export l=$3
export p=$4

export RUN_NAME="${a}_${s}-${p}-pods_l${l}_k1"
mkdir -p $OUTPUT$RUN_NAME
export ARGS="$a $p $s $l $OUTPUT$RUN_NAME/ 600 0 ALWAYS_FREE"
echo "#"
echo "# => Using ARGS: $ARGS"
echo "#"
run

echo "#"
echo "# => Arg script done."
echo "#"

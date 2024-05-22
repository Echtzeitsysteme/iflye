#!/bin/bash

set -e

function setup {
    # Make sure that folder for hipe-network exists
    mkdir -p bin
    mkdir -p metrics
    mkdir -p resources

    # Extract hipe-network.xmi file
    unzip -o $JAR "network/model/rules/*/hipe-network.xmi"
    rsync -a ./network ./bin
    rm -r ./network

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
# gips ./Sub_10_Virt_2_Scenario_12.xmi 5

export a=$1 # algorithm
export x=$2 # XMI file path
export scenario_name=$(echo $x | sed 's/.\///' | sed 's/.xmi//')
export r=$3 # number of runs

# GIPS workaround for all needed xmi files
if [ $a = "gips" ]; then
    echo "=> Applying GIPS hipe-network.xmi workaround."

    # Extract hipe-network.xmi file
    unzip -o $JAR "org/emoflon/gips/gipsl/examples/mdvne/hipe/*/hipe-network.xmi"
    unzip -o $JAR "org/emoflon/gips/gipsl/examples/mdvne/api/*/gips-model.xmi"
    unzip -o $JAR "org/emoflon/gips/gipsl/examples/mdvne/api/ibex-patterns.xmi"

    mkdir -p ../org.emoflon.gips.gipsl.examples.mdvne/src-gen/
    mkdir -p C%3A/Users/mkratz/git/gips-examples/org.emoflon.gips.gipsl.examples.mdvne/src-gen

    rsync -a ./org ./bin
    rsync -a ./org ../org.emoflon.gips.gipsl.examples.mdvne/src-gen
    rsync -a ./org ./C%3A/Users/mkratz/git/gips-examples/org.emoflon.gips.gipsl.examples.mdvne/src-gen
    rm -r ./org
fi

for ((i=1;i<=$r;i++));
do
    # Without memory measurement
    export RUN_NAME="${a}_${scenario_name}_run${i}"
    export ARGS="-a $a -o total-comm-c -x $x -c ./metrics/$RUN_NAME.csv"
    echo "#"
    echo "# => Using ARGS: $ARGS"
    echo "#"
    run
done

echo "#"
echo "# => Arg script done."
echo "#"

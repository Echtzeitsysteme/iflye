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
# pm two-tier-4-pods 4 1 3
# $1 $2              $3 $4 $5
# pm two-tier-4-pods auto 1 3

export a=$1 # algorithm
export s=$2 # scenario
export l=$3 # maximum path length
export k=$4 # k fastest paths to generate
export r=$5 # number of runs

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

# GIPS-migration workaround for all needed xmi files
if [ $a = "gips-mig" ]; then
    echo "=> Applying GIPS-migration hipe-network.xmi workaround."

    # Extract hipe-network.xmi file
    unzip -o $JAR "org/emoflon/gips/gipsl/examples/mdvne/migration/hipe/*/hipe-network.xmi"
    unzip -o $JAR "org/emoflon/gips/gipsl/examples/mdvne/migration/api/*/gips-model.xmi"
    unzip -o $JAR "org/emoflon/gips/gipsl/examples/mdvne/migration/api/ibex-patterns.xmi"

    mkdir -p ../org.emoflon.gips.gipsl.examples.mdvne.migration/src-gen/
    mkdir -p C%3A/Users/mkratz/git/gips-examples/org.emoflon.gips.gipsl.examples.mdvne.migration/src-gen

    rsync -a ./org ./bin
    rsync -a ./org ../org.emoflon.gips.gipsl.examples.mdvne.migration/src-gen
    rsync -a ./org ./C%3A/Users/mkratz/git/gips-examples/org.emoflon.gips.gipsl.examples.mdvne.migration/src-gen
    rm -r ./org
fi

# GIPS-sequence workaround for all needed xmi files
if [ $a = "gips-seq" ]; then
    echo "=> Applying GIPS-seq hipe-network.xmi workaround."

    # Extract hipe-network.xmi file
    unzip -o $JAR "org/emoflon/gips/gipsl/examples/mdvne/seq/hipe/*/hipe-network.xmi"
    unzip -o $JAR "org/emoflon/gips/gipsl/examples/mdvne/seq/api/*/gips-model.xmi"
    unzip -o $JAR "org/emoflon/gips/gipsl/examples/mdvne/seq/api/ibex-patterns.xmi"

    mkdir -p ../org.emoflon.gips.gipsl.examples.mdvne.seq/src-gen/
    mkdir -p C%3A/Users/mkratz/git/gips-examples/org.emoflon.gips.gipsl.examples.mdvne.seq/src-gen

    rsync -a ./org ./bin
    rsync -a ./org ../org.emoflon.gips.gipsl.examples.mdvne.seq/src-gen
    rsync -a ./org ./C%3A/Users/mkratz/git/gips-examples/org.emoflon.gips.gipsl.examples.mdvne.seq/src-gen
    rm -r ./org
fi

for ((i=1;i<=$r;i++));
do
    # Without memory measurement
    export RUN_NAME="${a}_${s}_l${l}_k${k}_run${i}"
    export ARGS="-a $a -o total-comm-c -e emoflon_wo_update -l $l -k $k -s resources/$s/snet.json -v resources/40-vnets/vnets.json -c ./metrics/$RUN_NAME.csv -i 600"
    echo "#"
    echo "# => Using ARGS: $ARGS"
    echo "#"
    run
done

echo "#"
echo "# => Arg script done."
echo "#"


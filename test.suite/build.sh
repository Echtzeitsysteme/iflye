#!/bin/bash

#
# Set constants.
#
junit_platform_version='1.8.2'
ant_version='1.10.11'
ant_folder="apache-ant-${ant_version}"
ant_archive="${ant_folder}-bin.tar.gz"

#
# Load and extract Apache Ant.
#
curl --remote-name "https://archive.apache.org/dist/ant/binaries/${ant_archive}"
tar --extract -z --exclude "${ant_folder}/manual" --file "${ant_archive}"

#
# Load and store junit-platform-console-standalone jar into ${ANT_HOME}/lib.
#
(cd "${ant_folder}/lib" && curl --remote-name  "https://repo1.maven.org/maven2/org/junit/platform/junit-platform-console-standalone/${junit_platform_version}/junit-platform-console-standalone-${junit_platform_version}.jar")

#
# Prepare iflye project dependencies
#
mkdir -p "${ant_folder}/lib/emf"
mkdir -p "${ant_folder}/lib/google"
mkdir -p "${ant_folder}/lib/emoflon"
mkdir -p "${ant_folder}/lib/hipe"
mkdir -p "${ant_folder}/lib/hipe-dependencies"
mkdir -p "${ant_folder}/lib/eclipse-collections"
mkdir -p "${ant_folder}/lib/log4j"

#
# Setup ENVs used by iflye
#
echo "=> Export envs."
export GRB_LICENSE_FILE=/home/mkratz/gurobi.lic
export GUROBI_HOME=/opt/gurobi950/linux64/
export LD_LIBRARY_PATH=/opt/gurobi950/linux64/lib/
export PATH=/opt/gurobi950/linux64/bin/:/opt/ibm/ILOG/CPLEX_Studio201/cplex/bin/x86-64_linux/:$PATH

#
# Finally, let Ant do its work...
#
ANT_HOME=${ant_folder} "./${ant_folder}/bin/ant"

#!/bin/bash

#
# This script needs (at least): curl, tar, git, cp, rm, a JAVA-JDK
#

set -e

#
# Set constants.
#
junit_platform_version='1.8.2'
ant_version='1.10.12'
ant_folder="apache-ant-${ant_version}"
ant_folder_abs="${PWD}/${ant_folder}"
ant_archive="${ant_folder}-bin.tar.gz"

#
# Load and extract Apache Ant.
#
curl --remote-name "https://archive.apache.org/dist/ant/binaries/${ant_archive}"
tar --extract -z --exclude "${ant_folder}/manual" --file "${ant_archive}"

#
# Load and store junit-platform-console-standalone jar into ${ANT_HOME}/lib.
#
(cd "${ant_folder}/lib" && curl --remote-name "https://repo1.maven.org/maven2/org/junit/platform/junit-platform-console-standalone/${junit_platform_version}/junit-platform-console-standalone-${junit_platform_version}.jar")

#
# Prepare iflye project and platform dependencies
#
mkdir -p "${ant_folder}/lib/emf"

mkdir -p "${ant_folder}/lib/google"
(cd "${ant_folder}/lib/google" && curl --remote-name "https://repo1.maven.org/maven2/com/google/guava/guava/31.0.1-jre/guava-31.0.1-jre.jar")

mkdir -p "${ant_folder}/lib/emoflon"
(mkdir -p /tmp/emoflon && cd /tmp/emoflon && git clone https://github.com/eMoflon/emoflon-ibex-updatesite.git && cp -r /tmp/emoflon/emoflon-ibex-updatesite/snapshot/updatesite/plugins/*.jar "${ant_folder_abs}/lib/emoflon" && rm -rf /tmp/emoflon)

mkdir -p "${ant_folder}/lib/hipe"
(mkdir -p /tmp/hipe && cd /tmp/hipe && git clone https://github.com/HiPE-DevOps/HiPE-Updatesite.git && cp -r /tmp/hipe/HiPE-Updatesite/hipe.updatesite/plugins/*.jar "${ant_folder_abs}/lib/hipe" && rm -rf /tmp/hipe)
(cd "${ant_folder}/lib/hipe" && unzip hipe.dependencies_*.jar)

mkdir -p "${ant_folder}/lib/eclipse-collections"
(cd "${ant_folder}/lib/eclipse-collections" && curl --remote-name "https://repo1.maven.org/maven2/org/eclipse/collections/eclipse-collections/11.0.0/eclipse-collections-11.0.0.jar")
(cd "${ant_folder}/lib/eclipse-collections" && curl --remote-name "https://repo1.maven.org/maven2/org/eclipse/collections/eclipse-collections-api/11.0.0/eclipse-collections-api-11.0.0.jar")

mkdir -p "${ant_folder}/lib/log4j"
(cd "${ant_folder}/lib/log4j" && curl --remote-name "https://download.eclipse.org/tools/orbit/downloads/drops/R20201118194144/repository/plugins/org.apache.log4j_1.2.15.v201012070815.jar")
# ^TODO: Find better URL for this JAR

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

#!/bin/bash

# This script copies the generated runtime components from the 
# (external) GIPS MdVNE example folder into this repository.

set -e

MDVNE_PROJECT_NAME="org.emoflon.gips.gipsl.examples.mdvne"

rm -rf ../$MDVNE_PROJECT_NAME/src-gen
rsync -a --progress --stats ../../gips-examples/$MDVNE_PROJECT_NAME/src-gen ../$MDVNE_PROJECT_NAME

rm -rf ../$MDVNE_PROJECT_NAME.migration/src-gen
rsync -a --progress --stats ../../gips-examples/$MDVNE_PROJECT_NAME.migration/src-gen ../$MDVNE_PROJECT_NAME.migration

rm -rf ../$MDVNE_PROJECT_NAME.seq/src-gen
rsync -a --progress --stats ../../gips-examples/$MDVNE_PROJECT_NAME.seq/src-gen ../$MDVNE_PROJECT_NAME.seq

#!/bin/bash

# This script copies the generated runtime components from the 
# (external) GIPS MdVNE example folder into this repository.

set -e

MDVNE_PROJECT_NAME="org.emoflon.gips.gipsl.examples.mdvne"

rm -rf ../$MDVNE_PROJECT_NAME/src-gen
rsync -a --progress --stats ../../gips-examples/$MDVNE_PROJECT_NAME/src-gen ../$MDVNE_PROJECT_NAME

#!/bin/bash

set -e

# Find all files with suffix .csv
BASE_PATH="../metrics"
cp $BASE_PATH/*.csv .

FILES=$(find . -name '*.csv')
for f in $FILES
do
    # Set filename to latex env, this is needed by the tex template
    export CSV_PATH=$f

    # Compile the file
    lualatex -synctex=1 -interaction=nonstopmode -jobname="${f%.csv}-out" metric-renderer.tex
done

# Cleanup
rm $(find . -name '*.aux' -o -name '*.log' -o -name '*.synctex.gz' -o -name '*.csv')
unset CSV_FILE BASE_PATH FILES

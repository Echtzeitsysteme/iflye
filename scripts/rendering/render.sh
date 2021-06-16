#!/bin/bash

set -e

# Find all files with suffix .csv
if [ $1 = "iflye" ]; then
    BASE_PATH="../metrics"
    cp $BASE_PATH/*.csv .

    FILES=$(find . -name '*.csv')
elif [ $1 = "idyve" ]; then
    BASE_PATH="../idyve-output"
    PATHS=$(find $BASE_PATH -name 'metrics.csv')
    for p in $PATHS
    do
        current_p=$(echo $p | awk -F / '{ print $(NF-1) }')
        mkdir -p $current_p
        cp $p ./$current_p/metrics.csv
    done

    FILES=$(find . -name '*.csv')
else
    echo "First parameter was invalid."
    exit 1
fi

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

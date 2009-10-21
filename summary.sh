#!/bin/bash
# e.g. ./summary.sh data/*.sent
./go.sh "$@" | awk '/^\*\*\* +Input/{print "";print;x=0}  /Entity Report/ {x=1}  x{print}'

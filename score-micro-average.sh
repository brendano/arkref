#!/bin/bash


PRECISION=`grep -P ' \d+/\d+\s+bad' $1 | sed 's/\// /' | awk '{num+=$1; denom+=$2} END{print 1-num/denom}'`
RECALL=`grep -P ' \d+/\d+\s+missing' $1 | sed 's/\// /' | awk '{num+=$1; denom+=$2} END{print 1-num/denom}'`

echo "" | awk "END{print \"PRECISION:\t$PRECISION\nRECALL:\t$RECALL\nF1:\t\"($PRECISION*$RECALL*2)/($PRECISION+$RECALL)\"\n\"}"





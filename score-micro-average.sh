#!/bin/bash


PRECISION=`grep -P ' \d+/\d+\s+bad' log | sed 's/\// /' | awk '{num+=$1; denom+=$2} END{print 1-num/denom}'`
RECALL=`grep -P ' \d+/\d+\s+missing' log | sed 's/\// /' | awk '{num+=$1; denom+=$2} END{print 1-num/denom}'`



perl -e "print \"PRECISION\t$PRECISION\nRECALL:\t$RECALL\nF1:\t\".($PRECISION*$RECALL*2)/($PRECISION+$RECALL).\"\n\";"




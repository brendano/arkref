#!/bin/bash


P=`grep -E '[0-9]+/[0-9]+ +bad' $1 | sed 's/\// /' | awk '{num+=$1; denom+=$2} END{print 1-num/denom}'`
R=`grep -E '[0-9]+/[0-9]+ +missing' $1 | sed 's/\// /' | awk '{num+=$1; denom+=$2} END{print 1-num/denom}'`

echo "" | awk "END{print \"PRECISION:\t$P\nRECALL:\t$R\nF1:\t\"($P*$R*2)/($P+$R)\"\n\"}"





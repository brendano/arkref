#!/bin/bash
cat | grep 'B3 prec' | perl -pe 's/=/ /g' | awk '
{p += $3; r += $5} 
END{
  p=p/NR
  r=r/NR
  print "prec",p," rec",r, " f1", 2*p*r/(p+r)
}'


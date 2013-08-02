#!/bin/bash
set -eux

mkdir -p cultest rothdev

cat ace04_roth_split/HLT2007.TEST | python copy.py cultest

setdiff ace04_roth_split/HLT2007.TRAIN ace04_roth_split/HLT2007.TRAIN_MINUS_DEV | python copy.py rothdev

# output should be

# ~/projects/coref/arkref_newsvn/data_ace % ./make.sh > make.log
# + cat ace04_roth_split/HLT2007.TEST
# + python copy.py cultest
# + setdiff ace04_roth_split/HLT2007.TRAIN ace04_roth_split/HLT2007.TRAIN_MINUS_DEV
# + python copy.py rothdev
# 
# and make.log has all the copy commands

## And I get these counts:
# ~/projects/coref/arkref_newsvn/data_ace % print -l cultest/*.SGM | wc -l
#      107
# ~/projects/coref/arkref_newsvn/data_ace % print -l rothdev/*.SGM | wc -l
#       68


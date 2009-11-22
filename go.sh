#!/usr/bin/env zsh
here=$(dirname $0)
java -mx1g -cp $(print $here/**/*.jar | tr ' ' :):bin analysis._Pipeline "$@"

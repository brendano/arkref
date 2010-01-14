#!/usr/bin/env zsh
here=$(dirname $0)
java -mx1g -ea -cp $(print $here/**/*.jar | tr ' ' :):bin arkref.analysis.ARKref "$@"

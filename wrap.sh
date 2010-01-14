#!/usr/bin/env zsh
h=$(dirname $0)
[[ "$1" != arkref.* ]] && 1=arkref.$1
java -mx1g -ea -cp $(print $h/lib/**/*.jar|tr ' ' :):$h/bin "$@"

#!/usr/bin/env zsh
h=$(dirname $0)
java -mx1g -ea -cp $(print $h/lib/**/*.jar|tr ' ' :):$h/bin "$@"

#!/usr/bin/env zsh
h=$(dirname $0)
java -ea -cp $(print $h/lib/**/*.jar|tr ' ' :):$h/bin "$@"

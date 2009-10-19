#!/bin/bash
here=$(dirname $0)
java -cp $here/bin:$here/stanford-ner-2008-05-07.jar:$here/stanford-parser-2008-10-26.jar:$here/commons-lang-2.4.jar analysis._Pipeline "$@"

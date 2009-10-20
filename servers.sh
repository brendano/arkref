set -x
java -server -mx1g -cp bin:stanford-parser-2008-10-26.jar parsestuff.StanfordParserServer lib/englishPCFG.ser.gz &
java -server -mx400m -cp stanford-ner-2008-05-07.jar edu.stanford.nlp.ie.NERServer -loadClassifier lib/ner-eng-ie.crf-muc7.ser.gz  1234 &

wait

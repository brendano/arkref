set -x
java -server -mx1g -cp bin:lib/stanford-parser-2008-10-26.jar arkref.parsestuff.StanfordParserServer lib/englishFactored.ser.gz &
java -server -mx400m -cp lib/stanford-ner-2008-05-07.jar edu.stanford.nlp.ie.NERServer -loadClassifier lib/ner-eng-ie.crf-muc7.ser.gz  5555 &

wait

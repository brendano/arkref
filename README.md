ARKref
======
* Website: http://www.ark.cs.cmu.edu/ARKref/
* Mike Heilman (http://www.cs.cmu.edu/~mheilman/)
* Brendan O'Connor (http://anyall.org/)

ARKref is a basic implementation of a syntactically rich, rule-based
coreference system very similar to (the syntactic components of) Haghighi and
Klein (2009). We find it is useful as a starting point to be adapted into
larger information extraction and natural language processing systems. For
example, by tweaking the gazetteers, customizing mention extraction, turning
the syntactic rules into log-linear features, etc., it can be made useful for
a variety of applications.

How to run
----------

To get started, the following command runs ARKref on a demo document included
with the code. We start with just one file, the document text:

    $ ls -1 demo/
    lee_example.txt
    
    $ cat demo/lee_example.txt
    This film should be brilliant. It sounds like a great plot, the actors are
    first grade, and the supporting cast is good as well, and Stallone is
    attempting to deliver a good performance. However, it can't hold up.

Run ARKref like so, creating intermediate files and output:

    $ ./arkref.sh -input demo/lee_example.txt
    ...

    $ ls -1 demo/
    lee_example.ner
    lee_example.osent
    lee_example.parse
    lee_example.tagged
    lee_example.txt

The file `.tagged` file is the final output, in a mention/entity-tagged pseudo-xml format.

    $ cat demo/lee_example.tagged 
    <mention mentionid="1" entityid="1_2_9">This film</mention> should be brilliant .
    <mention mentionid="2" entityid="1_2_9">It</mention> sounds like <mention mentionid="3" entityid="3">a great plot</mention> , <mention mentionid="4" entityid="4_5">the actors</mention> are <mention mentionid="5" entityid="4_5">first grade</mention> , and <mention mentionid="6" entityid="6">the supporting cast</mention> is good as well , and <mention mentionid="7" entityid="7">Stallone</mention> is attempting to deliver <mention mentionid="8" entityid="8">a good performance</mention> .
    However , <mention mentionid="9" entityid="1_2_9">it</mention> ca n't hold up .

During development, since it takes a while to load the parser and NER, it can
be convenient to run them as background servers. If they're running, ARKref will
automatically use them. Start them in a new terminal window with:

    $ ./servers.sh

Please see `./arkref.sh -help` for more options.


Seeing what's going on
----------------------

The debug output is designed to make it as easy as possible to understand why
the algorithm is making its decisions. This is possible since the approach is
strongly procedural and rule-oriented. See it with:

    $ ./arkref.sh -debug demo/lee_example.txt

Various development utility scripts are included. (We do not attempt to
maintain them to be runnable out-of-the-box, so you may have to install
prerequisites: see their comments.)  For example, streamlined tagging view:

    $ cat demo/lee_example.tagged | ./tagviz.rb 

    *This film*_1 should be brilliant .

    *It*_1 sounds like *a great plot* , *the actors*_4 are *first grade*_4 ,
    and *the supporting cast* is good as well , and *Stallone* is attempting
    to deliver *a good performance* .

    However , *it*_1 ca n't hold up .

This makes obvious the precision error on the "4" cluster through a
predicate-nominative construction. It's often useful to check for parsing
errors by looking at the (raw, pre-surgery) trees as PDF or PNG images:

    $ cat demo/lee_example.parse | ./treeviz.py

<center><iframe src="http://docs.google.com/viewer?url=http%3A%2F%2Fwww.ark.cs.cmu.edu%2FARKref%2Flee_example.parse.pdf&embedded=true" width="500" height="350" style="border: none;"></iframe></center>

Evaluation: there is code that loads ACE Phase 2 datasets and evaluates on
them. Unfortunately, this data cannot be freely redistributed. (Please contact
us if you need assistance.) Evaluation can be run something like this:

    $ ./arkref.sh -ace -input ace_rothdev/*.txt | tee log | ./score-micro-average.sh
    ....................................................................
    PRECISION:  0.639167
    RECALL:     0.509876
    F1:         0.567247



More information
----------------

We are working on a real tech report describing this system, but in the
meantime, a class project report is available with the code:
`notes/class_paper/coref_final_for_rtw.pdf`. But please first read:

* Aria Haghighi and Dan Klein. _Simple Coreference Resolution with Rich
  Syntactic and Semantic Features_. EMNLP 2009. At
  http://www.aclweb.org/anthology/D/D09/D09-1120.pdf.

Out of the box, ARKref is roughly equivalent to H&K's `+SYN-CONSTR` system. On
the dev data set, its F-score is about the same, though the precision/recall
tradeoff is different. Note that there is no lexical semantic compatibility
subsystem (what H&K call `+SEM-COMPAT`).

This approach depends on having a named entity recognizer and a syntactic
constituency parser. ARKref is written to use Stanford NER and the Stanford
Parser, which are included in this download. ARKref also makes heavy use of
the Stanford Tregex library for implementation of syntactic rules. Please see
the file LICENSE.txt for information on implications for redistribution.

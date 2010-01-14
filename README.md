ARKref
======
* Website: http://www.ark.cs.cmu.edu/arkref/
* Brendan O'Connor (http://anyall.org/)
* Mike Heilman (http://www.cs.cmu.edu/~mheilman/)

ARKref is a basic implementation of a syntactically rich, rule-based 
coreference system very similar to (the syntactic components of) Haghighi
and Klein (2009).  We find it is useful as a starting point to be adapted
into larger information extraction and natural language processing systems.
For example, by tweaking the gazetteers, special handling for quotations, 
turning the syntactic rules into log-linear features, etc., it can be made
useful for a variety of applications.

To get started, the following command runs it on a demo document included 
with the code.

    $ cat demo/document.txt
    BLA BLA BLA

    $ ./arkref.sh -writeTagged demo/document.txt
    Writng resolutions to demo/document.tagged
    
    $ cat demo/document.tagged
    BLA BLA BLA

Please see `./arkref.sh -help` for more options.  Since the core algorithm
is relatively simple and procedural, debug output from 
`./arkref.sh -debug` should make it obvious how ARKref is making its
decisions.


More information
----------------

We are working on a real tech report describing this system, but in the
meantime, a class project report is available with the code: 
`notes/class_paper/coref_final_for_rtw.pdf`.  Please first read:

* Aria Haghighi and Dan Klein.  _Simple Coreference Resolution with Rich
Syntactic and Semantic Features_.  EMNLP 2009.  
At http://www.aclweb.org/anthology/D/D09/D09-1120.pdf.

Out of the box, ARKref is roughly equivalent to H&K's `+SYN-CONSTR` 
system.  On the dev data set, its F-score is about the same, though 
the precision/recall tradeoff is different.  Note that there is no 
lexical semantic compatibility subsystem (what H&K call `+SEM-COMPAT`).

This approach depends on having a named entity recognizer and a 
syntactic constituency parser.  ARKref is written to use Stanford NER
and the Stanford Parser, which are included in this download (but 
could be swapped out with similar pieces of software).  Please see
the file LICENSE.txt for information on implications for
redistribution.


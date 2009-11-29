package ace;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.aliasi.util.Pair;

import parsestuff.U;
import analysis.Preprocess;
import analysis.SyntacticPaths;
import data.Document;
import data.NodeHashMap;
import data.Sentence;
import data.Word;
import edu.stanford.nlp.stats.IntCounter;
import edu.stanford.nlp.trees.Tree;

/** 
 * like analysis.FindMentions except use exclusively ACE's opinions of what the mentions are
 * the tricky bits are figuring out how to reconcile ACE's mentions to our parsetree-defined mentions
 * @author brendano
 */
public class FindAceMentions {
	public static class AlignmentFailed extends Exception { }


	public static void main(String[] args) throws Exception {
		for (String path : args) {
			path = Preprocess.shortPath(path);
			U.pf("DOC\t%s\n", path);
			Document myDoc    = Document.loadFiles(path);
			AceDocument aceDoc= AceDocument.load(path);
			myDoc.ensureSurfaceSentenceLoad(path);
			go(myDoc, aceDoc);	
		}
		
		
	}
	public static void go(Document myDoc, AceDocument aceDoc) throws AlignmentFailed {
		// (1) align our tokens to raw text char offsets
		// (2) calibrate ACE offsets to real text offsets
		// (3) map ACE mentions to Stanford tokens
		// (4) and map those to appropriate parse nodes
		// Issues
		//  * What about sentences that didn't parse?
		
		
		// Step (1)
		myDoc.doTokenAlignments(aceDoc.text);
		
		U.pl("***  ACE alignments ***\n");
		// Step (2)
		int aceOffsetCorrection = calculateAceOffsetCorrection(myDoc, aceDoc);
		
		// Step (3)
		List<AceDocument.Mention> aceMentions = aceDoc.document.getMentions();
		AceDocument.mentionsHeadSort(aceMentions);
		Map<AceDocument.Mention, Word> aceMention2word = 
			alignToTokens(myDoc, aceOffsetCorrection, aceMentions);
		
//		displayAceMentions(aceMentions, ace2word);


		// Step (4)
		// these are the relationships we are building between (1) parse nodes, (2) our mentions, (3) ACE mentions
		//   node -> myM    1-1, no more than one mention per node
		//   myM  -> AceM   1-many
		
		// Step (4.1)
		// OK, first let's build a map of parse nodes to all possible ACE mentions at that node.
		// Later we'll turn it into a map to a single mention per node,
		// which is the structure the mainline pipeline expects.
		NodeHashMap<ArrayList<AceDocument.Mention>> node2aceMentions = new NodeHashMap();
		Map<AceDocument.Mention, Pair <Sentence,Tree>> aceMention2node = new HashMap();
		
		Sentence curS = null;
		for (AceDocument.Mention m : aceMentions) {
			Word w = aceMention2word.get(m);
			assert w != null : "wtf every mention needs to map to something";
			if (w.sentence != curS) {
				curS = w.sentence;
//				U.pf("S%-2s  %s\n", curS.ID(), curS.text());
			}
//			U.pf("\nACE %-4s | %s\n", m.entity.mentions.size()==1 ? "" : m.entity.ID(), m);
			if (w.node() == null) {
//				U.pl("No parse node");
			} else {
//				U.pl("Parse node:  " + w.node());
				Tree maxHead = SyntacticPaths.getMaximalProjection(w.node(), w.sentence.rootNode());
				if (maxHead.label().value().matches("^(NNP|NN)$")) {
					// ugh, an issue that ACE heads can be multiwords and end up matching
					// too far to the left so don't hit Collins/Stanford heads.
					// Better way to solve is test for multiwords at alignment time and find the smallest subtree over the span
					// (there is already a helper function for this)
					// In the meantime, we use a heuristic detection (we kinda want NPs, not NNP or NNs),
					// and then just go for the next enclosing head structure.
					// But we can't hard require NPs because then JJ mentions resolve to enclosing NP which is wrong.
					
					// Problematic example.  [[]] is our one-word alignment, (()) is ACE head, full phrase is ACE extent.
					// (([[Joseph]] Conrad Parkhurst)), who founded the motorcycle magazine Cycle World in 1962
//					U.pl("Not far enough: " + maxHead);
					maxHead = maxHead.parent(w.sentence.rootNode());
					maxHead = SyntacticPaths.getMaximalProjection(maxHead, w.sentence.rootNode());
				}
//				U.pl("Preterminal: " + w.node().parent(w.sentence.rootNode()));
//				U.pl("MaxHead:     " + maxHead);
				
				if ( ! node2aceMentions.containsKey(w.sentence, maxHead)) {
					node2aceMentions.put(w.sentence, maxHead, new ArrayList());
				}
				node2aceMentions.get(w.sentence, maxHead).add(m);
				aceMention2node.put(m, new Pair(w.sentence,maxHead));
			}
		}
		
		// Step (4.2)
		// build up our native notion of Mention's -- much like FindMentions.go()
		// Ensure there is a myMention for every ACE mention that aligned to a parse node.
		int myMentionId = 0;
//		Map<AceDocument.Mention,  data.Mention> aceMention2myMention = new HashMap();
		for (AceDocument.Mention aceM : aceMention2node.keySet()) {
			Pair<Sentence,Tree> sn = aceMention2node.get(aceM);
			Sentence s = sn.a();
			Tree node = sn.b();
			
			data.Mention myMention = new data.Mention(++myMentionId, s, node);
			myDoc.mentions().add(myMention);
			myDoc.node2mention.put(s, node, myMention);

			aceM.myMention = myMention;
		}
		
		
		
		
		// Show what we just found.
		U.pl("\n***  ACE-driven mentions  ***");
		for (data.Mention myM : myDoc.mentions()) {
			U.pl("myMention  " + myM);
			for(AceDocument.Mention aceM : node2aceMentions.get(myM.getSentence(), myM.node())) {
				U.pl("  " + aceM);
			}
		}
		

		
	}
	
	
	private static void displayAceMentions(
			List<AceDocument.Mention> aceMentions,
			Map<AceDocument.Mention, Word> ace2word) {
		Sentence curS = null;
		for (AceDocument.Mention m : aceMentions) {
			Word w = ace2word.get(m);
			assert w != null : "wtf every mention needs to map to something";
			if (w.sentence != curS) {
				curS = w.sentence;
				U.pf("S%-2s  %s\n", curS.ID(), curS.text());
			}
			U.pf("  %-4s | %s\n", m.entity.mentions.size()==1 ? "" : m.entity.ID(), m);
		}
	}
	
	private static Map<AceDocument.Mention, Word> alignToTokens(Document myDoc, int aceOffsetCorrection,
			List<AceDocument.Mention> aceMentions) throws AlignmentFailed {
		HashMap<AceDocument.Mention, Word> ace2word = new HashMap();

		List<Word> allWords = myDoc.allWords();
		int word_i = 0;
		int m_i = 0;
		
		int BACKWARDS_FUDGE = 20; // e.g ACE "msnbc" and our "msnbc/reuters" .. and more?
		
		mention_loop:
		while(m_i < aceMentions.size()) {
			AceDocument.Mention m = aceMentions.get(m_i);
			int aceHeadStart = m.head.charseq.start - aceOffsetCorrection;
//			U.pf("Ace Mention to Align:  pos=%-3d  :  %s\n", m.head.charseq.start-aceOffsetCorrection, m);
			Word word;
			word = allWords.get(word_i);
			// want to use right edge of token, not left edge, in case ACE head matches an internal subword inside our token
			// e.g. ACE thinks [Russian] and [American] separate, but Stanford thinks [Russian-American] in 20001115_AFP_ARB_0060_ENG
			// [Russian] aligns to [Russian-American], but it advances past when trying to find [American]'s alignment.
			while( word.charStart+word.token.length() + BACKWARDS_FUDGE < aceHeadStart ) {
//				U.pf("  not high enough pos=%-3d  :  %s\n", word.charStart, word);
				word_i++;
				if (word_i >= allWords.size()) break mention_loop;
				word = allWords.get(word_i);
			}
			// guard against more weirdness, though this now seems weird. is necessary with BACKWARDS_FUDGE though
			while ( ! crudeMatch_AceHead_vs_Token(m, word)) {
//				U.pf("  no string match pos=%-3d  :  %s\n", word.charStart, word);
				word_i++;
				if (word_i >= allWords.size()) break mention_loop;
				word = allWords.get(word_i);
			}
//			U.pf("ALIGN\t%-4d %-4d   ****   [%-20s]  ******  [%-60s]\n", word.charStart, m.head.charseq.start-aceOffsetCorrection, word,m);
			ace2word.put(m, word);
			m_i++;
		}
		if (m_i < aceMentions.size())
			throw new AlignmentFailed();
		
		return ace2word;
	}
	
	/**
	 * ACE offsets are usually too high, by like 50-100 or so. 
	 * Estimate this offset correction by trying to find several crappy 
	 * string equality alignments then plurality vote **/
	public static int calculateAceOffsetCorrection(Document myDoc, AceDocument aceDoc) {
		IntCounter<Integer> offsetDiffs = new IntCounter();
		List<AceDocument.Mention> aceMentions = aceDoc.document.getMentions();
		AceDocument.mentionsHeadSort(aceMentions);

		for (int i=0; i<aceMentions.size() && (i < 15 || offsetDiffs.max() < 2); i++) {
			AceDocument.Mention m = aceMentions.get(i);
			// find our first token that matches ace head
			sent_loop:
			for (Sentence s : myDoc.sentences()) {
				for (Word w : s.words) {
					if (crudeMatch_AceHead_vs_Token(m,w)) {
						offsetDiffs.incrementCount( m.head.charseq.start - w.charStart );
						break sent_loop;
					}
				}
			}
		}
		U.pl("ace offset diff histogram: " + offsetDiffs);
		U.pl("Using offset: " + offsetDiffs.argmax());
		return offsetDiffs.argmax();
	}
	
	public static boolean crudeMatch_AceHead_vs_Token(AceDocument.Mention m, Word w) {
		// rules can differ for, at the very least:
		// * whether punctuation is included:  [Mr] vs [Mr.]
		// * multiwords:  [Jeb Bush] vs [Jeb]
		
		String aceHead = m.head.charseq.text;
		String tok = w.token;
		
		if (aceHead.length()==1 && tok.length()==1) {
			return aceHead.equals(tok);
		} else if (aceHead.length()==1 || tok.length()==1) {
			// tiny tokens as substring matches is very false positive-y
			return false;
		} else {
			return tok.contains(aceHead) || aceHead.contains(tok);	
		}
	}
	
}

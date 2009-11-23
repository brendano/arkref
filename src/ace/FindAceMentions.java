package ace;

import java.util.List;

import parsestuff.U;
import analysis.Preprocess;
import data.Document;
import data.Sentence;
import data.Word;
import edu.stanford.nlp.stats.IntCounter;

/** 
 * like analysis.FindMentions except use exclusively ACE's opinions of what the mentions are
 * the tricky bits are figuring out how to reconcile ACE's mentions to our parsetree-defined mentions
 * @author brendano
 */
public class FindAceMentions {
	@SuppressWarnings("serial")
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
		
		
		myDoc.doTokenAlignments(aceDoc.text);
		int aceOffsetCorrection = calculateAceOffsetCorrection(myDoc, aceDoc);
		
		List<AceDocument.Mention> aceMentions = aceDoc.document.getMentions();
		AceDocument.mentionsHeadSort(aceMentions);
		U.pl("All Mentions:  " + aceMentions);
//		HashMap<AceDocument.Mention, Word> ace2word = new HashMap();
		List<Word> allWords = myDoc.getAllWords();
		int word_i = 0;
		int m_i = 0;
		
		mention_loop:
		while(m_i < aceMentions.size()) {
			AceDocument.Mention m = aceMentions.get(m_i);
			int aceHeadStart = m.head.charseq.start - aceOffsetCorrection;
			U.pf("Ace Mention to Align:  pos=%-3d  :  %s\n", m.head.charseq.start-aceOffsetCorrection, m);
			Word word;
			word = allWords.get(word_i);
			// want to use right edge of token, not left edge, in case ACE head matches an internal subword inside our token
			// e.g. ACE thinks [Russian] and [American] separate, but Stanford thinks [Russian-American] in 20001115_AFP_ARB_0060_ENG
			// [Russian] aligns to [Russian-American], but it advances past when trying to find [American]'s alignment.
			while( word.charStart+word.token.length() < aceHeadStart ) {
//				U.pf("  not high enough pos=%-3d  :  %s\n", word.charStart, word);
				word_i++;
				if (word_i >= allWords.size()) break mention_loop;
				word = allWords.get(word_i);
			}
			// sometimes the offset increases - i.e., ACE offsets fall behind the real offsets relative to how ahead they were earlier in the document.
			// so do a second matching round via string matching
			// this will not solve if the offset decreases (ACE offsets get more ahead)
			while ( ! crudeMatch_AceHead_vs_Token(m, word)) {
				U.pf("  no string match pos=%-3d  :  %s\n", word.charStart, word);
				word_i++;
				if (word_i >= allWords.size()) break mention_loop;
				word = allWords.get(word_i);
			}
			U.pf("Alignment!  Word:ACE  %-4d:%-4d  :   [%-15s]  <->  [%-60s]\n", 
					word.charStart, m.head.charseq.start-aceOffsetCorrection, word,m);
			m_i++;
		}
		if (m_i < aceMentions.size()) {
			throw new AlignmentFailed();
		}
	}
	
	/**
	 * ACE offsets are usually too high, by like 50-100 or so. 
	 * Estimate this offset correction by trying to find several crappy 
	 * string equality alignments then plurality vote **/
	public static int calculateAceOffsetCorrection(Document myDoc, AceDocument aceDoc) {
		IntCounter<Integer> offsetDiffs = new IntCounter();
		List<AceDocument.Mention> aceMentions = aceDoc.document.getMentions();
		AceDocument.mentionsHeadSort(aceMentions);

		for (int i=0; i<aceMentions.size() && (i < 20 || offsetDiffs.max() < 5); i++) {
			AceDocument.Mention m = aceMentions.get(i);
			// find our first token that matches ace head
			sent_loop:
			for (Sentence s : myDoc.getSentences()) {
				for (Word w : s.words) {
					if (crudeMatch_AceHead_vs_Token(m,w)) {
						offsetDiffs.incrementCount( m.head.charseq.start - w.charStart );
						break sent_loop;
					}
				}
			}
		}
		U.pl("ace offset diff histogram: " + offsetDiffs);
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
			return false;
		} else {
			return tok.contains(aceHead) || aceHead.contains(tok);	
		}
	}
	
}

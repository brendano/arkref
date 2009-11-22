package ace;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import data.Document;
import data.Sentence;
import edu.stanford.nlp.ling.Word;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.util.StringUtils;

import parsestuff.AnalysisUtilities;
import parsestuff.U;

public class AcePreprocess {
	public static void go(String apfFileName) throws IOException {
		String path = apfFileName.replace("_APF.XML", "").replace("_A.XML", "");
		String sgmlFilename = path + ".SGM";
		assert new File(sgmlFilename).exists();
		if (!analysis.Preprocess.alreadyPreprocessed(path)){
			String sgml = U.readFile(sgmlFilename);
			Pattern p = Pattern.compile("<TEXT>(.*)</TEXT>", Pattern.DOTALL);
			Matcher m = p.matcher(sgml);
			m.find();
			String text = m.group(1);
			U.writeFile(text, path + ".txt");
			analysis.Preprocess.go(path + ".txt");
		}
		
		alignMentions(Document.loadFiles(path), AceDocument.parseFile(apfFileName));
	}
	
	public static void alignMentions(Document doc, AceDocument.Document ad) {
		int sent_i = 0;
		int leaf_i = 0;
		int num_sent = doc.getSentences().size();
		
		AceDocument.Mention lastAceM = null;
		
		U.pf("S%-3s\t%s\n", sent_i+1, doc.getSentences().get(sent_i).rootNode.getLeaves());

		OUTER:
		for (AceDocument.Mention m : ad.getMentions()) {
			List<Word> extentToks = AnalysisUtilities.getInstance().dp.getWordsFromString(m.extent.charseq.text);
			List<Word> headToks = AnalysisUtilities.getInstance().dp.getWordsFromString(m.head.charseq.text);

			U.pl("SEARCHING FOR MATCH   | "+m.aceID+  " | " + headToks.toString() + " | " + extentToks.toString());
			boolean foundMatch = false;
			
			// look for match in our Document
			while(!foundMatch && sent_i < num_sent) {
				if (matches(extentToks, doc, sent_i, leaf_i)) {
					U.pl("MATCH");
					foundMatch = true;
					lastAceM = m;
					// want to keep current leaf the SAME -- to deal with embedded matches (!)
					// if the same string was repeated over as different mentions this would fail.
					
				} else {
					leaf_i++;
					if (leaf_i >= doc.getSentences().get(sent_i).rootNode.getLeaves().size()) {
						leaf_i = 0;
						sent_i++;
						U.pl("ADVANCE SENTENCE");
						U.pf("S%-3s\t%s\n", sent_i+1, doc.getSentences().get(sent_i).rootNode.getLeaves());
//						U.pl(doc.getSentences().get(sent_i).rootNode.getLeaves());
					}					
				}
				if (foundMatch) {
					continue OUTER;
				}
//				if (m.extent.charseq.start - lastAceM.extent.charseq.start > (sent_i-lastM_sent_i)* )
				if (sent_i >= num_sent) {
					U.pl("FELL OFF END OF DOCUMENT");
					break OUTER;
				}
			}
		}
	}
	
	public static boolean matches(List<Word> aceToks, Document doc, int sent_i, int leaf_i) {
		Sentence sent = doc.getSentences().get(sent_i);
		List<Tree> leaves = sent.rootNode.getLeaves();
		if (leaves.size() - leaf_i < aceToks.size()) {
			U.pl("not enough room");
			return false;
		}

		// all ace phrase tokens and sentence subspan tokens must match
		
		ACE_TOK_LOOP:
		for (int j=0; j < aceToks.size(); j++) {
			String aceStr = aceToks.get(j).value();
			String leafStr= leaves.get(leaf_i + j).value();
			
			if ( ! aceStr.equals(leafStr) ) {
				U.pl("nomatch  acetok:  " + aceToks.get(j) + "     leaf: " + leaves.get(leaf_i + j));
				// issue: maybe ACE is tokenized more aggressively than Stanford.
				if (leafStr.length() > aceStr.length()) {
					String ace2 = aceStr.replaceAll("\\W", "");
					String leaf2 = leafStr.replaceAll("\\W", "");
					if (ace2.equals(leaf2)) {
						continue ACE_TOK_LOOP;
					}
						
					String[] retok = leafStr.split("[^a-zA-Z]");
					if (retok.length > 1) {
						U.pl("RETOK: " + StringUtils.join(retok,", "));
						for (String tok : retok) {
							if (tok.equals(ace2)) {
								continue ACE_TOK_LOOP;
							}
						}
					}
				}
				return false;
			}
		}
		// we got through ok
		return true;
	}

	public static void main(String args[]) throws IOException {
		go(args[0]);
		
	}
}

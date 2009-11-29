package analysis;
import java.io.IOException;

import parsestuff.TregexPatternFactory;
import data.Document;
import data.Mention;
import data.Sentence;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.tregex.TregexMatcher;
import edu.stanford.nlp.trees.tregex.TregexPattern;

public class FindMentions {
	public static void go(Document d) {
		System.out.println("\n***  Find Mentions  ***\n");

		String patS = "NP !>># NP"; //needs to be the maximum projection of a head word, or a conjunction
		TregexPattern pat = TregexPatternFactory.getPattern(patS);
		for (Sentence s : d.sentences()) {
			TregexMatcher matcher = pat.matcher(s.rootNode());
			while (matcher.find()) {
				Tree match = matcher.getMatch();
				d.newMention(s, match);
			}
		}
	}
	public static void main(String[] args) throws IOException {
		Document d = Document.loadFiles("/d/arkref/data/lcross");
		FindMentions.go(d);
	}
}

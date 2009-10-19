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

		int id=0;
		String patS = "NP";
		TregexPattern pat = TregexPatternFactory.getPattern(patS);
		for (Sentence s : d.sentences) {
			TregexMatcher matcher = pat.matcher(s.root);
			while (matcher.find()) {
				Tree match = matcher.getMatch();
				Mention mention = new Mention(++id, s, match);
				System.out.println("MENTION "+mention);
				d.mentions.add(mention);
				d.node2mention.put(match, mention);
			}
		}
	}
	public static void main(String[] args) throws IOException {
		Document d = Document.loadFiles("/d/arkref/data/lcross");
		FindMentions.go(d);
	}
}

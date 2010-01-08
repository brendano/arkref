package analysis;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


import parsestuff.TregexPatternFactory;
import parsestuff.U;
import data.Document;
import data.Sentence;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.tregex.TregexMatcher;
import edu.stanford.nlp.trees.tregex.TregexPattern;

public class FindMentions {
	public static void go(Document d) {
		U.pl("\n***  Find Mentions  ***\n");
		for (Sentence s : d.sentences()){
			for(Tree match: findMentionNodes(s.rootNode())){
				d.newMention(s, match);
			}
		}
	}
	
	public static List<Tree> findMentionNodes(Tree root){
		List<Tree> res = new ArrayList<Tree>();
		
		String patS = "NP !>># NP"; //needs to be the maximum projection of a head word, or a conjunction
		TregexPattern pat = TregexPatternFactory.getPattern(patS);
		TregexMatcher matcher = pat.matcher(root);
		while (matcher.find()) {
			res.add(matcher.getMatch());
		}
		return res;
	}
	
	public static void main(String[] args) throws IOException {
		Document d = Document.loadFiles("/d/arkref/data/lcross");
		FindMentions.go(d);
	}
}

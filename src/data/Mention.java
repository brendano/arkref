package data;

import java.util.List;

import parsestuff.TregexPatternFactory;

import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.tregex.TregexMatcher;

public class Mention {
	public Tree node;
	public Sentence sentence;
	public int id;
	public Mention(int id, Sentence sentence, Tree node) { this.id=id; this.sentence=sentence; this.node=node; }
	public String neType() {
		// using head word strongly outperforms using right-most
		List<Tree> leaves = node.getLeaves();
		Tree rightmost = leaves.get(leaves.size()-1);
//		System.out.println(rightmost);
		return sentence.neType(rightmost);
	}
	public String toString() { 
		return String.format("M%-3d : S%-2d : %-12s : %s", id, sentence.id, neType(), node); 
	}
	
}
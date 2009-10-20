package data;

import java.util.List;

import parsestuff.AnalysisUtilities;

import edu.stanford.nlp.trees.Tree;

public class Mention {
	private Tree node;

	private Sentence sentence;


	private int id;
	
	public Mention(int id, Sentence sentence, Tree node) { this.id=id; this.sentence=sentence; this.node=node; }
	public String neType() {
		// using head word strongly outperforms using right-most
		//List<Tree> leaves = node.getLeaves();
		//Tree rightmost = leaves.get(leaves.size()-1);
		//return sentence.neType(rightmost);
		Tree head = node.headTerminal(AnalysisUtilities.getInstance().getHeadFinder()); 
		return sentence.neType(head);
	}
	public String toString() { 
		return String.format("M%-3d : S%-2d : %-12s : %s", id, sentence.getID(), neType(), node); 
	}
	
	public int getID() {
		return id;
	}
	public Tree getNode() {
		return node;
	}
	
	public Sentence getSentence() {
		return sentence;
	}
	
}
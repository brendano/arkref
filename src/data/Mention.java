package data;

import analysis.Types;
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
	
	public boolean isName() { return !neType().equals("O"); }
	
	public String toString() { 
		String g = safeToString(Types.gender(this));
		String n = safeToString(Types.number(this));
		String p = safeToString(Types.personhood(this));
		return String.format("M%-2d | S%-2d | %3s %2s %4s | %-12s | %s", id, sentence.getID(), 
				g, n, p, neType(), node); 
	}
	public String safeToString(Object o) {
		if (o==null) return "";
		return o.toString();
	}
	
	public int getID() {
		return id;
	}
	
	public Tree getNode() {
		return node;
	}
	
	public String getHeadWord(){
		return node.headTerminal(AnalysisUtilities.getInstance().getHeadFinder()).yield().toString();
	}
	
	public Sentence getSentence() {
		return sentence;
	}
	
}
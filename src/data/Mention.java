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
		if (node==null) {
			// TODO wrong!!!  can get from Word alignment
			return "O";
		}
		Tree head = node.headTerminal(AnalysisUtilities.getInstance().getHeadFinder()); 
		return sentence.neType(head);
	}
	
	public boolean isName() { return !neType().equals("O"); }
	
	public String toString() { 
		String g = safeToString(Types.gender(this));
		String n = safeToString(Types.number(this));
		String p = safeToString(Types.personhood(this));
		return String.format("M%-2d | S%-2d | %3s %2s %4s | %-12s | %s", id, sentence.ID(), 
				g, n, p, neType(), node); 
	}
	public String safeToString(Object o) {
		if (o==null) return "";
		return o.toString();
	}
	
	public int ID() {
		return id;
	}
	
	public Tree node() {
		return node;
	}
	
	public String getHeadWord(){
		if (node==null) {
			// TODO tricky: use the token span alignments and do guesswork if length>1.
			// for now, bailing...
			return "NO_HEAD_WORD";
		}
		return node.headTerminal(AnalysisUtilities.getInstance().getHeadFinder()).yield().toString();
	}
	
	public Sentence getSentence() {
		return sentence;
	}
	
}
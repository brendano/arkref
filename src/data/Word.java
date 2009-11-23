/**
 * 
 */
package data;

import edu.stanford.nlp.trees.Tree;

public class Word {
	/** node could be null on parse failure **/
	private Tree node;
	private String neTag;
	public int charStart = -1; // in raw original text
	public String token;
//	public Sentence sentence; // enclosing sentence, just for convenience
	
	public Tree getNode() {
		return node;
	}
	
	public void setNode(Tree node) {
		this.node = node;
	}
	
	public String getNeTag() {
		return neTag;
	}
	
	public void setNeTag(String neTag) {
		this.neTag = neTag;
	}
	
	public String toString() { 
		String s = token;
//		s += "/" + (node!=null ? node.parent().value() : "null"); // wrong
		s += "/" + neTag;
		return s;
	}
}
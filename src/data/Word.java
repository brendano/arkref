/**
 * 
 */
package data;

import edu.stanford.nlp.trees.Tree;

public class Word {
	private Tree node;
	private String neTag;
	public int charStart = -1; // in raw original text
	
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
	
	public String toString(){ return node.toString()+"/"+neTag; }
}
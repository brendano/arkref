/**
 * 
 */
package data;

import edu.stanford.nlp.trees.Tree;

public class Word {
	public Tree node;
	public String neTag;
	public String toString(){ return node.toString()+"/"+neTag; }
}
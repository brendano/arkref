/**
 * 
 */
package data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import edu.stanford.nlp.trees.Tree;

public class Sentence {
	private List<Word> words;
	private Map<String,Word> node2wordMap;
	public Tree rootNode;


	private int id;

	public Sentence(int id) { this.id = id; words=new ArrayList<Word>(); node2wordMap=new HashMap<String,Word>(); }

	public void setStuff(Tree root, String neTagging) {
		this.rootNode = root;
		String[] neTaggedWords = neTagging.split(" ");
		List<Tree> leaves = root.getLeaves();
		assert neTaggedWords.length == leaves.size();
		for (int i=0; i < leaves.size(); i++) {
			Word word = new Word();
			word.setNode(leaves.get(i));
			String[] parts = neTaggedWords[i].replace("\\/", "_SLASH_").split("/");
			assert parts.length == 2;
			word.setNeTag(parts[1]);
			//				System.out.println(word);
			words.add(word);
			set_node2word(word.getNode(), word);
		}
	}

	public Word node2word(Tree node) {
		String key = nodeKey(node);
		return node2wordMap.get(key);
	}
	
	public String nodeKey(Tree node) {
		return String.format("node_%s_%s", rootNode.leftCharEdge(node), node.hashCode());
	}
	
	public void set_node2word(Tree node, Word w) {
		String key = nodeKey(node);
		node2wordMap.put(key, w);
	}


	public String neType(Tree leaf) {
		assert leaf.isLeaf();
		Word w = node2word(leaf);
		//			if (w == null) return null;
		return w.getNeTag();
	}

	public String text() {
		// oops we don't have original anymore.  but maybe we don't want it.
		ArrayList<String> toks = new ArrayList<String>();
		for (Tree L : rootNode.getLeaves()) {
			toks.add(L.label().toString());
		}
		return StringUtils.join(toks, " ");
	}

	public Tree getRootNode() {
		return rootNode;
	}


	public int getID() {
		return id;
	}
}
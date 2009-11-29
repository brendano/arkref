/**
 * 
 */
package data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;

import parsestuff.U;

import sent.SentenceBreaker;

import edu.stanford.nlp.trees.Tree;

/**
 * Our notion of a sentence, integrating information from all subsystems (or at least maintaining pointers to those data structures)
 * 
 * Note that this is different than sent.SentenceBreaker.Sentence, which only contains early-stage surface information about a sentence.
 * 
 * @author brendano
 *
 */
public class Sentence {
	public List<Word> words;
	private Map<String,Word> node2wordMap;
	private Tree rootNode;
	public boolean hasParse;
	/** optional: more surface info **/
	public SentenceBreaker.Sentence surfSent = null;

	private int id;

	public Sentence(int id) { this.id = id; words=new ArrayList<Word>(); node2wordMap=new HashMap<String,Word>(); }

	public void setStuff(Tree root, String neTagging, boolean parseSuccess) {
		this.setRootNode(root);
		String[] neTaggedWords = neTagging.split(" ");
		List<Tree> leaves = root.getLeaves();
		assert !parseSuccess || neTaggedWords.length == leaves.size();
		
		for (int i=0; i < neTaggedWords.length; i++) {
			Word word = new Word();
			word.sentence = this;
			
			String[] parts = neTaggedWords[i].split("/");
			word.setNeTag(parts[parts.length-1]);
			String nerToken = StringUtils.join(ArrayUtils.subarray(parts, 0, parts.length-1), "/");
			
			if (parseSuccess) {
				word.setNode(leaves.get(i));
				assert nerToken.equals( word.node().value() ) : String.format("NER and parser tokens disagree: [%s] vs [%s]", word.token, word.node().value());
				set_node2word(word.node(), word);
			}
			word.token = nerToken.replace("\\/", "/");
			words.add(word);

		}
	}

	public Word node2word(Tree node) {
		String key = nodeKey(node);
		return node2wordMap.get(key);
	}
	
	public String nodeKey(Tree node) {
		return String.format("node_%s_%s", rootNode().leftCharEdge(node), node.hashCode());
	}
	
	public void set_node2word(Tree node, Word w) {
		String key = nodeKey(node);
		node2wordMap.put(key, w);
	}


	public String neType(Tree leaf) {
		assert leaf.isLeaf();
		Word w = node2word(leaf);
		//			if (w == null) return null;
		return w.neTag();
	}

	public String text() {
		// oops we don't have original anymore.  but maybe we don't want it.
		ArrayList<String> toks = new ArrayList<String>();
		for (Tree L : rootNode().getLeaves()) {
			toks.add(L.label().toString());
		}
		return StringUtils.join(toks, " ");
	}
	
	public String[] tokens() {
		List<Tree> leaves = rootNode().getLeaves();
		String[] toks = new String[leaves.size()];
		for (int i=0; i<leaves.size(); i++)
			toks[i] = leaves.get(i).value();
		return toks;
	}

	public Tree rootNode() {
		return rootNode;
	}


	public int ID() {
		return id;
	}

	public void setRootNode(Tree rootNode) {
		this.rootNode = rootNode;
	}
}
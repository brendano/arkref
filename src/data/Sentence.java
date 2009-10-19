/**
 * 
 */
package data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import edu.stanford.nlp.trees.Tree;

public class Sentence {
		public ArrayList<Word> words;
		public HashMap<Tree,Word> node2word;
		public Tree root;
		public int id;
		
		public Sentence(int id) { this.id=id; words=new ArrayList<Word>(); node2word=new HashMap<Tree,Word>(); }
		
		public void setStuff(Tree root, String neTagging) {
			this.root = root;
			String[] neTaggedWords = neTagging.split(" ");
			List<Tree> leaves = root.getLeaves();
			assert neTaggedWords.length == leaves.size();
			for (int i=0; i < leaves.size(); i++) {
				Word word = new Word();
				word.node = leaves.get(i);
				String[] parts = neTaggedWords[i].replace("\\/", "_SLASH_").split("/");
				assert parts.length == 2;
				word.neTag = parts[1];
//				System.out.println(word);
				words.add(word);
				node2word.put(word.node, word);
			}
		}
		
		public String neType(Tree leaf) {
			assert leaf.isLeaf();
			assert node2word.containsKey(leaf);
			Word w = node2word.get(leaf);
//			if (w == null) return null;
			return w.neTag;
		}
		
		public String text() {
			// oops we don't have original anymore.  but maybe we don't want it.
			ArrayList<String> toks = new ArrayList();
			for (Tree L : root.getLeaves()) {
				toks.add(L.label().toString());
			}
			return StringUtils.join(toks, " ");
		}
	}
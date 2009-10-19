package parsestuff;

import java.util.Iterator;

import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.tregex.TregexMatcher;
import edu.stanford.nlp.trees.tregex.TregexPattern;

public class TregexMatchIter implements Iterable<Tree> {
	String pat;
	Tree root;
	public TregexMatchIter(Tree root, String pat) {
		this.pat=pat;
		this.root=root;
	}

	@Override
	public Iterator<Tree> iterator() {
		
		
		
		// TODO Auto-generated method stub
		return null;
	}
	
	public class TMIterator implements Iterator {
		TregexMatcher matcher;
		Tree cur;
		public TMIterator() {
			TregexPattern tpat = TregexPatternFactory.getPattern(pat);
			TregexMatcher matcher = tpat.matcher(root);
			
			cur = null;			
		}
		@Override
		public boolean hasNext() {
			if (cur != null) return true;
			// cur is null
			
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public Object next() {
			if (cur==null) {
				Tree ret = cur;
				cur = null;
				return ret;
			}
			return null;
		}

		@Override
		public void remove() {
			System.out.println("Illegal can't remove");
		}
		
	}
	
}

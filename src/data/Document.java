package data;

import java.util.*;
import java.io.*;

import analysis.FindMentions;
import analysis.Preprocess;

import parsestuff.AnalysisUtilities;
import parsestuff.TregexPatternFactory;

import edu.stanford.nlp.trees.LabeledScoredTreeFactory;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreeFactory;
import edu.stanford.nlp.trees.tregex.TregexMatcher;
import edu.stanford.nlp.trees.tregex.TregexPattern;
import edu.stanford.nlp.util.IntPair;

public class Document {
	private ArrayList<Sentence> sentences;
	private ArrayList<Mention> mentions;
	private Map<String, Mention> node2mention;
	private RefGraph refGraph;
	private Tree tree = null; //tree that includes all the trees for the sentences, in order, under a dummy node	
	private EntityGraph entGraph;


	public Document() {
		sentences = new ArrayList<Sentence>();
		mentions = new ArrayList<Mention>();
		node2mention = new HashMap<String,Mention>();
		refGraph = new RefGraph();
	}

	
	/**
	 * if there is no mention for the given node, this will walk up the tree 
	 * to try to find one, as in H&K EMNLP 09.   Such a method is necessary
	 * because the test data coref labels may not match up with constituents exactly
	 * 
	 * @param s
	 * @param node
	 * @return
	 */
	public Mention findMentionDominatingNode(int sentenceIndex, Tree node) {
		String key; 
		Mention res = null;
		Tree tmpNode = node;
		
		if(sentenceIndex >= sentences.size()){
			return null;
		}
		
		Sentence s = sentences.get(sentenceIndex);
		
		do{
			key = nodeKey(s, tmpNode);
			res = node2mention.get(key);
			tmpNode = tmpNode.parent(s.getRootNode());
		}while(res == null && tmpNode != null);
			
		return res;
	}
	
	
	/**
	 * Given a span defined by indexes for the sentence, start token, and end token,
	 * this method returns the smallest node that includes that span. 
	 * 
	 * @param sentenceIndex
	 * @param spanStart inclusive
	 * @param spanEnd inclusive
	 * @return
	 */
	public Tree findNodeThatCoversSpan(int sentenceIndex, int spanStart, int spanEnd){
		Tree res = null;

		if(sentenceIndex >= sentences.size()){
			return null;
		}
		Sentence sent = sentences.get(sentenceIndex);

		int smallestSpan = 999999;
		int nodeSpanLength;

		List<Tree> leaves = sent.getRootNode().getLeaves();
		if(spanStart < 0 || leaves.size() == 0 || spanEnd >= leaves.size()){
			return null;
		}
		
		Tree startLeaf = leaves.get(spanStart);
		Tree endLeaf = leaves.get(spanEnd);
		
		for(Tree t: sent.getRootNode().subTrees()){
				if(!(t.dominates(startLeaf) && t.dominates(endLeaf))){
					continue;
				}
				
				nodeSpanLength = t.getLeaves().size();
				if(smallestSpan > nodeSpanLength){
					smallestSpan = nodeSpanLength;
					res = t;
				}
			
		}

		return res;
	}


	public Mention node2mention(Sentence s, Tree node) {
		String key = nodeKey(s,node);
		return node2mention.get(key);
	}

	public String nodeKey(Sentence s, Tree node) {
		return String.format("sent_%s_node_%s_%s", s.getID(), s.getRootNode().leftCharEdge(node), node.hashCode());
	}

	public void set_node2mention(Sentence s, Tree node, Mention m) {
		String key = nodeKey(s,node);
		node2mention.put(key, m);
	}

	public static Document loadFiles(String path) throws IOException {
		Document d = new Document();

		String shortpath = Preprocess.shortPath(path);

		String parseFilename = shortpath + ".parse";
		String neFilename = path = shortpath + ".ner";
		BufferedReader parseR = new BufferedReader(new FileReader(parseFilename));
		BufferedReader nerR = new BufferedReader(new FileReader(neFilename));
		String parse; String ner;
		int curSentId=0;
		while ( (parse = parseR.readLine()) != null) {
			parse = parse.replace("=H ", " ");
			Tree tree = AnalysisUtilities.getInstance().readTreeFromString(parse);
			Document.addNPsAbovePossessivePronouns(tree);
			Document.addInternalNPStructureForRoleAppositives(tree);
			ner = nerR.readLine();
			Sentence sent = new Sentence(++curSentId);
			sent.setStuff(tree, ner);
			d.sentences.add(sent);
		}
		System.out.printf("***  Input %s  ***\n\n", shortpath);
		for (Sentence s : d.sentences) {
			System.out.printf("S%-2d\t%s\n", s.getID(), s.text());
		}

		return d;
	}


	private static void addNPsAbovePossessivePronouns(Tree tree) {
		TreeFactory factory = new LabeledScoredTreeFactory(); //TODO might want to keep this around to save time
		String patS = "NP=parentnp < /^PRP\\$/=pro"; //needs to be the maximum projection of a head word
		TregexPattern pat = TregexPatternFactory.getPattern(patS);
		TregexMatcher matcher = pat.matcher(tree);
		while (matcher.find()) {
			Tree parentNP = matcher.getNode("parentnp");
			Tree pro = matcher.getNode("pro");
			Tree newNP = factory.newTreeNode("NP", new ArrayList<Tree>());
			int index = parentNP.indexOf(pro);

			newNP.addChild(pro);
			parentNP.removeChild(index);
			parentNP.addChild(index, newNP);

		}
	}


	private static void addInternalNPStructureForRoleAppositives(Tree tree) {
		TreeFactory factory = new LabeledScoredTreeFactory(); //TODO might want to keep this around to save time
		String patS = "NP=parentnp < (NN|NNS=role . NNP|NNPS)";
		TregexPattern pat = TregexPatternFactory.getPattern(patS);
		TregexMatcher matcher = pat.matcher(tree);
		Tree newNode;

		while (matcher.find()) {
			Tree parentNP = matcher.getNode("parentnp");
			Tree roleNP = matcher.getNode("role");
			Tree tmpTree;
			
			newNode = factory.newTreeNode("NP", new ArrayList<Tree>());
			int i = parentNP.indexOf(roleNP);
			while(i>=0){
				tmpTree = parentNP.getChild(i);
				if(!tmpTree.label().value().matches("^NN|NNS|DT|JJ|ADVP$")){
					break;
				}
				newNode.addChild(0, tmpTree);
				parentNP.removeChild(i);
				i--;
			}
			
			parentNP.addChild(i+1, newNode);
			
		}
	}



	/** goes backwards through document **/
	public Iterable<Mention> prevMentions(final Mention start) {
		return new Iterable<Mention>() {
			public Iterator<Mention> iterator() {
				return new MentionRevIterIter(start);
			}
		};
	}
	public class MentionRevIterIter implements Iterator<Mention> {
		int mi = -1;
		public MentionRevIterIter(Mention start) {
			for (int i=0; i < mentions.size(); i++) {
				if (mentions.get(i) == start) {
					this.mi = i;
					break;
				}
			} 
			assert mi != -1;
		}

		@Override
		public boolean hasNext() {
			return mi > 0;
		}

		@Override
		public Mention next() {
			if (mi==-1) return null;
			mi--;
			if (mi==-1) return null;
			return mentions.get(mi);

		}

		// why-t-f did i write this?
		//		if (!filterToRemaining) {
		//			mi--;				
		//		} else {
		//			do {
		//				mi--;
		//				if (refGraph.needsReso(mentions.get(mi))) break;
		//			} while (mi != -1);
		//		}

		@Override
		public void remove() {	
			System.out.println("bad");			
		}

	}


	/**
	 * make a right branching tree out of all the sentence trees
	 * e.g., (DOCROOT T1 (DOCROOT T2 (DOCROOT T3))) 
	 * This will make sure that ndoes in t3 are further from nodes in t1 
	 * than they are fromnodes in t2.
	 * 
	 * @return
	 */
	public Tree getTree() {
		if(tree == null){
			TreeFactory factory = new LabeledScoredTreeFactory();
			tree = factory.newTreeNode("DOCROOT", new ArrayList<Tree>());
			Tree tmpTree1 = tree;
			Tree tmpTree2;
			for(int i=0; i<sentences.size(); i++){
				tmpTree1.addChild(sentences.get(i).getRootNode());
				if(i<sentences.size()-1){ 
					tmpTree2 = factory.newTreeNode("DOCROOT", new ArrayList<Tree>());
					tmpTree1.addChild(tmpTree2);
					tmpTree1 = tmpTree2;
				}
			}

		}
		return tree;
	}

	public ArrayList<Mention> getMentions() {
		return mentions;
	}

	public ArrayList<Sentence> getSentences() {
		return sentences;
	}

	public RefGraph getRefGraph() {
		return refGraph;
	}

	public void setEntGraph(EntityGraph entGraph) {
		this.entGraph = entGraph;
	}

	public EntityGraph getEntGraph() {
		return entGraph;
	}

}

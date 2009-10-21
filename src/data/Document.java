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

public class Document {
	private List<Sentence> sentences;
	private List<Mention> mentions;
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
		System.out.printf("***  Input %s  ***\n\n", path);
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
		String patS = "NP=parentnp < NNP < NN";
		TregexPattern pat = TregexPatternFactory.getPattern(patS);
		TregexMatcher matcher = pat.matcher(tree);
		
		String prevLabelS = "";
		String curLabelS;
		Tree tmp;
		int start;
		Tree newNode;
		
		while (matcher.find()) {
			Tree parentNP = matcher.getNode("parentnp");
			start = -1;
			boolean endOfSubseq;
			
			for(int i=0; i<parentNP.numChildren(); i++){
				endOfSubseq = false;
				tmp = parentNP.getChild(i);
				curLabelS = tmp.label().value();
				
				if(start == -1){
					//if(curLabelS.matches("^NNP|NN|JJ|DT$")){
					if(curLabelS.matches("^NN|JJ|DT$")){
						start = i;
					}
				}else{
					//if(prevLabelS.equals("NNP")){
					//	endOfSubseq = !curLabelS.equals("NNP");
					//}else
						if(prevLabelS.matches("^NN|JJ|DT$")){
						endOfSubseq = !curLabelS.matches("^NN|JJ|DT$");
					}
						
				}
					
				if(endOfSubseq){
					//System.err.println("start="+start+" i="+i);
					newNode = factory.newTreeNode("NP", new ArrayList<Tree>());
					for(int j=0; j<i-start; j++){
						newNode.addChild(parentNP.getChild(start));
						parentNP.removeChild(start);
					}
					parentNP.addChild(start, newNode);
					
					//adjust the index since we changed the list of children 
					//(probably not the cleanest way to do this...)
					i=start+1;
					
					//if(curLabelS.matches("^NNP|NN|JJ|DT$")){
					if(curLabelS.matches("^NN|JJ|DT$")){
						start = i;
					}else{
						start = -1;
					}
				}
				
				prevLabelS = curLabelS;
			}

			//if the last subsequence ended the list of children, make sure to add it too
			if(start != -1){
				newNode = factory.newTreeNode("NP", new ArrayList<Tree>());
				for(int j=0; j<parentNP.numChildren()-start+1; j++){
					//System.err.println(j+" "+parentNP.numChildren());
					newNode.addChild(parentNP.getChild(start));
					parentNP.removeChild(start);
				}
				parentNP.addChild(start, newNode);
			}
			
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
	
	
	public Tree getTree() {
		if(tree == null){
			TreeFactory factory = new LabeledScoredTreeFactory();
			tree = factory.newTreeNode("DOCROOT", new ArrayList<Tree>());
			for(int i=0; i<sentences.size(); i++){
				tree.addChild(sentences.get(i).getRootNode());
			}
			
		}
		return tree;
	}
	
	public List<Mention> getMentions() {
		return mentions;
	}

	public List<Sentence> getSentences() {
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

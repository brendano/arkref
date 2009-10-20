package data;

import java.util.*;
import java.io.*;

import parsestuff.AnalysisUtilities;

import edu.stanford.nlp.trees.LabeledScoredTreeFactory;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreeFactory;

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
	
	public static Document loadFiles(String baseFilename) throws IOException {
		Document d = new Document();
		String parseFilename = baseFilename + ".parse";
		String neFilename = baseFilename = baseFilename + ".ner";
		BufferedReader parseR = new BufferedReader(new FileReader(parseFilename));
		BufferedReader nerR = new BufferedReader(new FileReader(neFilename));
		String parse; String ner;
		int curSentId=0;
		while ( (parse = parseR.readLine()) != null) {
			parse = parse.replace("=H ", " ");
			Tree tree = AnalysisUtilities.getInstance().readTreeFromString(parse);
			ner = nerR.readLine();
			Sentence sent = new Sentence(++curSentId);
			sent.setStuff(tree, ner);
			d.sentences.add(sent);
		}
		System.out.printf("***  Input %s  ***\n\n", baseFilename);
		for (Sentence s : d.sentences) {
			System.out.printf("S%-2d\t%s\n", s.getID(), s.text());
		}

		return d;
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

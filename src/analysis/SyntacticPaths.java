package analysis;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import parsestuff.AnalysisUtilities;

import data.Document;
import data.Mention;
import edu.stanford.nlp.trees.HeadFinder;
import edu.stanford.nlp.trees.Tree;

public class SyntacticPaths {
	
	
	/**
	 * finds the closest candidate by looking at the syntactic path distance
	 * 
	 * @param mention 
	 * @param candidates other mentions that appeared previously
	 * @return
	 */
	public static Mention findBestCandidateByShortestPath(Mention mention, List<Mention> candidates, Document document) {
		int minLength = 1000000;
		int minIndex = 0;
		Mention res; 
		
		List<Integer> pathLengths = scoreCandidatesByPathLength(mention, candidates, document);
		
		//Mention tmpCandidate;
		int tmp;
		for(int i=0; i<pathLengths.size(); i++){
			tmp = pathLengths.get(i);
			if(tmp < minLength){
				minLength = tmp;
				minIndex = i;
			}
		}
				
		res = candidates.get(minIndex);
		return res;
	}
	
	
	public static List<Integer> scoreCandidatesByPathLength(Mention mention, List<Mention> candidates, Document doc) {
		List<Integer> pathLengths = new ArrayList<Integer>();
		
		Iterator<Mention> iter = candidates.iterator();
		Mention tmpCandidate;
		while(iter.hasNext()){
			tmpCandidate = iter.next();
			pathLengths.add(computePathLength(mention.getNode(), tmpCandidate.getNode(), doc.getTree()));
			
		}
		
		return pathLengths;
	}
	
	


	/**
	 * 
	 * @param node1
	 * @param node2
	 * @param commonRoot should contain both node1 and node2
	 * @return
	 */
	public static int computePathLength(Tree node1, Tree node2, Tree commonRoot) {
		int res = 1000;
		
		/*
		//find the node in the tree that dominates both input nodes
		int len1 = 0;
		int len2 = 0;
		Tree tmpNode = node1;
		List<Tree> dominationPath;
		while(tmpNode != null){
			dominationPath = tmpNode.dominationPath(node2);
			if(dominationPath != null){
				len2 = dominationPath.size()-1;
			}
		
			tmpNode = tmpNode.parent(commonRoot);
			len1++;
		}
		
		//sum the distances from each input node to their common ancestor
		res = len1+len2;*/
		
		List<Tree> path = commonRoot.pathNodeToNode(node1, node2);
		if(path != null){
			res = path.size()-1;
		}
		
		return res;
	}


	public static boolean aIsDominatedByB(Mention A, Mention B) {
		boolean bDominatesA = B.getNode().dominates(A.getNode());
		

		return bDominatesA;
	}


	public static Tree getMaximalProjection(Tree parent, Tree root) {
		Tree res = parent;
		Tree tmp = parent;
		HeadFinder hf = AnalysisUtilities.getInstance().getHeadFinder();
		Tree parentHead = parent.headTerminal(hf);
		
		while(tmp != null){
			tmp = res.parent(root);
			if(tmp.headTerminal(hf) == parentHead){
				res = tmp;
			}else{
				break;
			}
		}
		
		return res;
	}

	
}

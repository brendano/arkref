package analysis;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import parsestuff.AnalysisUtilities;
import parsestuff.TregexPatternFactory;
import data.Document;
import data.Mention;
import data.Sentence;
import edu.stanford.nlp.trees.HeadFinder;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.tregex.TregexMatcher;
import edu.stanford.nlp.trees.tregex.TregexPattern;

public class Resolve {
	public static void go(Document d) {
		System.out.println("\n***  Resolve ***\n");
		Mention antecedent;
		
		for (Mention m : d.getMentions()) {
			System.out.println("= Resolving\t" + m);
			
			if (Types.isPronominal(m)) {
				resolvePronoun(m, d);
			} else if (inAppositiveConstruction(m)) {
				resolveAppositive(m, d);
			} else if ((antecedent = findAntecedentInRoleAppositiveConstruction(m,d)) != null) {
				d.getRefGraph().setRef(m, antecedent);		
			} else if ((antecedent = findAntecendentInPredicateNominativeConstruction(m, d)) != null) {
				d.getRefGraph().setRef(m, antecedent);		
			} else {
				resolveOther(m, d);
			}
		}
	}
	
	
	/**
	 * 
	 * Note: This is slightly different than what is described in H&K EMNLP 09.
	 * I think the head rules they used were slightly different (or possibly their description is a little off).
	 * 
	 * @param m
	 * @param d
	 * @return
	 */
	private static Mention findAntecedentInRoleAppositiveConstruction(Mention m, Document d) {
		Tree root = m.getSentence().getRootNode();
		Tree node = m.getNode();
		Tree parent = node.parent(root);
		
		//System.err.println("mention:"+node.yield().toString()+"\thead:"+node.headTerminal(AnalysisUtilities.getInstance().getHeadFinder()).yield().toString());
		if(!parent.label().value().equals("NP")){
			return null;
		}
		
		int index = parent.indexOf(node);
		if(index+1 >= parent.numChildren()){
			return null;
		}
		
		
		TregexPattern pat = TregexPatternFactory.getPattern("NP=parent !> __ <<# (NNP=head ,, NP=mention)");
		TregexMatcher matcher = pat.matcher(parent);
		while (matcher.find()) {
			if (matcher.getNode("mention") == node){
				Tree head = matcher.getNode("head");

				//find maximal projection of the head of the parent
				Tree maxProj = SyntacticPaths.getMaximalProjection(head, root);
				
				//find the mention for the parent
				for(Mention cand:d.getMentions()){
					if(cand.getNode() == maxProj){
						if(cand.neType().matches("^PER.*$")){
							return cand;
						}
						break;
					}
				}
			}
		}
		
		return null;
	}

	/**
	 * returns the antecedent NP or null
	 * The way this method is called could be made more efficient.  
	 * It doesn't really need to get called for every mention
	 * 
	 */
	private static Mention findAntecendentInPredicateNominativeConstruction(Mention m, Document d) {
		Tree root = m.getSentence().getRootNode();
		Tree node = m.getNode();
		
		TregexPattern pat = TregexPatternFactory.getPattern("S < NP=np1 <+(VP) (VP < (/^VB.*/ < be|is|was|were|are|being|been) < NP=np2)");
		TregexMatcher matcher = pat.matcher(root);
		while (matcher.find()) {
			if(matcher.getNode("np2") == node){
				Tree ante  = matcher.getNode("np1");
				for(Mention m2: d.getMentions()){
					if(ante == m2.getNode()){
						return m2;
					}
				}
			}
		}
		
		return null;
	}

	
	/**
	 * return true when m is the third child in of a parent who expands as
	 * NP -> NP , NP .*
	 * 
	 * @param m
	 * @return
	 */
	private static boolean inAppositiveConstruction(Mention m) {
		Tree root = m.getSentence().getRootNode();
		Tree node = m.getNode();
		Tree parent = node.parent(root);
		
		if(parent.numChildren()<3){
			return false;
		}else if(!parent.getChild(0).label().value().equals("NP")){
			return false;
		}else if(!parent.getChild(1).label().value().equals(",")){
			return false;
		}else if(parent.indexOf(node) != 2){
			return false;
		}
	
		//check to make sure this isn't a conjunction
		for(Tree sibling: parent.getChildrenAsList()){
			if(sibling.label().value().equals("CC")){
				return false;
			}
		}
		
		return true;
	}

	
	public static void resolveAppositive(Mention mention, Document d) {
		Tree root = mention.getSentence().getRootNode();
		Tree node = mention.getNode();
		Tree parent = node.parent(root);
		
		for (Mention cand : d.prevMentions(mention)) {
			if(cand.getNode() == parent){
				d.getRefGraph().setRef(mention, cand);
				break;
			}
		}
		
		
		Mention ref = d.getRefGraph().getFinalResolutions().get(mention);
		if(ref != null){
			System.out.printf("resolved appositive M%-2d -> M%-2d    %20s    ->   %-20s\n", 
					mention.getID(), ref.getID(), AnalysisUtilities.abbrevTree(mention.getNode()),
					 AnalysisUtilities.abbrevTree(ref.getNode()));
		}
	}
	
	
	
	public static void resolvePronoun(Mention mention, Document d) {
		System.out.println("trying to resolve as a pronoun");
		
		ArrayList<Mention> candidates = new ArrayList<Mention>();
		
		for (Mention cand : d.prevMentions(mention)) {
			boolean match = Types.checkPronominalMatch(mention, cand);
			
			
			if (SyntacticPaths.aIsDominatedByB(mention, cand)){ // I-within-I constraint 
				match = false;
			} else if (!Types.isReflexive(mention) && SyntacticPaths.inSubjectObjectRelationship(cand, mention)){
				match = false;
			} else if (SyntacticPaths.isSubjectAndMentionInAdjunctPhrase(mention, cand)){
				match = false;
			}
			
			if (match) {
				System.out.println("yay    match: " + cand);
				candidates.add(cand);
			} else {
				System.out.println("reject mismatch:  " + cand);
			}
		}
		if (candidates.size() == 0) {
			System.out.println("No legal candidates");
			d.getRefGraph().setNullRef(mention);
		} else if (candidates.size() == 1) {
			System.out.println("Single legal resolution");
			d.getRefGraph().setRef(mention, candidates.get(0));
		} else if (candidates.size() > 1) {
			System.out.println("Finding pronoun antecedent by shortest syntactic path");
			d.getRefGraph().setRef(mention, SyntacticPaths.findBestCandidateByShortestPath(mention, candidates, d)); 
		}
		Mention ref = d.getRefGraph().getFinalResolutions().get(mention);
		if(ref != null){
			System.out.printf("resolved pronoun M%-2d -> M%-2d    %20s    ->   %-20s\n", 
				mention.getID(), ref.getID(), AnalysisUtilities.abbrevTree(mention.getNode()),
				 AnalysisUtilities.abbrevTree(ref.getNode()));
		}
	}



	public static void resolveOther(Mention mention, Document d) {
		//TODO SEMANTICS!
		
		ArrayList<Mention> candidates = new ArrayList<Mention>();
		boolean match = false;
		
		for (Mention cand : d.prevMentions(mention)) {
			if (SyntacticPaths.aIsDominatedByB(mention, cand)) { // I-within-I constraint 
				match = false;
			} else if(SyntacticPaths.inSubjectObjectRelationship(cand, mention)){
				match = false;
			} else if (SyntacticPaths.isSubjectAndMentionInAdjunctPhrase(mention, cand)){
				match = false;
			} else if(SyntacticPaths.haveSameHeadWord(mention, cand)) { //matching head word 
				//TODO keep this or not?
				match = true;
			} else {
				match = false;
			}			
			if (match) {
				System.out.println("yay   match:\t" + cand);
				candidates.add(cand);
			} else {
				System.out.println("reject mismatch:\t" + cand);
			}
		}
		
		if (candidates.size() == 0) {
			System.out.println("No legal candidates");
			d.getRefGraph().setNullRef(mention);
		} else if (candidates.size() == 1) {
			System.out.println("Single legal resolution");
			d.getRefGraph().setRef(mention, candidates.get(0));
		} else if (candidates.size() > 1) {
			System.out.println("Finding pronoun antecedent by shortest syntactic path");
			d.getRefGraph().setRef(mention, SyntacticPaths.findBestCandidateByShortestPath(mention, candidates, d)); 
		}
		
		Mention ref = d.getRefGraph().getFinalResolutions().get(mention);
		if(ref != null){
			System.out.printf("resolved after filtering M%-2d -> M%-2d    %20s    ->   %-20s\n", 
				mention.getID(), ref.getID(), AnalysisUtilities.abbrevTree(mention.getNode()),
				 AnalysisUtilities.abbrevTree(ref.getNode()));
		}
		
		//semantics!
	}



	
}

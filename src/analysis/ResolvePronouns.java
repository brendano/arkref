package analysis;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import parsestuff.AnalysisUtilities;
import parsestuff.TregexPatternFactory;
import data.Document;
import data.Mention;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.tregex.TregexMatcher;
import edu.stanford.nlp.trees.tregex.TregexPattern;

public class ResolvePronouns {
	public static void go(Document d) {
		System.out.println("\n***  Resolve Pronouns  ***\n");

		for (Mention m : d.getMentions()) {
			if (isPronominal(m)) {
				resolve(m, pronoun(m), d);
			}
		}
	}
	
	public static void resolve(Mention mention, String pronoun, Document d) {
		
		ArrayList<Mention> candidates = new ArrayList<Mention>();
		
		for (Mention cand : d.prevMentions(mention)) {
			boolean match;
			if (personhood(pronoun).equals("NONPER")) {    // e.g. "it"
				if (!isPronominal(cand)) {
					match = !cand.neType().equals("PERSON");
				} else {
					String g2 = gender(cand);
					System.out.println("gender "+g2+"  for  "+cand);
					if (g2.equals("M") || g2.equals("F")) {
						match = false;
					} else if (number(cand).equals("SG")) {
						match = true;
					} else { 
						match = true;  // ??  "it" -> "the store" i suppose.
					}					
				}
			} else if (personhood(pronoun).equals("PER")) {
				if (isPronominal(cand)) {
					match = gender(cand).equals(gender(mention));
				} else {
					// should use namelist here
					match = cand.neType().equals("PERSON");
				}
			} else {
				match = false;
			}			
			if (match) {
				System.out.println("yay    typematch: " + cand);
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
			System.out.println("Doing stupid surfaceish-closest resolution");
			d.getRefGraph().setRef(mention, SyntacticPaths.findBestCandidateByShortestPath(mention, candidates, d)); 
		}
		Mention ref = d.getRefGraph().getFinalResolutions().get(mention);
		System.out.printf("RESOLVE M%-3d -> M%-3d    %20s    ->   %-20s\n", 
				mention.getID(), ref.getID(), AnalysisUtilities.abbrevTree(mention.getNode()),
				 AnalysisUtilities.abbrevTree(ref.getNode()));
//		System.out.printf("RESOLVE M%-3d %s  ->  M%-3d %s\n", mention.id, d.refGraph.finalResolutions.get(mention));
	}


	
	// TODO (PRP$ Its)
	public static boolean isPronominal(Mention m) {
		TregexMatcher matcher = TregexPatternFactory.getPattern("NP <<# PRP").matcher(m.getNode());
		return matcher.find();
	}
	
	public static String pronoun(Mention m) {
		TregexPattern pat = TregexPatternFactory.getPattern("NP=np <<# PRP=pronoun");
		TregexMatcher matcher = pat.matcher(m.getNode());
		if (matcher.find()) {
			Tree PRP = matcher.getNode("pronoun");
			return pronoun(PRP);
		} else {
			return null;
		}
	}
	
	public static String pronoun(Tree PRP) {
		Tree c = PRP.getChild(0);
		assert c.isLeaf();
		String p = c.label().toString().toLowerCase();
		return p;
	}
	
	public static String gender(Mention m) {
		if (isPronominal(m)) {
			String p = pronoun(m);
			if (p.matches("^(he|him|his)$")) {
				return "M";
			} else if (p.matches("^(she|her|hers)$")) {
				return "F";
			} else if (p.matches("^(it|its)$")) {
				return "N";  // neuter
			} else {
				return null;   // no decision
			}
		}
		// else name lists, i guess
		return null;
	}
	public static String personhood(Mention m) {
		if (isPronominal(m)) {
			String p = pronoun(m);
			return personhood(p);
		}
		return null;
	}
	public static String personhood(String pronoun) {
		if (pronoun.matches("^(he|him|his|she|her|hers)$")) {
			return "PER";
		} else if (pronoun.matches("^(it|its)$")) {
			return "NONPER";
		}
		return null;
	}
	public static String number(Mention m) {
		if (isPronominal(m)) {
			String p = pronoun(m);
			if (p.matches("^(they|them|these|those|we|us)$")) {
				return "PL";
			} else {  //if (p.matches("^(it|its|that|this|he|him|his|she|her)$")) {
				return "SG";
			}
		}
		return null;
	}
}

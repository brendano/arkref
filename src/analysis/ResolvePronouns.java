package analysis;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import parsestuff.AnalysisUtilities;
import parsestuff.TregexPatternFactory;
import data.Document;
import data.Mention;
import data.Sentence;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.tregex.TregexMatcher;
import edu.stanford.nlp.trees.tregex.TregexPattern;

public class ResolvePronouns {
	public static void go(Document d) {
		System.out.println("\n***  Resolve Pronouns  ***\n");

		// todo (PRP$ Its)
		TregexPattern pat = TregexPatternFactory.getPattern("NP=np <<# PRP=pronoun");
		for (Sentence s : d.sentences) {
			TregexMatcher matcher = pat.matcher(s.root);
			while (matcher.find()) {
				Tree NP = matcher.getNode("np");
				Tree PRP= matcher.getNode("pronoun");
				String pronoun = pronoun(PRP);
//				Mention mention = d.node2mention.get(NP);
				Mention mention = d.node2mention(s,NP);
				System.out.println("\nResolving phrase "+mention);
				System.out.println("head? pronoun: "+pronoun);
				resolve(mention, pronoun, d);
			}
		}
	}
	
	public static void resolve(Mention mention, String pronoun, Document d) {
		
		ArrayList<Mention> candidates = new ArrayList<Mention>();
		
		for (Mention prev : d.prevMentions(mention)) {
			boolean match;
			String t = prev.neType();
			if (pronoun.equals("it")) { //&& !t.equals("PERSON") &&) {
				if (!isPronominal(prev)) {
					match = !prev.neType().equals("PERSON");
				} else {
					String g2 = gender(prev);
					System.out.println("gender "+g2+"  "+prev);
					if (g2.equals("M") || g2.equals("F")) {
						match = false;
					} else if (number(prev).equals("SG")) {
						match = true;
					} else { 
						match = true;  // ??  "it" -> "the store" i suppose.
					}					
				}
			} else if (personhood(pronoun).equals("PER")) {
				System.out.println(mention + "  |  " + prev);
				if (isPronominal(prev)) {
					match = gender(prev).equals(gender(mention));
				} else {
					// should use namelist here
					match = prev.neType().equals("PERSON");
				}
			} else {
				match = false;
			}
			
			if (match) {
				System.out.println("yay    typematch: " + prev);
				candidates.add(prev);
			} else {
				System.out.println("reject mismatch:  " + prev);
			}
		}
		if (candidates.size() == 0) {
			System.out.println("No legal candidates");
			d.refGraph.setNullRef(mention);			
		} else if (candidates.size() == 1) {
			System.out.println("Single legal resolution");
			d.refGraph.setRef(mention, candidates.get(0));
		} else if (candidates.size() > 1) {
			// want shortest path length and stuff
			System.out.println("Doing stupid surfaceish-closest resolution");
			d.refGraph.setRef(mention, candidates.get(0)); 
		}
		Mention ref = d.refGraph.finalResolutions.get(mention);
		System.out.printf("RESOLVE M%-3d -> M%-3d    %20s    ->   %-20s\n", 
				mention.id, ref.id, AnalysisUtilities.abbrevTree(mention.node),
				 AnalysisUtilities.abbrevTree(ref.node));
//		System.out.printf("RESOLVE M%-3d %s  ->  M%-3d %s\n", mention.id, d.refGraph.finalResolutions.get(mention));
	}
	
	public static boolean isPronominal(Mention m) {
		TregexMatcher matcher=TregexPatternFactory.getPattern("NP <<# PRP").matcher(m.node);
		return matcher.find();
	}
	public static String pronoun(Mention m) {
		TregexPattern pat = TregexPatternFactory.getPattern("NP=np <<# PRP=pronoun");
		TregexMatcher matcher = pat.matcher(m.node);
		if (matcher.find()) {
			Tree PRP= matcher.getNode("pronoun");
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
			} else if (p.matches("^(she|her)$")) {
				return "F";
			} else if (p.matches("^(it|its)$")) {
				return "N";  // neuter
			} else {
				return null;   // no decision
			}
		} else {
			// name lists
			return null;
		}
	}
	public static String personhood(Mention m) {
		if (isPronominal(m)) {
			String p = pronoun(m);
			return personhood(p);
		} else { return null; }
	}
	public static String personhood(String pronoun) {
		if (pronoun.matches("^(he|him|his|she|her)$")) {
			return "PER";
		} else {
			return null;
		}
		
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

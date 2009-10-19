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
				Tree c = PRP.getChild(0);
				assert c.isLeaf();
				String pronoun = c.label().toString().toLowerCase().replace("s$","");
				Mention mention = d.node2mention.get(NP);
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
			if (pronoun.equals("it") && !t.equals("PERSON")) {
				match = true;
			} else if (pronoun.matches("he|she|him|his|her|we|us|u|them|they") && t.equals("PERSON")) {
				match = true;
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
}

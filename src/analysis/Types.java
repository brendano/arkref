package analysis;

import parsestuff.TregexPatternFactory;
import data.Mention;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.tregex.TregexMatcher;
import edu.stanford.nlp.trees.tregex.TregexPattern;

public class Types {
	
	public static enum Gender {
		Male, Female, Neuter;
		public String toString() {
			switch(this) {
			case Male: return "Mal";
			case Female: return "Fem";
			case Neuter: return "Neu";
			}
			return null;
		}
	}
	public static enum Personhood {
		Person, NotPerson;
		public String toString() {
			switch(this) {
			case Person: return "Per";
			case NotPerson: return "NPer";
			}
			return null;
		}
	}
	public static enum Number {
		Singular, Plural;
		public String toString() {
			switch(this) {
			case Singular: return "Sg";
			case Plural: return "Pl";
			}
			return null;
		}
	}

	public static boolean checkPronominalMatch(Mention mention, Mention cand) {
		assert isPronominal(mention);
		String pronoun = pronoun(mention);
		if (SyntacticPaths.aIsDominatedByB(mention, cand)){ // I-within-I constraint 
			return false;
		}else if (personhood(pronoun) == Personhood.NotPerson) {    // e.g. "it"
			if (!isPronominal(cand)) {
				return !cand.neType().equals("PERSON");
			} else {
				Gender g2 = gender(cand);
				System.out.println("gender "+g2+"  for  "+cand);
				if (g2==Gender.Male || g2==Gender.Female) {
					return false;
				} else if (number(cand) == Number.Singular) {
					return true;
				} else { 
					return true;  // ??  "it" -> "the store" i suppose.
				}					
			}
		} else if (personhood(pronoun) == Personhood.Person) {
			if (isPronominal(cand)) {
				return gender(cand).equals(gender(mention));
			} else {
				// should use namelist here
				return cand.neType().equals("PERSON");
			}
		} else {
			return  false;
		}
	}
	public static boolean isPronominal(Mention m) {
		TregexMatcher matcher = TregexPatternFactory.getPattern("NP <<# /^PRP/ !> NP").matcher(m.getNode());
		return matcher.find();
	}
	
	public static String pronoun(Mention m) {
		TregexPattern pat = TregexPatternFactory.getPattern("NP=np <<# /^PRP/=pronoun !> NP");
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
	
	public static Gender gender(Mention m) {
		if (isPronominal(m)) {
			String p = pronoun(m);
			if (p.matches("^(he|him|his)$")) {
				return Gender.Male;
			} else if (p.matches("^(she|her|hers)$")) {
				return Gender.Female;
			} else if (p.matches("^(it|its)$")) {
				return Gender.Neuter;
			} else {
				return null;   // no decision
			}
		}
		// else name lists, i guess
		return null;
	}
	
	public static Personhood personhood(Mention m) {
		if (isPronominal(m)) {
			String p = pronoun(m);
			return personhood(p);
		}
		String t = m.neType();
		if (t.equals("PERSON")) return Personhood.Person;
		if (t.equals("ORGANIZATION")) return Personhood.NotPerson;
		if (t.equals("LOCATION")) return Personhood.NotPerson;
		return null;
	}
	
	public static Personhood personhood(String pronoun) {
		if (pronoun.matches("^(he|him|his|she|her|hers|our|ours|my|mine|you|yours|i|we)$")) {
			return Personhood.Person;
		} else if (pronoun.matches("^(it|its)$")) {
			return Personhood.NotPerson;
		}
		return null;
	}
	
	public static Number number(Mention m) {
		if (isPronominal(m)) {
			String p = pronoun(m);
			if (p.matches("^(they|them|these|those|we|us|their|ours|our|theirs)$")) {
				return Number.Plural;
			} else {  //if (p.matches("^(it|its|that|this|he|him|his|she|her)$")) {
				return Number.Singular;
			}
		}
		return null;
	}

}

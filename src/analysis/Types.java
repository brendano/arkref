package analysis;

import analysis.Types.Gender;
import parsestuff.AnalysisUtilities;
import parsestuff.TregexPatternFactory;
import data.FirstNames;
import data.Mention;
import edu.stanford.nlp.trees.HeadFinder;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.tregex.TregexMatcher;
import edu.stanford.nlp.trees.tregex.TregexPattern;

public class Types {

	// in all cases, null indicates unknown; that is, our system does not know.
	
	public static enum Gender {
		Male, Female;
		public String toString() {
			switch(this) {
			case Male: return "Mal";
			case Female: return "Fem";
			}
			return null;
		}
	}
	public static enum Personhood {
		Person, NotPerson, MaybePerson;
		public String toString() {
			switch(this) {
			case Person: return "Per";
			case NotPerson: return "NPer";
			case MaybePerson: return "MaybePer";
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
	public static enum Perspective {
		First, Second, Third;
		public String toString() {
			switch(this) {
			case First: return "1";
			case Second: return "2";
			case Third: return "3";
			default: return null;
			}
		}
	}
	
	public static <T> boolean relaxedEquals(T x, T y) {
		if (x==null || y==null)
			return true;
		return x==y;
	}
	
	
	public static boolean sexistGenderEquals(Gender x, Gender y) {
		// see testDefaultMale()
		// unknown gender defaults to male
		// unknown gender cannot match female
		if (x==null && y==null) return true;
		if (x==null && y==Gender.Male) return true;
		if (y==null && x==Gender.Male) return true;
		return x==y;
	}
	
	
	public static boolean personhoodEquals(Personhood x, Personhood y) {
		// see testEntityTypeMatching(), testThey()
		if(	(x==null || x==Personhood.NotPerson || x==Personhood.MaybePerson) && 
			(y==null || y==Personhood.NotPerson || y==Personhood.MaybePerson))
		{
			return true;
		}
		if(	(x==Personhood.Person || x==Personhood.MaybePerson) && 
			(y==Personhood.Person || y==Personhood.MaybePerson))
		{
			return true;
		}
		return x==y;
	}


	public static boolean checkPronominalMatch(Mention mention, Mention cand) {
		assert isPronominal(mention);
		String pronoun = pronoun(mention);
		if (!isPronominal(cand) && perspective(pronoun) == Perspective.First) {
			// testFirstPerson
			return false;
		}
		
		// this hurts recall a good bit (!)
		// if (isPronominal(cand) && perspective(pronoun) != perspective(cand))
		//	return false;
		
		// using lax test on personhood because i don't know how to get it for most common nouns
		// number is easiest to get
		// gender is gray area
		return
			personhoodEquals(personhood(pronoun), personhood(cand)) &&
			sexistGenderEquals(gender(mention), gender(cand)) &&
			relaxedEquals(number(mention), number(cand)) && // "they" should be able to match singular nouns for groups
			true;
	}
	
	public static boolean isReflexive(Mention m) {
		return m.getHeadWord().matches("^(itself|yourself|myself|himself|herself|themselves|ourselves)$");
	}

	public static boolean isPronominal(Mention m) {
		if (m.node()==null) return false;
		TregexMatcher matcher = TregexPatternFactory.getPattern("NP <<# /^PRP/ !>> NP").matcher(m.node());
		return matcher.find();
	}
	
	public static String pronoun(Mention m) {
		TregexPattern pat = TregexPatternFactory.getPattern("NP=np <<# /^PRP/=pronoun !>> NP");
		TregexMatcher matcher = pat.matcher(m.node());
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
		if (m.node()==null) return null;
		if (isPronominal(m)) {
			String p = pronoun(m);
			if (p.matches("^(he|him|his|himself)$")) {
				return Gender.Male;
			} else if (p.matches("^(she|her|hers|herself)$")) {
				return Gender.Female;
			} else if (p.matches("^(it|its|itself)$")) {
				return null;
//				return Gender.Neuter;
			} else {
				return null;   // no decision
			}
		}
		
		//if its something other than PERSON or other (e.g., LOCATION)
		//then return null because its obviously not male or female.
		String neType = m.neType();
		if(!neType.equals("PERSON") && !neType.equals("O")){
			return null;
		}
		
		Gender firstNameGender = genderByFirstNames(m);
		
		return firstNameGender;
	}


	private static Gender genderByFirstNames(Mention m) {
		if (m.node()==null) return null;  // TODO we can still figure something out, right
		
		//Go through all the NNP tokens in the noun phrase and see if any of them
		//are person names.  If so, return the gender of that name.
		//Note: this will fail for ambiguous month/person names like "April"
		
		Tree head = m.node().headPreTerminal(AnalysisUtilities.getInstance().getHeadFinder());
		Tree root = m.getSentence().rootNode();
		
		for(Tree leaf : m.node().getLeaves()){
			//System.err.println(head+"\t"+leaf+"\t"+head.parent(root)+"\t"+leaf.parent(root));
			if(!leaf.parent(m.node()).label().value().equals("NNP") 
					|| leaf.parent(root).parent(root) != head.parent(root)) //must be a sibling of the head node, as in "(NP (NNP John) (POS 's))"
			{
				continue;
			}
			String genderS = FirstNames.getInstance().getGenderString(leaf.value());
			if(genderS.equals("Mal")){
				return Gender.Male;
			}else if(genderS.equals("Fem")){
				return Gender.Female;
			}
		}
		
		return null;
	}
	
	
	public static Personhood personhood(Mention m) {
		if (isPronominal(m)) {
			String p = pronoun(m);
			return personhood(p);
		}
		String t = m.neType();
		if (t.equals("PERSON") 
			 || NounTypes.getInstance().getType(m.getHeadWord()).equals("person")
			 || genderByFirstNames(m) != null)
			return Personhood.Person;
		if (t.equals("O")) 
			return null;
		return Personhood.NotPerson;
	}
	
	public static Personhood personhood(String pronoun) {
		if (pronoun.matches("^(me|he|him|his|she|her|hers|we|us|our|ours|i|my|mine|you|yours|himself|herself|ourselves|myself)$")) {
			return Personhood.Person;
		} else if (pronoun.matches("^(it|its|itself)$")) {
			return Personhood.NotPerson;
		}else if (pronoun.matches("^(they|their|theirs|them|these|those|themselves)$")) {
			return Personhood.MaybePerson;
		}
		return null;
	}
	
	/** what the heck is the real name for this? at least it is nice and reliably deterministic **/
	public static Perspective perspective(String pronoun) {
		if (pronoun.matches("^(i|me||my|mine|we|our|ours|ourselves|myself)$")) {
			return Perspective.First;
		} else if (pronoun.matches("^(you|yours|y'all|y'alls|yinz|yourself)$")) {
			return Perspective.Second;
		} else {
			return Perspective.Third;
		}
	}
	public static Perspective perspective(Mention mention) {
		assert isPronominal(mention);
		return perspective(pronoun(mention));
	}

	
	public static Number number(Mention m) {
		if (m.node()==null) return null;
		TregexPattern pat = TregexPatternFactory.getPattern("NP < CC|CONJP !>> NP");
		TregexMatcher matcher = pat.matcher(m.node());
		if(matcher.find()) {
			return Number.Plural;
		}
		
		
		if (isPronominal(m)) {
			String p = pronoun(m);
			if (p.matches("^(they|them|these|those|we|us|their|ours|our|theirs|themselves|ourselves)$")) {
				return Number.Plural;
			} else {  //if (p.matches("^(it|its|that|this|he|him|his|she|her)$")) {
				return Number.Singular;
			}
		} else {
			HeadFinder hf = AnalysisUtilities.getInstance().getHeadFinder();
			Tree head = m.node().headPreTerminal(hf);
			String tag = head.label().toString();
			// http://bulba.sdsu.edu/jeanette/thesis/PennTags.html
			//String headWord = head.getChild(0).label().value();
			//if (NounTypes.getInstance().getType(headWord).equals("organization")
			//		|| NounTypes.getInstance().getType(headWord).equals("group")
			//		|| m.neType().equals("ORGANIZATION")) return null;
			if (tag.matches("^NNP?S$")) return Number.Plural;
			if (tag.matches("^NNP?$"))  return Number.Singular;
			// TODO mass nouns?
		}
		return null;
	}


	public static boolean isPossessive(Mention mention) {
		return mention.getHeadWord().matches("^(its|his|her|their|our|my)$");
	}

}

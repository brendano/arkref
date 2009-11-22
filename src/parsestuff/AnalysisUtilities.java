package parsestuff;


import java.io.*;
import java.net.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;


//import net.didion.jwnl.data.POS;
//import net.didion.jwnl.dictionary.Dictionary;

import edu.stanford.nlp.ie.AbstractSequenceClassifier;
import edu.stanford.nlp.ie.crf.CRFClassifier;
import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.ling.Label;
import edu.stanford.nlp.parser.lexparser.*;
import edu.stanford.nlp.process.DocumentPreprocessor;
import edu.stanford.nlp.trees.*;
import edu.stanford.nlp.trees.tregex.TregexMatcher;
import edu.stanford.nlp.trees.tregex.TregexPattern;
import edu.stanford.nlp.trees.tregex.tsurgeon.Tsurgeon;
import edu.stanford.nlp.trees.tregex.tsurgeon.TsurgeonPattern;
import edu.stanford.nlp.util.Pair;

//import net.didion.jwnl.*;


public class AnalysisUtilities {
	public static boolean DEBUG = true;
	private AnalysisUtilities(){
		parser = null;
		ner = null;
		dp = new DocumentPreprocessor(false);
		
		properties = new Properties();
		try{
			properties.load(new FileInputStream("config/arkref.properties"));
		}catch(Exception e){
			e.printStackTrace();
			System.exit(0);
		}
		
//		try{
//			JWNL.initialize(new FileInputStream(properties.getProperty("jwnlPropertiesFile", "config/file_properties.xml")));
//		}catch(Exception e){
//			e.printStackTrace();
//		}
//		
//		conjugator = new VerbConjugator();
//		conjugator.load(properties.getProperty("verbConjugationsFile", "verbConjugations.txt"));
		headfinder = new CollinsHeadFinder();
		tree_factory = new LabeledScoredTreeFactory();
		tlp = new PennTreebankLanguagePack();
	}
	
	
	protected static String preprocess(String sentence) {
		sentence = sentence.trim();
		if(!sentence.matches(".*\\.['\"]*$")){//charAt(sentence.length()-1) != '.'){
			sentence += ".";
		}
		
		sentence = sentence.replaceAll("can't", "can not");
		sentence = sentence.replaceAll("won't", "will not");
		sentence = sentence.replaceAll("n't", " not"); //aren't shouldn't don't isn't
		
		return sentence;
	}
	
	
	protected static String preprocessTreeString(String sentence) {
		sentence = sentence.replaceAll(" n't", " not");
		sentence = sentence.replaceAll("\\(MD ca\\)", "(MD can)");
		sentence = sentence.replaceAll("\\(MD wo\\)", "(MD will)");
		sentence = sentence.replaceAll("\\(MD 'd\\)", "(MD would)");
		sentence = sentence.replaceAll("\\(VBD 'd\\)", "(VBD had)");
		sentence = sentence.replaceAll("\\(VBZ 's\\)", "(VBZ is)");
		sentence = sentence.replaceAll("\\(VBZ 's\\)", "(VBZ is)");
		sentence = sentence.replaceAll("\\(VBZ 's\\)", "(VBZ is)");
		sentence = sentence.replaceAll("\\(VBP 're\\)", "(VBP are)");
		
		return sentence;
	}
	
	
	public static int[] alignTokens(String rawText, Tree parse) {
		List<Tree> leaves = parse.getLeaves();
		
		return alignTokens(rawText, leaves);
	}
	public static int[] alignTokens(String rawText, List<Tree> leaves) {
		int MAX_ALIGNMENT_SKIP = 100;
		int[] alignments = new int[leaves.size()];
		int curPos = 0;
		tok_loop:
		for (int i=0; i < leaves.size(); i++) {
			String tok = leaves.get(i).value();
			U.pf("TOKEN [%s]  :  ", tok);
			for (int j=0; j < MAX_ALIGNMENT_SKIP; j++) {
				boolean directMatch  = rawText.regionMatches(curPos + j, tok, 0, tok.length());
				
				
				String substr = rawText.substring(curPos+j, curPos+j+tok.length()*2+10);
				Matcher m=tokenSurfaceMatches(tok).matcher(substr);
//				U.pl("PATTERN "+ tokenSurfaceMatches(tok));
				boolean alternateMatch = m.find() && m.start()==0;
				
//				U.pl("MATCHES "+ directMatch + " " + alternateMatch);
				if (directMatch || alternateMatch) {
					alignments[i] = curPos+j;
					if (directMatch)
						curPos = curPos+j+tok.length();
					else
						curPos = curPos+j+1;
					U.pf("\n  Aligned to %d\n", alignments[i]);
					continue tok_loop;
				}
				U.pf("%s", U.backslashEscape(rawText.substring(curPos+j,curPos+j+1)));
			}
			U.pf("  FAILED MATCH for token [%s]\n", tok);
			alignments[i] = -1;
		}
		// TODO backoff for gaps .. at least guess the 2nd gap position or something (2nd char after previous token ends...)
		return alignments;
	}
	
	/** undo penn-treebankification of tokens.  want the raw original form if possible. **/
	public static Pattern tokenSurfaceMatches(String tok) {
		if (tok.equals("-LRB-")) {
			return Pattern.compile("[(\\[]");
		} else if (tok.equals("-RRB-")) {
			return Pattern.compile("[)\\]]");
		} else if (tok.equals("``")) {
			return Pattern.compile("(\"|``)");
		} else if (tok.equals("''")) {
			return Pattern.compile("(\"|'')");
		}
		return Pattern.compile(Pattern.quote(tok));
	}
	

	
	public List<String> getSentences(String document) {
		List<String> res = new ArrayList<String>();
		String sentence;
		StringReader reader = new StringReader(cleanupDocument(document));
		
		List<List<? extends HasWord>> sentences = new ArrayList<List<? extends HasWord>>();
		Iterator<List<? extends HasWord>> iter1 ;
		Iterator<? extends HasWord> iter2;
		
		try{
			sentences = dp.getSentencesFromText(reader);
		}catch(Exception e){
			e.printStackTrace();
		}
		
		iter1 = sentences.iterator();
		while(iter1.hasNext()){
			iter2 = iter1.next().iterator();
			sentence = "";
			while(iter2.hasNext()){
				String tmp = iter2.next().word().toString();
				sentence += tmp;
				if(iter2.hasNext()){
					sentence += " ";
				}
			}
			res.add(sentence);
		}
		
		return res;
	}
	
	/** some ACE docs have weird markup in them that serve as paragraph-ish markers **/
	public static String cleanupDocument(String document) {
		document = document.replaceAll("<\\S+>", "\n");
		return document;
	}
	
	
//	public VerbConjugator getConjugator(){
//		return conjugator;
//	}
	
	
	public CollinsHeadFinder getHeadFinder(){
		return headfinder;
	}
	
	
	public static AnalysisUtilities getInstance(){
		if(instance == null){
			instance = new AnalysisUtilities();
		}
		return instance;
	}
	
	public double getLastParseScore(){
		return lastParseScore;
	}
	
	public Double getLastParseScoreNormalizedByLength() {
		double length = lastParse.yield().length();
		double res = lastParseScore;
		if(length <= 0){
			res = 0.0;
		}else{
			res /= length;
		}
		return res;
	}
	
	public Tree parseSentence(String sentence) {
		String result = "";
		
		//see if a parser socket server is available
        int port = new Integer(properties.getProperty("parserServerPort","5556"));
        String host = "127.0.0.1";
        Socket client;
        PrintWriter pw;
        BufferedReader br;
        String line;
		try{
			client = new Socket(host, port);

			pw = new PrintWriter(client.getOutputStream());
			br = new BufferedReader(new InputStreamReader(client.getInputStream()));
			pw.println(sentence);
			pw.flush(); //flush to complete the transmission
            while((line = br.readLine())!= null){
                //if(!line.matches(".*\\S.*")){
                //        System.out.println();
                //}
                if(br.ready()){
                	line = line.replaceAll("\n", "");
                    line = line.replaceAll("\\s+", " ");
                	result += line + " ";
                }else{
                	lastParseScore = new Double(line);
                }
            }

			br.close();
			pw.close();
			client.close();
			
			System.err.println("parser output:"+ result);
			
			lastParse = readTreeFromString(result);
			return lastParse;
		} catch (Exception ex) {
			if(DEBUG) System.err.println("Could not connect to parser server.");
			//ex.printStackTrace();
		}
        
		//if socket server not available, then use a local parser object
		if(parser == null){
			try {
				Options op = new Options();
				String serializedInputFileOrUrl = properties.getProperty("parserGrammarFile", "lib/englishPCFG.ser.gz");
				parser = new LexicalizedParser(serializedInputFileOrUrl, op);
				int maxLength = new Integer(properties.getProperty("parserMaxLength", "40")).intValue();
				parser.setMaxLength(maxLength);
				parser.setOptionFlags("-outputFormat", "oneline");
			} catch (Exception e){
				e.printStackTrace();
			}
		}

		try{
			if(parser.parse(sentence)){
				lastParse = parser.getBestParse();
				lastParseScore = parser.getPCFGScore();
				return lastParse;
			}
		}catch(Exception e){
		}

		lastParse = readTreeFromString("(ROOT (. .))");
                lastParseScore = -99999.0;
                return lastParse;
	}
	
//	@SuppressWarnings("unchecked")
//	public String getLemma(Tree tensedverb){
//		if(tensedverb == null){
//			return "";
//		}
//		
//		String res = "";
//		Pattern p = Pattern.compile("\\(\\S+ ([^\\)]*)\\)");
//		Matcher m = p.matcher(tensedverb.toString());
//		m.find();
//		res = m.group(1);
//		
//		if(res.equals("is") || res.equals("are") || res.equals("were") || res.equals("was")){
//			res = "be";
//		}else{
//			try{
//				Iterator<String> iter = Dictionary.getInstance().getMorphologicalProcessor().lookupAllBaseForms(POS.VERB, res).iterator();
//				
//				int maxCount = -1;
//				int tmpCount;
//				while(iter.hasNext()){
//					String lemma = iter.next();
//					tmpCount = conjugator.getBaseFormCount(lemma);
//					//System.err.println("lemma: "+lemma + "\tcount: "+tmpCount);
//					if(tmpCount > maxCount){
//						res = lemma;
//						maxCount = tmpCount;
//					}
//				}
//			}catch(Exception e){
//				e.printStackTrace();
//			}
//		}		
//		
//		return res;
//	}
	

	public String annotateSentenceNER(String sentence) {
		String result = "";
		
		//see if a NER socket server is available
        int port = new Integer(properties.getProperty("nerServerPort","5555"));
        String host = "127.0.0.1";
        Socket client;
        PrintWriter pw;
        BufferedReader br;
        String line;
		try{
			client = new Socket(host, port);

			pw = new PrintWriter(client.getOutputStream());
			br = new BufferedReader(new InputStreamReader(client.getInputStream()));
			pw.println(sentence);
			pw.flush(); //flush to complete the transmission

			while((line = br.readLine())!= null){
				if(result.length()>0){
					result += "\n";
				}
				result += line;
			}
			br.close();
			pw.close();
			client.close();
			
		} catch (Exception ex) {
			if(DEBUG) System.err.println("Could not connect to NER server.");
			//ex.printStackTrace();
		}
		
		//if socket server not available, then use a local NER object
		if(result.length() == 0){
			if(ner == null){
				try {
					ner = CRFClassifier.getClassifierNoExceptions(properties.getProperty("nerModelFile", "lib/ner-eng-ie.crf-muc7.ser.gz"));
				} catch (Exception e){
					e.printStackTrace();
				}
			}
			result = ner.testString(sentence);
		}
		
		result = result.trim();
		if(DEBUG) System.err.println("annotateSentenceNER: "+result);
		return result;
	}
	
	
	/**
	 * Remove traces and non-terminal decorations (e.g., "-SUBJ" in "NP-SUBJ") from a Penn Treebank-style tree.
	 * 
	 * @param inputTree
	 */
	public void normalizeTree(Tree inputTree){
		inputTree.label().setFromString("ROOT");

		List<Pair<TregexPattern, TsurgeonPattern>> ops = new ArrayList<Pair<TregexPattern, TsurgeonPattern>>();
		List<TsurgeonPattern> ps = new ArrayList<TsurgeonPattern>();
		String tregexOpStr;
		TregexPattern matchPattern;
		TsurgeonPattern p;
		TregexMatcher matcher;
		
		tregexOpStr = "/\\-NONE\\-/=emptynode";
		matchPattern = TregexPatternFactory.getPattern(tregexOpStr);
		matcher = matchPattern.matcher(inputTree);
		ps.add(Tsurgeon.parseOperation("prune emptynode"));
		matchPattern = TregexPatternFactory.getPattern(tregexOpStr);
		p = Tsurgeon.collectOperations(ps);
		ops.add(new Pair<TregexPattern,TsurgeonPattern>(matchPattern,p));
		Tsurgeon.processPatternsOnTree(ops, inputTree);
		
		Label nonterminalLabel;
		
		tregexOpStr = "/.+\\-.+/=nonterminal < __";
		matchPattern = TregexPatternFactory.getPattern(tregexOpStr);
		matcher = matchPattern.matcher(inputTree);
		while(matcher.find()){
			nonterminalLabel = matcher.getNode("nonterminal");
			if(nonterminalLabel == null) continue;
			nonterminalLabel.setFromString(tlp.basicCategory(nonterminalLabel.value()));
		}
		

	}
	
	

	
	public static String getCleanedUpYield(Tree inputTree){
		Tree copyTree = inputTree.deeperCopy();

		if(DEBUG)System.err.println(copyTree.toString());

		String res = copyTree.yield().toString();
		if(res.length() > 1){
			res = res.substring(0,1).toUpperCase() + res.substring(1);
		}

		//(ROOT (S (NP (NNP Jaguar) (NNS shares)) (VP (VBD skyrocketed) (NP (NN yesterday)) (PP (IN after) (NP (NP (NNP Mr.) (NNP Ridley) (POS 's)) (NN announcement)))) (. .)))
		
		res = res.replaceAll("\\s([\\.,!\\?\\-;:])", "$1");
		res = res.replaceAll("(\\$)\\s", "$1");
		res = res.replaceAll("can not", "cannot");
		res = res.replaceAll("\\s*-LRB-\\s*", " (");
		res = res.replaceAll("\\s*-RRB-\\s*", ") ");
		res = res.replaceAll("\\s*([\\.,?!])\\s*", "$1 ");
		res = res.replaceAll("\\s+''", "''");
		//res = res.replaceAll("\"", "");
		res = res.replaceAll("``\\s+", "``");
		res = res.replaceAll("\\-[LR]CB\\-", ""); //brackets, e.g., [sic]

		//remove extra spaces
		res = res.replaceAll("\\s\\s+", " ");
		res = res.trim();

		return res;
	}
	
	
	public Tree readTreeFromString(String parseStr){
		//read in the input into a Tree data structure
		TreeReader treeReader = new PennTreeReader(new StringReader(parseStr), tree_factory);
		Tree inputTree = null;
		try{
			inputTree = treeReader.readTree();
			
		}catch(IOException e){
			e.printStackTrace();
		}
		return inputTree;
	}
	
	protected static boolean filterSentenceByPunctuation(String sentence) {
		//return (sentence.indexOf("\"") != -1 
				//|| sentence.indexOf("''") != -1 
				//|| sentence.indexOf("``") != -1
				//|| sentence.indexOf("*") != -1);
				return (sentence.indexOf("*") != -1);
	}
	
	
	/**
	 * Sets the parse and score.
	 * For use when the input tree is given (e.g., for gold standard trees from a treebank)
	 * 
	 * @param parse
	 * @param score
	 */
	public void setLastParseAndScore(Tree parse, double score){
		lastParse = parse;
		lastParseScore = score;
	}
	
	public static String abbrevTree(Tree tree) {
		ArrayList<String> toks = new ArrayList();
		for (Tree L : tree.getLeaves()) {
			toks.add(L.label().toString());
		}
		return tree.label().toString() + "[" + StringUtils.join(toks, " ") + "]";
	}
	
	
	private AbstractSequenceClassifier ner; // stanford CRF classifier for NER
	private LexicalizedParser parser;
	private static AnalysisUtilities instance;
	private Properties properties;
//	private VerbConjugator conjugator;
	private CollinsHeadFinder headfinder;
	private LabeledScoredTreeFactory tree_factory;
	private PennTreebankLanguagePack tlp;
	private double lastParseScore;
	private Tree lastParse;
	public DocumentPreprocessor dp;
}

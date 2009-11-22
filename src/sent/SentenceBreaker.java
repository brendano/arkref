package sent;

import com.aliasi.sentences.SentenceModel;

import com.aliasi.tokenizer.TokenizerFactory;
import com.aliasi.tokenizer.Tokenizer;

import com.aliasi.util.Arrays;
import com.aliasi.util.Files;
import com.aliasi.util.Strings;

import edu.stanford.nlp.util.StringUtils;

import java.io.File;
import java.io.IOException;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.ArrayUtils;

import parsestuff.AnalysisUtilities;
import parsestuff.U;


/**
 * Breaks a document into sentences in a very re-traceable manner, using LingPipe's sentence breaker.
 * @author brendano
 */
public class SentenceBreaker {

	
    static final TokenizerFactory TOKENIZER_FACTORY = MyIndoEuropeanTokenizerFactory.INSTANCE;

    public static class Sentence {
    	public int charStart;
    	public int charEnd;
    	public String rawText;
    	public String cleanText;
    	public List<String> tokens;
    }
    
    public static List<Sentence> getSentences(String text) {
    	ArrayList<Sentence> sentences = new ArrayList<Sentence>();
    	
		List<String> tokenList = new ArrayList<String>();
		List<String> whiteList = new ArrayList<String>();
		Tokenizer tokenizer = TOKENIZER_FACTORY.tokenizer(text.toCharArray(),0,text.length());
		tokenizer.tokenize(tokenList,whiteList);
		
	    SentenceModel SENTENCE_MODEL  = new MyIndoEuropeanSentenceModel(usesCapitalConvention(tokenList));
		

		String[] tokens = new String[tokenList.size()];
		String[] whites = new String[whiteList.size()];
		tokenList.toArray(tokens);
		whiteList.toArray(whites);
		int[] sentenceBoundaries = SENTENCE_MODEL.boundaryIndices(tokens,whites);		
	
		if (sentenceBoundaries.length < 1) {
		    return sentences;
		}
		if (sentenceBoundaries[sentenceBoundaries.length-1] < tokens.length - 1) {
			sentenceBoundaries = ArrayUtils.add(sentenceBoundaries, tokens.length-1);
		}

		int charStart = 0;
		int charEnd = 0;
		int sentStartTok = 0;
		int sentEndTok = 0;
		for (int i = 0; i < sentenceBoundaries.length; ++i) {
			sentEndTok = sentenceBoundaries[i];
			List<String> sentToks = new ArrayList<String>();
		    for (int j=sentStartTok; j<=sentEndTok; j++) {
		    	charEnd += tokens[j].length() + whites[j+1].length();
		    	sentToks.add(tokens[j]);
		    }
		    Sentence s = new Sentence();
		    s.charStart = charStart;
		    s.charEnd = charEnd;
		    s.rawText = text.substring(charStart, charEnd);
		    s.cleanText = Strings.normalizeWhitespace(s.rawText);
		    s.tokens = sentToks;
		    sentences.add(s);
		    
		    sentStartTok = sentEndTok+1;
		    charStart = charEnd;
		}
//		Sentence last = sentences.get(sentences.size()-1);
//		String rest = text.substring(last.charEnd, text.length());
		// should add degenerate last "sentence" ... if missing punctuation, it might be a real sentence
		return sentences;
    }
    
    
    /**
     * see notes/cap_ratio_experiment
     */
    public static boolean usesCapitalConvention(List<String> documentTokens) {
    	double numCap = 0.1;
    	double numPunct = 0.1;
    	for (String tok : documentTokens) {
    		if (Strings.allPunctuation(tok))  numPunct++;
    		if (Strings.capitalized(tok.toCharArray())) numCap++;
    	}
//    	U.pf("CAPRATIO %f\n", numCap/numPunct);
    	return (numCap / numPunct) > 0.3;
    }
    
    public static void main(String[] args) throws IOException {
    	for (String arg : args) {
    		if (args.length > 1)
    			U.pf("DOCUMENT\t%s\n", arg);
    		String text = U.readFile(arg);
    		text = AnalysisUtilities.cleanupDocument(text);
    		for (Sentence s : getSentences(text)) {
    			// rawText might have newlines, tabs
//    			System.out.printf("%d\t%d\t%s\n", s.charStart, s.charEnd, s.cleanText);
    			U.pl(StringUtils.join(s.tokens));
    		}
    		
    	}
    }
}

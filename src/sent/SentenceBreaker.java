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

import parsestuff.U;


/**
 * Breaks a document into sentences in a very re-traceable manner, using LingPipe's sentence breaker.
 * @author brendano
 */
public class SentenceBreaker {

	
    static final TokenizerFactory TOKENIZER_FACTORY = MyIndoEuropeanTokenizerFactory.INSTANCE;
    static final SentenceModel SENTENCE_MODEL  = new MyIndoEuropeanSentenceModel();

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

		String[] tokens = new String[tokenList.size()];
		String[] whites = new String[whiteList.size()];
		tokenList.toArray(tokens);
		whiteList.toArray(whites);
		int[] sentenceBoundaries = SENTENCE_MODEL.boundaryIndices(tokens,whites);		
	
		if (sentenceBoundaries.length < 1) {
		    return sentences;
		}
		int charStart = 0;
		int charEnd = 0;
		int sentStartTok = 0;
		int sentEndTok = 0;
		for (int i = 0; i < sentenceBoundaries.length; ++i) {
			sentEndTok = sentenceBoundaries[i];
			List<String> sentToks = new ArrayList();
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
		// should add degenerate last "sentence" ... if missing punctuation, it might be a real sentence
		return sentences;
    }
    
    
    public static void main(String[] args) throws IOException {
		File file = new File(args[0]);
		String text = Files.readFromFile(file,"UTF-8");
		
		for (Sentence s : getSentences(text)) {
			// rawText might have newlines, tabs
//			System.out.printf("%d\t%d\t%s\n", s.charStart, s.charEnd, s.cleanText);
			U.pl(StringUtils.join(s.tokens));
		}
    }
}

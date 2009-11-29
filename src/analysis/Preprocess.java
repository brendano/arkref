package analysis;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import parsestuff.AnalysisUtilities;
import parsestuff.U;
import sent.SentenceBreaker;
import edu.stanford.nlp.trees.Tree;

public class Preprocess {
	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		if(args.length == 0){
			System.err.println("You need to pass a text file as a command line argument.");
			System.exit(0);
		}
		String txtfile = args[0];
		Preprocess.go(txtfile);
	}

	public static boolean alreadyPreprocessed(String path) {
		String shortpath = shortPath(path);
		return 
			new File(shortpath+".ner").exists() &&
			new File(shortpath+".parse").exists();
	}

	public static String shortPath(String path) {
		return path.replace(".txt","").replace(".sent","");
	}
	
	public static void go(String path) throws IOException {
		go(path, false);
	}
	
	public static void writeOffsetSentenceFile(List <SentenceBreaker.Sentence> sentences, String shortpath, boolean useTempFiles) throws FileNotFoundException {
		File osentOutputFile = new File(shortpath + ".osent");
		if (useTempFiles) osentOutputFile.deleteOnExit();
		PrintWriter pwOSent = new PrintWriter(new FileOutputStream(osentOutputFile));

		for (SentenceBreaker.Sentence s : sentences) {
			pwOSent.printf("%d\t%d\t%s\n", s.charStart, s.charEnd, s.cleanText);
		}
		pwOSent.close();
	}
	public static void go(String path, boolean useTempFiles) throws IOException {
//		assert path.endsWith(".txt") || path.endsWith(".sent") : "bad filename extension";
		
		File parseOutputFile = new File(path+".parse");
		File nerOutputFile = new File(path+".ner");
		
		if (useTempFiles && !parseOutputFile.exists() && !nerOutputFile.exists()) {
			parseOutputFile.deleteOnExit();
			nerOutputFile.deleteOnExit();
		}
		
		PrintWriter pwParse = new PrintWriter(new FileOutputStream(parseOutputFile));
		PrintWriter pwNER = new PrintWriter(new FileOutputStream(nerOutputFile));
		
		String textpath;
		if (new File( (textpath=  path+".sent")).exists()) {
		} else if(new File(textpath= path+".txt").exists()) {
		} else { assert false : "need a sentence or text file"; }
		String text = U.readFile(textpath);
		String[] sentenceTexts = null;
		
		if (textpath.endsWith(".sent")) {
			sentenceTexts = text.split("\n");
		
		} else if (textpath.endsWith(".txt")) {
			List<SentenceBreaker.Sentence> sentences = AnalysisUtilities.cleanAndBreakSentences(text);
			writeOffsetSentenceFile(sentences, path, useTempFiles);
			
			sentenceTexts = new String[sentences.size()];
			for(int i=0; i < sentences.size(); i++) {
				sentenceTexts[i] = sentences.get(i).cleanText;
			}
		} else { assert false; }
		
		for(String sentence : sentenceTexts) {
			String ner = AnalysisUtilities.getInstance().annotateSentenceNER(sentence);
			AnalysisUtilities.ParseResult res = AnalysisUtilities.getInstance().parseSentence(sentence);
			U.pf("%s\t%s\t%s\n", res.success ? "PARSE" : "ERROR", res.score, res.parse);
			pwParse.printf("%s\t%s\t%s\n", res.success ? "PARSE" : "ERROR", res.score, res.parse);
			pwNER.println(ner);
		}
		pwNER.close();
		pwParse.close();
	}
}

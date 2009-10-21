package analysis;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.List;

import edu.stanford.nlp.trees.Tree;

import parsestuff.AnalysisUtilities;

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
		if (new File(path+".ner").exists() 
				&& new File(path+".parse").exists())
		{
			return true;
		}
		return false;
	}

	public static void go(String path) throws IOException {
		String shortpath = path;
		shortpath.replace(".txt", "");
		shortpath.replace(".sent", "");
		
		PrintWriter pwParse = new PrintWriter(new FileOutputStream(new File(shortpath+".parse")));
		PrintWriter pwNER = new PrintWriter(new FileOutputStream(new File(shortpath+".ner")));
		
		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(path)));
		String buf;
		String doc = "";
		while((buf=br.readLine()) != null){
			doc += buf + " ";
		}
		
		
		//split sentences
		List<String> sentences = AnalysisUtilities.getInstance().getSentences(doc);
		
		for(String sentence:sentences){
		
			String ner = AnalysisUtilities.getInstance().annotateSentenceNER(sentence);
			Tree parse = AnalysisUtilities.getInstance().parseSentence(sentence);
			
			
			//write output
			pwParse.println(parse);
			pwNER.println(ner);
		}
		
		pwNER.close();
		pwParse.close();		
	}

}

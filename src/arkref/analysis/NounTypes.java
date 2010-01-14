package arkref.analysis;

import java.util.*;
import java.io.*;

import arkref.parsestuff.AnalysisUtilities;


import edu.stanford.nlp.trees.Tree;

public class NounTypes {
	private NounTypes(){
		wordTypeMap = new HashMap<String, String>();
		
		Properties properties = new Properties();
		try{
			properties.load(new FileInputStream("config/arkref.properties"));
		}catch(Exception e){
			e.printStackTrace();
			System.exit(0);
		}
		
		String personListPath= properties.getProperty("person-hyponyms", "config/person-hyponyms.txt");
		String orgListPath= properties.getProperty("organization-hyponyms", "config/social-group-hyponyms.txt");
		String locListPath= properties.getProperty("location-hyponyms", "config/location-structure-facility-and-geological-formation-hyponyms.txt");
		String timeListPath = properties.getProperty("time-hyponyms", "config/time-point-unit-and-period-hyponyms.txt");
		String groupListPath = properties.getProperty("group-hyponyms", "config/single-word-group-hyponyms.txt");
		
		
		loadTypes("person", personListPath);
		loadTypes("group", groupListPath); //note: all organizations will also be groups
		loadTypes("organization", orgListPath);
		loadTypes("location", locListPath);
		loadTypes("time", timeListPath);
	}
	
	
	
	/**
	 * expects a path to a list of words, one per line
	 * 
	 * @param string
	 * @param path
	 */
	private void loadTypes(String label, String path) {
		try{
			String buf;
			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(path)));
			String lemma;
			while((buf = br.readLine()) != null){
				lemma = buf.toLowerCase();
				wordTypeMap.put(lemma, label.intern());
				wordTypeMap.put(lemma+"s", label.intern());
				wordTypeMap.put(lemma+"es", label.intern());
				if(lemma.charAt(lemma.length()-1) == 'y'){
					wordTypeMap.put(lemma.substring(0,lemma.length()-1)+"ies", label.intern());
				}
			}
			br.close();
		}catch(Exception e){
			e.printStackTrace();
			System.exit(0);
		}

	}

	public static NounTypes getInstance(){
		if(instance == null){
			instance = new NounTypes();
		}
		return instance;
	}
	

	
	public String getType(String noun){
		String res = wordTypeMap.get(noun);
		if(res == null){
			res = "";
		}
		return res;
	}
	
	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		String buf;
		
		if(args.length>0 && args[0].equals("--label-file") && args.length > 1){ // label a file
			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(args[1])));
			String doc = "";
			while((buf = br.readLine()) != null){
				doc += buf + "\n";
			}
			
			String word, label;
			
			List<String> sentences = AnalysisUtilities.getInstance().getSentencesStanford(doc);
			for(String s: sentences){
				Tree parse = AnalysisUtilities.getInstance().parseSentence(s).parse;
				for(Tree leaf: parse.getLeaves()){
					word = leaf.label().value();
					label = "";
					if(leaf.parent(parse).label().value().matches("NN|NNS|NNP|NNPS")){
						label = NounTypes.getInstance().getType(word.toLowerCase());
					}
					//System.err.println(word+"\t"+label);
					if(label.length()>0){
						System.out.print("<"+label+">");
					}
					System.out.print(word);
					if(label.length()>0){
						System.out.print("</"+label+">");
					}
					System.out.print(" ");
				}
				System.out.println();
				
			}
			
			
			
		}else{ // label words given on the command line
		
			//pre-load
			NounTypes.getInstance();
			
			BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
			System.err.println("Enter a noun.");
			while((buf = br.readLine()) != null){
				System.out.println(NounTypes.getInstance().getType(buf));
				System.err.println("enter a noun:");
			}
		}
	}
	
	
	private Map<String, String> wordTypeMap;

	
	private static NounTypes instance;

}

package analysis;

import java.io.IOException;

import parsestuff.AnalysisUtilities;

import data.Document;

public class _Pipeline {
	public static void main(String[] args) throws IOException {
		String path;
		if (args.length >= 1)  path = args[0];
		else path = "/d/arkref/data/test1";
	
		if(!Preprocess.alreadyPreprocessed(path)){
			Preprocess.go(path);
		}		

		Document d = Document.loadFiles(path);
		
		_Pipeline.go(d);
	}
	
	public static void go(Document d) throws IOException{

	
		FindMentions.go(d);
		Resolve.go(d);
		RefsToEntities.go(d);
		
	}
}
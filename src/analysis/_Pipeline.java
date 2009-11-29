package analysis;

import java.io.IOException;

import parsestuff.AnalysisUtilities;
import parsestuff.U;

import data.Document;

public class _Pipeline {

	public static void main(String[] args) throws IOException {
		for (String path : args) {
			U.pf("***  Input %s  ***", path);
			if(!Preprocess.alreadyPreprocessed(path)){
				Preprocess.go(path);
			}
			Document d = Document.loadFiles(path);
			_Pipeline.go(d);
		}	
	}
	
	public static void go(Document d) throws IOException{
		FindMentions.go(d);
		Resolve.go(d);
		RefsToEntities.go(d);
	}
	
}
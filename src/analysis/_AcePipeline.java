package analysis;

import java.io.IOException;

import ace.AceDocument;
import ace.FindAceMentions;
import ace.FindAceMentions.AlignmentFailed;

import parsestuff.U;
import data.Document;

public class _AcePipeline {
	public static void main(String[] args) throws Exception {
		for (String path : args) {
			path = Preprocess.shortPath(path);
			U.pf("***  Input %s  ***\n", path);
			if(!Preprocess.alreadyPreprocessed(path)){
				Preprocess.go(path);
			}
			Document myDoc = Document.loadFiles(path);
			AceDocument aceDoc= AceDocument.load(path);
			myDoc.ensureSurfaceSentenceLoad(path);
			FindAceMentions.go(myDoc, aceDoc);	
			Resolve.go(myDoc);
			RefsToEntities.go(myDoc);
			
			ace.Eval.pairwise(aceDoc, myDoc.entGraph());
		}	
	}
	
}

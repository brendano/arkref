package analysis;

import java.io.FileInputStream;
import java.util.Properties;

import parsestuff.U;
import ace.AceDocument;
import ace.AcePreprocess;
import ace.Eval;
import ace.FindAceMentions;
import data.Document;
import fig.basic.Option;
import fig.basic.OptionsParser;

public class ARKref {
	
	public static class Opts {
		@Option(gloss="Use ACE eval pipeline")
		public static boolean ace = false;
		@Option(gloss="Force preprocessing")
		public static boolean forcePre = false;
		@Option(gloss="Write entity/mention xml output to .reso.xml")
		public static boolean writeXml = false;
		@Option(gloss="Number of sentences in possible antecedent window")
		public static int sentenceWindow = 999;
		@Option(gloss="Input paths", required=true)
		public static String[] input;
	}

	public static void main(String[] args) throws Exception {
		Properties properties = new Properties();
		properties.load(new FileInputStream("config/arkref.properties"));
	
		OptionsParser op = new OptionsParser(Opts.class);
		op.doParse(args);
		
		if (Opts.input == null || Opts.input.length==0) {
			U.pl("Please specify file or files to run on.  e.g.:  ./arkref.sh -input data/*.sent"+
					"\nLeaving off extension is OK.  "+
					"We assume other files are in same directory with different extensions; "+
					"if they don't exist we will make them.");
			System.exit(-1);
		}
		
		U.pl("=Options=\n" + op.doGetOptionPairs());
		
		for (String path : Opts.input) {
			path = Preprocess.shortPath(path);

			U.pl("\n***  Input "+path+"  ***\n");
			
			Document d;

			if (Opts.ace) {
				if (Opts.forcePre || !Preprocess.alreadyPreprocessed(path)) {
					AcePreprocess.go(path);
					Preprocess.go(path);
				}
				d = Document.loadFiles(path);
				AceDocument aceDoc = AceDocument.load(path);
				d.ensureSurfaceSentenceLoad(path);
				FindAceMentions.go(d, aceDoc);
				Resolve.go(d);
				RefsToEntities.go(d);
				Eval.pairwise(aceDoc, d.entGraph());
			} else {
				if (Opts.forcePre || !Preprocess.alreadyPreprocessed(path)) {
					Preprocess.go(path);
				}
				d = Document.loadFiles(path);
				FindMentions.go(d);
				Resolve.go(d);
				RefsToEntities.go(d);
			}
			
			if (Opts.writeXml)
				WriteXml.go(d.entGraph(), path);
		}
	}

}

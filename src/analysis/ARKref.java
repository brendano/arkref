package analysis;

import java.io.BufferedReader;
import fig.basic.Option;
import fig.basic.OptionsParser;
import fig.basic.OrderedStringMap;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.Properties;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;

import ace.AceDocument;
import ace.AcePreprocess;
import ace.Eval;
import ace.FindAceMentions;

import parsestuff.U;

import data.Document;

public class ARKref {
	
	public static class Opts {
		@Option(gloss="Use ACE eval pipeline?")
		public static boolean ace = false;
		@Option(gloss="Force preprocessing?")
		public static boolean forcePre = false;
		@Option(gloss="Write entity/mention xml output?")
		public static boolean writeXml = false;
	}

	public static void main(String[] args) throws Exception {
		Properties properties = new Properties();
		properties.load(new FileInputStream("config/arkref.properties"));
	
		int i=0;
		while(i<args.length && args[i].startsWith("-")) { i += 2; }
		String[] opts   = (String []) ArrayUtils.subarray(args, 0, i);
		String[] paths  = (String []) ArrayUtils.subarray(args, i, args.length);
		
		OptionsParser op = new OptionsParser(Opts.class);
		op.doParse(opts);
		
		if (paths.length==0) {
			U.pl("Please specify file or files to run on.  "+
					"'Shortpath' without extension is OK.  "+
					"We assume other files are in same directory with different extensions; "+
					"if they don't exist we will make them.");
			System.exit(-1);
		}
		
		U.pl("=Options=\n" + op.doGetOptionPairs());
		
		U.pl("Files: [" + StringUtils.join(paths,", ")+"]");
		for (String path : paths) {
			path = Preprocess.shortPath(path);

			U.pl("\n***  Input "+path+"  ***\n");
			
			Document d = Document.loadFiles(path);

			if (Opts.ace) {
				if (Opts.forcePre || !Preprocess.alreadyPreprocessed(path)) {
					AcePreprocess.go(path);
					Preprocess.go(path);
				}
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
				FindMentions.go(d);
				Resolve.go(d);
				RefsToEntities.go(d);
			}
			
			if (Opts.writeXml)
				WriteXml.go(d.entGraph(), path);
		}
	}

}

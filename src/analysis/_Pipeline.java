package analysis;

import java.io.IOException;

import data.Document;

public class _Pipeline {
	public static void main(String[] args) throws IOException {
		String f;
		if (args.length >= 1)  f = args[0];
		else f = "/d/arkref/data/test1";
	
		Document d = Document.loadFiles(f);		
		_Pipeline.go(d);
	}
	
	public static void go(Document d){
		FindMentions.go(d);
		Resolve.go(d);
		RefsToEntities.go(d);
	}
}
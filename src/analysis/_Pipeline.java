package analysis;

import java.io.IOException;

import data.Document;
import data.Sentence;

public class _Pipeline {
	public static void main(String[] args) throws IOException {
		String f;
		if (args.length >= 1)  f = args[0];
		else f = "/d/arkref/data/test1";
		Document d = Document.loadFiles(f);
		System.out.printf("***  Input %s  ***\n\n", f);
		for (Sentence s : d.sentences) {
			System.out.printf("S%-2d\t%s\n", s.id, s.text());
		}
		FindMentions.go(d);
		ResolvePronouns.go(d);
		RefsToEntities.go(d);
	}
}

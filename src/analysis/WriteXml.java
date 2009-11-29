package analysis;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.StringEscapeUtils;

import parsestuff.U;

import data.EntityGraph;
import data.Mention;
import data.Sentence;
import data.EntityGraph.Entity;

public class WriteXml {
	public static void go(EntityGraph eg, String filename) throws FileNotFoundException {
		filename = filename + ".reso.xml";
		File file = new File(filename);
		U.pl("Writng resolutions to " + filename);
		PrintWriter pw = new PrintWriter(new FileOutputStream(file));

		pw.printf("<entities>\n");
		List<Entity> ents = eg.sortedEntities();
		for (Entity e : ents) {
			pw.printf("<entity id=\"%s\">\n", e.id);
			for (Mention m : e.sortedMentions()) {
				pw.printf("  <mention ");
				pw.printf(" id=\"%s\"", m.ID());
				Sentence s = m.getSentence();
				pw.printf(" sentence=\"%s\"", s.ID());
				pw.printf(">\n");
				
				if (m.node() != null) {
					pw.printf("    <tokens>%s</tokens>\n", StringEscapeUtils.escapeXml(
						m.node().yield().toString()));
				}
				pw.printf("  </mention>\n");	
			}
			pw.printf("</entity>\n");
		}
		pw.printf("</entities>\n");
		
		pw.close();
		
	}
	
}

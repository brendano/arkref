package ace;
import org.simpleframework.xml.*;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;
import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import edu.stanford.nlp.util.ArrayUtils;

// http://simple.sourceforge.net/download/stream/doc/tutorial/tutorial.php

public class AceDocument {
	
	////////////   APF XML structures  ////////////
	
	@Root(strict=false)
	public static class SourceFile {
		@Element(name="document")
		Document document;
	}
	@Root(strict=false)
	public static class Document {
		@ElementList(inline=true, entry="entity")
		List <Entity> entities;
		
		
		public ArrayList<Mention> getMentions() {
			ArrayList <Mention> mentions = new ArrayList<Mention>();
			for (Entity en : entities ) {
				for (Mention m : en.mentions) {
					mentions.add(m);
				}
			}
			Collections.sort(mentions, 
				new Comparator<Mention>() {
				public int compare(Mention m1, Mention m2) {
					return Integer.valueOf(m1.extent.charseq.start  ).compareTo(m2.extent.charseq.start);
				}
			});
			return mentions;
		}

	}
	@Root(name="entity",strict=false)
	public static class Entity {
		@Attribute(name="ID")
		private String aceID;
		@ElementList(inline=true)
		List <Mention> mentions;
		public String ID() { return aceID.replaceFirst(".*-E", "E"); }
	}
	@Root(name="entity_mention",strict=false)
	public static class Mention {
		@Attribute(name="ID")
		String aceID;
		@Element
		Phrase extent;
		@Element
		Phrase head;
		
		public int ID() { return Integer.parseInt(aceID.replaceFirst(".*-","")); }
	}
	@Root(strict=false)
	public static class Phrase {
		@Element(name="charseq")
		public Charseq charseq;
	}
	@Root
	public static class Charseq {
		// these start and ends are consistent with one another, but it's a complete mystery what they're counting from
		// e.g. start=0 is a random-ass place in the SGML file.
		@Attribute(name="START")
		public int start;
		@Attribute(name="END")
		public int end;
		@Text
		public String text;
	}
	
	public static Document parseFile(String apfXmlFile) { 
		Serializer serializer = new Persister();
		File source = new File(apfXmlFile);
		SourceFile sf = null;
		try {
			sf = serializer.read(SourceFile.class, source);
		} catch (Exception e) {
			e.printStackTrace();
		}
		for (Entity en : sf.document.entities ) {
			for (Mention m : en.mentions) {
				assert en.ID().replace("E","").equals(m.aceID.replaceFirst("-.*",""));
				
			}
		}

		return sf.document;
	}
	
	public static void main(String args[]) throws Exception {
		for (String f : args) {
			Document d = parseFile(f);
			for (Entity en : d.entities ) {
				for (Mention m : en.mentions) {
					System.out.println(m.aceID +" | "+m.ID()+" | "+m.head.charseq.text+" | "+m.extent.charseq.text);
					
				}
			}
		}
	}
}

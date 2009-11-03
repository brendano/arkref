package tests;

import java.io.IOException;
import java.util.List;

import parsestuff.AnalysisUtilities;

import analysis.FindMentions;
import analysis.Types;
import analysis._Pipeline;

import data.Document;
import data.Mention;

import edu.stanford.nlp.trees.Tree;
import junit.framework.TestCase;

public class TestArkref extends TestCase {

	/**
	 * Sets up the test fixture. 
	 * (Called before every test case method.) 
	 */ 
	protected void setUp() { 
	} 

	/**
	 * Tears down the test fixture. 
	 * (Called after every test case method.) 
	 */ 
	protected void tearDown() { 
	} 
	
	
	public void assertLink(Mention m1, Mention m2, Document d) {
		assertTrue(m1.getNode().toString()+"\t==>\t"+m2.getNode().toString(), d.getEntGraph().getLinkedMentions(m1).contains(m2));
	}
	public void assertLink(Document d, Mention m1, Mention m2){ assertLink(m1,m2,d); }
	public void assertNoLink(Mention m1, Mention m2, Document d) {
		assertFalse(m1.getNode().toString()+"\t==>\t"+m2.getNode().toString(), d.getEntGraph().getLinkedMentions(m1).contains(m2));
	}
	public void assertNoLink(Document d, Mention m1, Mention m2){ assertNoLink(m1,m2,d); }
	public void assertSurface(Mention m, String surface) {
		// "surface" is space-sep tokens
		assertEquals(surface, m.getNode().yield().toString());
	}
	public void assertSingleton(Document d, int mi) {
		Mention m = d.getMentions().get(mi-1);
		assertEquals(1, d.getEntGraph().getLinkedMentions(m).size());
	}

	public void assertLink(int m1, int m2, Document d) {
		assertLink(d.getMentions().get(m1-1), d.getMentions().get(m2-1), d);
	}
	public void assertLink(Document d, int m1, int m2) { assertLink(m1,m2,d); }
	public void assertNoLink(Document d, int m1, int m2) { assertNoLink(m1,m2,d); }
	public void assertNoLink(int m1, int m2, Document d) {
		assertNoLink(d.getMentions().get(m1-1), d.getMentions().get(m2-1), d);
	}
	public void assertSurface(Document d, int m, String s) {
		assertSurface(d.getMentions().get(m-1), s);
	}
	
		
	
	public void testReflexives() throws IOException{

		//John bought himself a book.  (s1)
		//Bob knew that John bought himself a book.  (s2)
		//John knew that Bob bought him a book.  (s3)
		//The company ruined itself.  (s4)
		//The corporation ruined its chances.  (s5)
		//The bank ruined it.	(s6)
		//James believed he could win. (s7)
		
		Document d = Document.loadFiles("data/reflexives");
		_Pipeline.go(d);
				
		assertLink(1,2, d); //John, himself (s1)
		assertNoLink(4,6, d); //Bob, himself (s2)
		assertLink(5,6, d); //John, himself (s2)
		assertNoLink(9,10, d); //Bob, him (s3)
		assertLink(8,10, d); //John, him (s3)
		assertLink(12,13,d); //company, itself (s4)
		assertLink(14,16,d); //corporation, its (s5)
		assertNoLink(17,18,d); //bank, it (s6)
		assertLink(19,20,d);  // James, he (s7)
	}
	
	
	
	
	public void testSameHead() throws IOException{
		//The nice, smart boy liked to play in the park.
		//This boy also liked to play soccer.
		
		Document d = Document.loadFiles("data/sameHeadWordTest");
		_Pipeline.go(d);
		
		assertSurface(d,1, "The nice , smart boy");
		assertSurface(d,2, "the park");
		assertSurface(d,3, "This boy");

		assertLink(3,1, d);
		assertNoLink(3,2, d);
	}
	
	
	public void testRoleAppositives() throws IOException{
		
		//The author John Smith wrote the book.
		//I learned about the painter John Smith, the subject of the exposition.
		//The shared Lunar Precursor Robotic Program was new.
		
		Document d = Document.loadFiles("data/roleAppositivesTest");
		_Pipeline.go(d);
		
		assertTrue(d.getMentions().toString(), d.getMentions().size()==9);
		
		//Tree t = AnalysisUtilities.getInstance().readTreeFromString("(NP (NP (DT The) (NN author)) (NNP John) (NNP Smith))");
		//System.err.println(t.toString());
		//System.err.println(t.headTerminal(AnalysisUtilities.getInstance().getHeadFinder()).toString());
		

		assertSurface(d, 1, "The author John Smith");
		assertSurface(d, 2, "The author");
		assertSurface(d, 5, "the famous painter John Smith , the subject of the exposition");
		assertSurface(d, 6, "the famous painter");
		assertSurface(d, 7, "the subject of the exposition");
		assertSurface(d, 9, "The shared Lunar Precursor Robotic Program");
		
		assertLink(2,1, d);
		assertLink(7,5, d);
		assertLink(6,5, d);
	}
	
	public void testFirstPerson() throws IOException{
		Document d = Document.loadFiles("data/roleAppositivesTest");
		_Pipeline.go(d);
		assertSurface(d, 4, "I");
		assertSingleton(d, 4);
		
		d = Document.loadFiles("data/firstPerson1");
		_Pipeline.go(d);
		assertSurface(d,1,"I");
		assertSurface(d,4,"I");
		assertSurface(d,6,"my");
		assertSurface(d,7,"it");
		assertLink(d,1,4);
		assertLink(d,4,6);
		assertNoLink(d,6,7);
		// BTO: should do appositive & pred-noms? low prio
	}
	
	public void testDefaultMale() throws IOException{
		// Some of these cases should flip if we turn on hard gender constraints
		// Which might make more sense for certain domains.
		// If we make it an option, should make tests to ensure the flip happens.

		Document d;
		d = Document.loadFiles("data/defaultMale1"); _Pipeline.go(d);
		assertSurface(d,1, "Sally");
			assertEquals(Types.Gender.Female, Types.gender(mention(d,1)));
		assertSurface(d,2, "the banker");
			assertEquals(null, Types.gender(mention(d,2)));
		assertSurface(d,3, "He");
		assertLink(d, 2,3);

		d = Document.loadFiles("data/defaultMale2"); _Pipeline.go(d);
		assertSurface(d,1, "Bob");
			assertEquals(Types.Gender.Male, Types.gender(mention(d,1)));
		assertSurface(d,2, "the banker");
			assertEquals(null, Types.gender(mention(d,2)));
		assertSurface(d,3, "He");
		assertLink(d, 1,3);

		d = Document.loadFiles("data/defaultMale3"); _Pipeline.go(d);
		assertSurface(d,1, "The banker");
			assertEquals(null, Types.gender(mention(d,1)));
		assertSurface(d,2, "Bob");
			assertEquals(Types.Gender.Male, Types.gender(mention(d,2)));
		assertSurface(d,3, "He");
		assertLink(d, 1,3);
				
	}
	
	public void testAppositives() throws IOException{
		//example from H&K 2009
		//Walmart says Gitano, its top-selling brand, is underselling.
		
		Document d = Document.loadFiles("data/IWithinI");
		_Pipeline.go(d);
		
		assertSurface(d, 1, "Walmart");
		assertSurface(d, 2, "Gitano , its top-selling brand ,");
		assertSurface(d, 3, "its top-selling brand");
		assertSurface(d, 4, "its");
		
		assertLink(3,2, d);
		
		d = Document.loadFiles("data/nativeAmericans");
		_Pipeline.go(d);
		

		assertSurface(d, 7, "the Anishinaabe , the Dakota , and other Native American inhabitants");
		assertSurface(d, 8, "the Dakota");
		assertSurface(d, 9, "other Native American inhabitants");
		
		assertNoLink(7,8, d);
		assertNoLink(7,9, d);
		assertNoLink(8,9, d);
	}
	
	
	
	public void testPathLength() throws IOException{
		//John knew that Bob was weird, but he still invited him to the party.
		Document d = Document.loadFiles("data/pathLengthTest");
		_Pipeline.go(d);

		assertSurface(d, 1, "John");
		assertSurface(d, 2, "Bob");
		assertSurface(d, 3, "he");
		assertLink(3,1, d);
		assertNoLink(3,2, d);
		// BTO: error, "him" != "John" but path length won't solve
	}
	
	
	public void testLargerNodeComesFirstAsMention() throws IOException{
		//Nintendo of America announced its new console.
		Document d = Document.loadFiles("data/pathLengthTest2");
		_Pipeline.go(d);

		assertSurface(d,1,"Nintendo of America");
		assertSurface(d,2,"America");
		assertSurface(d,3,"its new console"); 
		assertSurface(d,4,"its"); 
		
		assertTrue(""+d.getMentions(), d.getMentions().size()==4);		
	}
	

	public void testPathLength2() throws IOException{
		//Nintendo of America announced its new console.
		Document d = Document.loadFiles("data/pathLengthTest2");
		_Pipeline.go(d);

		Mention m1 = d.getMentions().get(0); //Nintendo of America
		Mention m2 = d.getMentions().get(1); //America
		Mention m3 = d.getMentions().get(2); //its new console
		Mention m4 = d.getMentions().get(3); //its
		
		assertLink(m4,m1, d);
		assertNoLink(m4,m2, d);
		assertNoLink(m2,m1, d);
		assertNoLink(m3,m2, d);
		
		//TODO the following commented-out test needs semantic information (i.e., Nintendo != console)
		//BTO: but it works
		assertNoLink(m3,m1, d);		
	}
	
	public void testEntityTypeMatching() throws IOException{
		//John went to the store.
		//Bob also went to the store.
		//It was a grocery store.
		//He bought an item.

		Document d = Document.loadFiles("data/test1");
		_Pipeline.go(d);
				
		assertLink(5,4,d); //store and it
		assertNoLink(3,2,d); //bob and store
		assertLink(7,3,d); //he and bob
		assertNoLink(7,6,d); //he and store
		assertNoLink(1,2,d); //john and store
		
		d = Document.loadFiles("data/personNounTest");
		_Pipeline.go(d);

		//The astronaut went to the space with Howard.
		//The robot did, too.
		//He had fun.
				
		assertLink(1,5,d); //astronaut and he
		assertNoLink(5,4,d); //he and robot
		assertNoLink(4,1,d); //robot and astronaut
		assertNoLink(5,3,d); //he and Howard
		assertSingleton(d,6); //fun
	}
	
	
	public void testIWithinI() throws IOException{
		//example from H&K 2009
		//Walmart says Gitano, its top-selling brand, is underselling.
		
		Document d = Document.loadFiles("data/IWithinI");
		_Pipeline.go(d);

		assertSurface(d,1,"Walmart");
		assertSurface(d,2,"Gitano , its top-selling brand ,");
		assertSurface(d,3,"its top-selling brand");
		assertSurface(d,4,"its");
		
		assertLink(  4,1, d);
		assertNoLink(4,2, d);
		assertNoLink(4,3, d);
	}
	
	
	public void testPredicateNominatives() throws IOException{
		
		//Lincoln was president.
		//Lincoln had been president.
		//Lincoln was being president.
		//Lincoln will be president.
		
		Document d = Document.loadFiles("data/predNomTest");
		_Pipeline.go(d);

		Mention m1;
		Mention m2;
		
		m1 = d.getMentions().get(0); //Lincoln
		m2 = d.getMentions().get(1); //president
		assertLink(m1,m2,d);

		m1 = d.getMentions().get(2); //Lincoln
		m2 = d.getMentions().get(3); //president
		assertLink(m1,m2,d);

		m1 = d.getMentions().get(6); //Lincoln
		m2 = d.getMentions().get(7); //president
		assertLink(m1,m2,d);
	}
	
	
	
	public void testConjunctions() throws IOException{
		//He and Fred went to the store.
		//They also went to the library.
		
		Document d = Document.loadFiles("data/conjunctionsTest");
		_Pipeline.go(d);

		Mention m1 = d.getMentions().get(0); //He and Fred
		Mention m2 = d.getMentions().get(1); //the store
		
		assertSurface(m1, "He and Fred");
		assertSurface(m2, "the store");
		assertNoLink(1, 2, d);
		assertLink(1, 3, d);
		
	}
	
	public void testThey() throws IOException{
		//The earliest known settlers followed herds of large game to the region
		//during the last glacial period. They preceded the Anishinaabe, the Dakota, 
		//and other Native American inhabitants.
		
		Document d = Document.loadFiles("data/they1");
		_Pipeline.go(d);
		assertSurface(d,1,"The earliest known settlers");
		assertSurface(d,6,"They");

		Mention m1 = mention(d,1);
		assertEquals(Types.Number.Plural, Types.number(m1));

		//The team practiced very hard, and later on they won the game.
		//The herd of animals grazed on the land, and then they moved on.
		
		 d = Document.loadFiles("data/they2");
		_Pipeline.go(d);
		assertLink(d,1,2);
		assertLink(d,4,7);
		assertNoLink(d,2,4);
	}
	
	
	public Mention mention(Document d, int mi) {
		return d.getMentions().get(mi-1);
	}
	
	
	public void testFindNodeFromSpan() throws IOException{
		//He and Fred went to the store.
		//They also went to the library.
		
		Document d = Document.loadFiles("data/conjunctionsTest");
		_Pipeline.go(d);

		Tree t;
		
		t = d.findNodeThatCoversSpan(0, 0, 0);
		assertTrue(t.yield().toString(), t.yield().toString().equals("He"));
				
		t = d.findNodeThatCoversSpan(0, 0, 2);
		assertTrue(t.yield().toString(), t.yield().toString().equals("He and Fred"));
		
		t = d.findNodeThatCoversSpan(0, 0, 3);
		assertTrue(t.yield().toString(), t.yield().toString().equals("He and Fred went to the store ."));

		t = d.findNodeThatCoversSpan(0, 3, 5);
		assertTrue(t.yield().toString(), t.yield().toString().equals("went to the store"));
	}
	
	
	public void testFindMentionDominatingNode() throws IOException{
		//example from H&K 2009
		//Walmart says Gitano, its top-selling brand, is underselling.
		
		Document d = Document.loadFiles("data/IWithinI");
		_Pipeline.go(d);
		
		Tree t; 
		Mention m;
		
		t = d.findNodeThatCoversSpan(0, 0, 0);
		m = d.findMentionDominatingNode(0, t);
		assertTrue(t.yield().toString(), t.yield().toString().equals("Walmart"));
		assertTrue(m.toString(), m.getNode().yield().toString().equals("Walmart"));
		
		t = d.findNodeThatCoversSpan(0, 6, 6);
		m = d.findMentionDominatingNode(0, t);
		assertTrue(t.yield().toString(), t.yield().toString().equals("brand"));
		assertTrue(m.toString(), m.getNode().yield().toString().equals("its top-selling brand"));
		
		t = d.findNodeThatCoversSpan(0, 2, 2);
		m = d.findMentionDominatingNode(0, t);
		assertTrue(t.yield().toString(), t.yield().toString().equals("Gitano"));
		assertTrue(m.toString(), m.getNode().yield().toString().equals("Gitano , its top-selling brand ,"));
		
	}
	
	
	
	public void testAdjunctPhrases() throws IOException{
		//The students were tired of working. (s1)
		//To meet their friends, they went to the bar. (s2)
		//To meet new people, they talked to them. (s3)
		//Since Bill wanted to talk to John, he picked up the phone. (s4)
		//To Susan, she seemed nice. (s5)
		
		Document d = Document.loadFiles("data/adjunctPhrases");
		_Pipeline.go(d);
				
		assertLink(1,3, d); //students, their
		assertLink(1,4, d); //students, they
		assertNoLink(2,4, d); //their friends, they
		assertLink(1,7, d); //students, they (s3)
		assertNoLink(6,7, d); //they (s3), new people
		assertLink(8,6, d); //them (s3), new people
		assertNoLink(1,8, d); //students, them (s3)
		assertLink(9,11,d); //Bill, he (s4)
		assertNoLink(10,11,d); //John, he (s4)
		assertNoLink(13,14,d); //Susan, she (s5)
	}	

	
}



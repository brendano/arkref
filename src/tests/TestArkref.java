package tests;

import java.io.IOException;
import java.util.List;

import parsestuff.AnalysisUtilities;

import analysis.FindMentions;
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
		assertTrue(m1.getNode().toString(), d.getEntGraph().getLinkedMentions(m1).contains(m2));
	}
	public void assertLink(Document d, Mention m1, Mention m2){ assertLink(m1,m2,d); }
	public void assertNoLink(Mention m1, Mention m2, Document d) {
		assertFalse(m1.getNode().toString(), d.getEntGraph().getLinkedMentions(m1).contains(m2));
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
		
		Document d = Document.loadFiles("data/roleAppositivesTest");
		_Pipeline.go(d);
		
		assertTrue(d.getMentions().toString(), d.getMentions().size()==8);
		
		//Tree t = AnalysisUtilities.getInstance().readTreeFromString("(NP (NP (DT The) (NN author)) (NNP John) (NNP Smith))");
		//System.err.println(t.toString());
		//System.err.println(t.headTerminal(AnalysisUtilities.getInstance().getHeadFinder()).toString());
		

		assertSurface(d, 1, "The author John Smith");
		assertSurface(d, 2, "The author");
		assertSurface(d, 5, "the famous painter John Smith , the subject of the exposition");
		assertSurface(d, 6, "the famous painter");
		assertSurface(d, 7, "the subject of the exposition");
		
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
		//Bob went to the store.
		//It was a grocery store.
		//He bought an item.

		Document d = Document.loadFiles("data/test1");
		_Pipeline.go(d);

		Mention m1 = d.getMentions().get(0); //Bob
		Mention m2 = d.getMentions().get(1); //store
		Mention m3 = d.getMentions().get(2); //it
		Mention m5 = d.getMentions().get(4); //he
		
		assertLink(m3,m2,d);
		assertNoLink(m3,m1,d);
		assertLink(m5,m1,d);
		assertNoLink(m5,m2,d);
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
		
		Document d = Document.loadFiles("data/conjunctionsTest");
		_Pipeline.go(d);

		Mention m1 = d.getMentions().get(0); //He and Fred
		Mention m2 = d.getMentions().get(1); //the store
		
		assertTrue(d.getMentions().toString(), d.getMentions().size() == 2);
		assertSurface(m1, "He and Fred");
		assertSurface(m2, "the store");
		assertNoLink(m1,m2, d);
	}
	
	
}

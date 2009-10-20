package tests;

import java.io.IOException;
import java.util.List;

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
	
	public void testAppositives() throws IOException{
		//example from H&K 2009
		//Walmart says Gitano, its top-selling brand, is underselling.
		
		Document d = Document.loadFiles("data/IWithinI");
		_Pipeline.go(d);

		Mention m1 = d.getMentions().get(0); //Walmart
		Mention m2 = d.getMentions().get(1); //Gitano, its top-selling brand,
		Mention m3 = d.getMentions().get(2); //its top-selling brand
		Mention m4 = d.getMentions().get(3); //its
		
		assertTrue(m1.getNode().toString(), m1.getNode().yield().toString().equals("Walmart"));
		assertTrue(m2.getNode().toString(), m2.getNode().yield().toString().equals("Gitano , its top-selling brand ,"));
		assertTrue(m3.getNode().toString(), m3.getNode().yield().toString().equals("its top-selling brand"));
		assertTrue(m4.getNode().toString(), m4.getNode().yield().toString().equals("its"));
		
		assertTrue(m3.getNode().toString(), d.getEntGraph().getLinkedMentions(m3).contains(m2));
	}
	
	public void testPathLength() throws IOException{
		//John knew that Bob was weird, but he still invited him to the party.
		Document d = Document.loadFiles("data/pathLengthTest");
		_Pipeline.go(d);

		Mention m1 = d.getMentions().get(0); //John
		Mention m2 = d.getMentions().get(1); //Bob
		Mention m3 = d.getMentions().get(2); //he
		assertTrue(m3.getNode().toString(), d.getEntGraph().getLinkedMentions(m3).contains(m1));
		assertFalse(m3.getNode().toString(), d.getEntGraph().getLinkedMentions(m3).contains(m2));
	}
	
	
	public void testLargerNodeComesFirstAsMention() throws IOException{
		//Nintendo of America announced its new console.
		Document d = Document.loadFiles("data/pathLengthTest2");
		_Pipeline.go(d);

		Mention m1 = d.getMentions().get(0); //Nintendo of America
		Mention m2 = d.getMentions().get(1); //America
		Mention m3 = d.getMentions().get(2); //its new console
		Mention m4 = d.getMentions().get(3); //its
		
		assertTrue(""+d.getMentions(), d.getMentions().size()==4);
		
		assertTrue(d.getMentions().toString(), m1.getNode().yield().toString().equals("Nintendo of America"));
		assertTrue(d.getMentions().toString(), m2.getNode().yield().toString().equals("America"));
		assertTrue(d.getMentions().toString(), m3.getNode().yield().toString().equals("its new console")); 
		assertTrue(d.getMentions().toString(), m4.getNode().yield().toString().equals("its")); 
	}
	

	public void testPathLength2() throws IOException{
		//Nintendo of America announced its new console.
		Document d = Document.loadFiles("data/pathLengthTest2");
		_Pipeline.go(d);

		Mention m1 = d.getMentions().get(0); //Nintendo of America
		Mention m2 = d.getMentions().get(1); //America
		Mention m3 = d.getMentions().get(2); //its new console
		Mention m4 = d.getMentions().get(3); //its
		
		assertTrue(m4.getNode().toString(), d.getEntGraph().getLinkedMentions(m4).contains(m1));
		assertFalse(m4.getNode().toString(), d.getEntGraph().getLinkedMentions(m4).contains(m2));
		assertFalse(m2.getNode().toString(), d.getEntGraph().getLinkedMentions(m2).contains(m1));
		assertFalse(m3.getNode().toString(), d.getEntGraph().getLinkedMentions(m3).contains(m2));
		
		//TODO the following commented-out test needs semantic information (i.e., Nintendo != console)
		//assertFalse(m3.getNode().toString(), d.getEntGraph().getLinkedMentions(m3).contains(m1));
		
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
		
		assertTrue(m3.getNode().toString(), d.getEntGraph().getLinkedMentions(m3).contains(m2));
		assertFalse(m3.getNode().toString(), d.getEntGraph().getLinkedMentions(m3).contains(m1));
		assertTrue(m5.getNode().toString(), d.getEntGraph().getLinkedMentions(m5).contains(m1));
		assertFalse(m5.getNode().toString(), d.getEntGraph().getLinkedMentions(m5).contains(m2));
	}
	
	
	public void testIWithinI() throws IOException{
		//example from H&K 2009
		//Walmart says Gitano, its top-selling brand, is underselling.
		
		Document d = Document.loadFiles("data/IWithinI");
		_Pipeline.go(d);

		Mention m1 = d.getMentions().get(0); //Walmart
		Mention m2 = d.getMentions().get(1); //Gitano, its top-selling brand,
		Mention m3 = d.getMentions().get(2); //its top-selling brand
		Mention m4 = d.getMentions().get(3); //its
		
		assertTrue(m1.getNode().toString(), m1.getNode().yield().toString().equals("Walmart"));
		assertTrue(m2.getNode().toString(), m2.getNode().yield().toString().equals("Gitano , its top-selling brand ,"));
		assertTrue(m3.getNode().toString(), m3.getNode().yield().toString().equals("its top-selling brand"));
		assertTrue(m4.getNode().toString(), m4.getNode().yield().toString().equals("its"));
		
		assertTrue(m4.getNode().toString(), d.getEntGraph().getLinkedMentions(m4).contains(m1));
		assertFalse(m4.getNode().toString(), d.getEntGraph().getLinkedMentions(m4).contains(m2));
		assertFalse(m4.getNode().toString(), d.getEntGraph().getLinkedMentions(m4).contains(m3));
	}
	
	

	
	
}

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
	
	
	
	public void testPathLength() throws IOException{
		//John knew that Bob was weird, but he still invited him to the party.
		Document d = Document.loadFiles("data/pathLengthTest");
		_Pipeline.go(d);

		Mention m0 = d.getMentions().get(0); //John
		Mention m1 = d.getMentions().get(1); //Bob
		Mention m2 = d.getMentions().get(2); //he
		assertTrue(m2.getNode().toString(), d.getEntGraph().getLinkedMentions(m2).contains(m0));
		assertFalse(m2.getNode().toString(), d.getEntGraph().getLinkedMentions(m2).contains(m1));
	}
	
	
	public void testEntityTypeMatching() throws IOException{
		//Bob went to the store.
		//It was a grocery store.
		//He bought an item.

		Document d = Document.loadFiles("data/test1");
		_Pipeline.go(d);

		Mention m0 = d.getMentions().get(0); //Bob
		Mention m1 = d.getMentions().get(1); //store
		Mention m2 = d.getMentions().get(2); //it
		Mention m4 = d.getMentions().get(4); //he
		
		assertTrue(m2.getNode().toString(), d.getEntGraph().getLinkedMentions(m2).contains(m1));
		assertFalse(m2.getNode().toString(), d.getEntGraph().getLinkedMentions(m2).contains(m0));
		assertTrue(m4.getNode().toString(), d.getEntGraph().getLinkedMentions(m4).contains(m0));
		assertFalse(m4.getNode().toString(), d.getEntGraph().getLinkedMentions(m4).contains(m1));
	}
	
	
	
}

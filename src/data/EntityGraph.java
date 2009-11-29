package data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

/**
 * progressively add pairwise equivalences to this data structure.  
 * internally, it builds the transitive closure. 
 **/
public class EntityGraph {
	public Map<Mention, HashSet<Mention>> mention2corefs;
	public Set<Entity> entities = null;
	
	public static class Entity {
		public String id; 
		public Set<Mention> mentions;
		
		public int hashCode() { return id.hashCode(); }
		public boolean equals(Entity e2) { 
			assert this.id!=null && e2.id!=null;
			return this.id.equals( e2.id );
		}
	}
	public EntityGraph(Document d) {
		mention2corefs = new HashMap<Mention, HashSet<Mention>>();
		for (Mention m : d.mentions()) { 
			mention2corefs.put(m, new HashSet<Mention>());
			mention2corefs.get(m).add(m);
		}
	}
	
	public void addPair(Mention m1, Mention m2) {
		assert entities==null : "we're frozen, please don't addPair() anymore";
		// Strategy: always keep mention2corefs a complete record of all coreferents for that mention
		// So all we do is merge
		Set<Mention> corefs1 = (Set<Mention>) mention2corefs.get(m1).clone();
		Set<Mention> corefs2 = (Set<Mention>) mention2corefs.get(m2).clone();
		for (Mention n1 : corefs1) {
			for (Mention n2 : corefs2) {
				mention2corefs.get(n1).add(n2);
				mention2corefs.get(n2).add(n1);
			}
		}
	}
	
	/** Call this only once, and only once all addPair()ing is done. **/
	public void freezeEntities() {
		assert entities == null : "call freezeEntities() only once please";
		entities = new HashSet<Entity>();
		for (Mention m : mention2corefs.keySet()) {
			Entity e = makeEntity(m);
			entities.add(e);
		}
	}
	/** helper for freezeEntities() **/
	private Entity makeEntity(Mention m) {
		Entity e = new Entity();
		e.id = entName(m);
		e.mentions = mention2corefs.get(m);
		return e;
	}
	
	public Set<Mention> getLinkedMentions(Mention m){
		return mention2corefs.get(m);
	}
	
	public boolean isSingleton(Mention m) {
		return mention2corefs.get(m).size()==1;
	}
	
	public String entName(Mention m) {
		return entName(mention2corefs.get(m));
	}
	
	public String entName(Set<Mention> corefs) {
		List<Integer> L = new ArrayList<Integer>();
		for (Mention m : corefs) {
			L.add(m.ID());
		}
		Collections.sort(L);
		return StringUtils.join(L, "_");
	}
}

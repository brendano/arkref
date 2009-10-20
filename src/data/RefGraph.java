package data;

import java.util.HashMap;
import java.util.Map;

/** build this up through the pipeline stages **/
public class RefGraph {
//	public Map<Mention, ArrayList<Mention>> refCandidates;
	private Map<Mention, Mention> finalResolutions;
	
	public RefGraph(){ finalResolutions=new HashMap<Mention, Mention>(); }
	
	
	
	public void setNullRef(Mention m) {
		finalResolutions.put(m, null);
	}
	
	public void setRef(Mention m, Mention m2) {
		finalResolutions.put(m, m2);
	}
	
	public Map<Mention, Mention> getFinalResolutions() {
		return finalResolutions;
	}

	public boolean needsReso(Mention m) {
		return !finalResolutions.containsKey(m);
	}
}

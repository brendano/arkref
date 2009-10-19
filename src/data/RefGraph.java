package data;

import java.util.ArrayList;
import java.util.HashMap;

/** build this up through the pipeline stages **/
public class RefGraph {
//	public HashMap<Mention, ArrayList<Mention>> refCandidates;
	public HashMap<Mention, Mention> finalResolutions;
	public RefGraph(){ finalResolutions=new HashMap(); }
	public void setNullRef(Mention m) {
		finalResolutions.put(m, null);
	}
	public void setRef(Mention m, Mention m2) {
		finalResolutions.put(m, m2);
	}
	
	public boolean needsReso(Mention m) {
		return !finalResolutions.containsKey(m);
	}
}

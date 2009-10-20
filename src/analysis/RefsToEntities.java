package analysis;

import data.Document;
import data.EntityGraph;
import data.Mention;

/** Do the transitive closure to make reference-referent pairs into entity partitions **/
public class RefsToEntities {
	public static void go(Document d) {
		EntityGraph eg = new EntityGraph(d);
		for (Mention m1 : d.getRefGraph().getFinalResolutions().keySet()) {
			if (d.getRefGraph().getFinalResolutions().get(m1) != null) {
				eg.addPair(m1, d.getRefGraph().getFinalResolutions().get(m1));
			}
		}
		
		d.setEntGraph(eg);
		
		System.out.println("\n*** Entity Report ***\n");
		for (Mention m : d.getMentions()){
			if (eg.isSingleton(m)) {
				System.out.printf("%-20s %s\n", "singleton", m);
			} else {
				System.out.printf("%-20s %s\n", "entity_"+eg.entName(m), m);
			}
		}
	}
}

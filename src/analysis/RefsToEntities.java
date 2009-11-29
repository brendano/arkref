package analysis;

import data.Document;
import data.EntityGraph;
import data.Mention;

/** Do the transitive closure to make reference-referent pairs into entity partitions **/
public class RefsToEntities {
	public static void go(Document d) {
		EntityGraph eg = new EntityGraph(d);
		for (Mention m1 : d.refGraph().getFinalResolutions().keySet()) {
			if (d.refGraph().getFinalResolutions().get(m1) != null) {
				eg.addPair(m1, d.refGraph().getFinalResolutions().get(m1));
			}
		}
		
		d.setEntGraph(eg);
		
		System.out.println("\n*** Entity Report ***\n");
		int s=-1;
		for (Mention m : d.mentions()){
			if (m.getSentence().getID() != s) {
				s = m.getSentence().getID();
				System.out.printf("S%-2s  %s\n",s, m.getSentence().text());
			}
			if (eg.isSingleton(m)) {
				System.out.printf("\t%-20s  %s\n", "singleton", m);
			} else {
				System.out.printf("\t%-20s  %s\n", "entity_"+eg.entName(m), m);
			}
		}
//		System.out.println("");
	}
}

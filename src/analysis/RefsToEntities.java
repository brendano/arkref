package analysis;

import parsestuff.U;
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
		eg.freezeEntities();
		
		d.setEntGraph(eg);
		
		System.out.println("\n*** Entity Report ***\n");
		int s=-1;
		for (Mention m : d.mentions()){
			if (m.getSentence().ID() != s) {
				s = m.getSentence().ID();
				System.out.printf("S%-2s  %s\n",s, m.getSentence().text());
			}
			U.pf("  ");
			if (m.aceMention != null) {
				U.pf("%-3s ", m.aceMention.isSingleton() ? "" : m.aceMention.entity);
			}
			if (eg.isSingleton(m)) {
				U.pf("%-20s  %s\n", "singleton", m);
			} else {
				U.pf("%-20s  %s\n", "entity_"+eg.entName(m), m);
			}
		}
//		System.out.println("");
	}
}

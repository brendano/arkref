package arkref.ace;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import arkref.data.EntityGraph;
import arkref.data.Mention;
import arkref.parsestuff.U;


public class Eval {

	public static void bcubed(AceDocument aceDoc, EntityGraph eg) {
		U.pl("\n***  B-cubed Evaluation ***\n");
		double recsum = 0, precsum = 0;
		assert eg.mention2corefs.size() == aceDoc.document.getMentions().size();
		
		Map<Mention, Set<Mention>>mention2goldcorefs = new HashMap<Mention,Set<Mention>>();
		for (Mention m : eg.mention2corefs.keySet()) {
			mention2goldcorefs.put(m, new HashSet<Mention>());
			if (m.aceMention == null) {
				continue;
			}
			for (AceDocument.Mention goldM : m.aceMention.entity.mentions) {
				mention2goldcorefs.get(m).add(goldM.myMention);
			}
		}
		
		int nMentions = 0;
		for (Mention m : eg.mention2corefs.keySet()) {
			nMentions++;
			int predSize = eg.mention2corefs.get(m).size();
//			if (!mention2goldcorefs.containsKey(m)) {
//				U.pl("B3 SKIPPING MENTION " + m);
//				continue;
//			}
			int goldSize = mention2goldcorefs.get(m).size();
			
			Set<Mention> intersection = new HashSet<>(eg.mention2corefs.get(m));
			intersection.retainAll(mention2goldcorefs.get(m));
			int bothSize = intersection.size();
//			U.pf("gold=%d pred=%d both=%d\n", goldSize, predSize, bothSize);
			if (goldSize>0) recsum += 1.0*bothSize/goldSize;
			if (predSize>0) precsum += 1.0*bothSize/predSize;
		}
		assert nMentions > 0;
		U.pf("B3 prec=%s rec=%s\n", precsum/nMentions, recsum/nMentions);
	}
	
	/**
	 *  we iterate through ACE's notions of entities and their mention members
	 *  and test to see if our system agreed in its pairwise decisions.
	 *  
	 *  could take just the RefGraph actually for this
	 *  
	 */
	public static void pairwise(AceDocument aceDoc, EntityGraph eg) {
		U.pl("\n***  Pairwise Evaluation  ***\n");
		
		int gold_tp=0,fn=0;
		
		
		U.pl("\n**  Analysis of gold clusters  (for Recall)  **\n");
		for (AceDocument.Entity aceE : aceDoc.document.entities) {
			String stuff = "";
			int cluster_tp=0, cluster_fn=0;
			for (int i=0; i < aceE.mentions.size(); i++) {
				AceDocument.Mention am1 = aceE.mentions.get(i);
				arkref.data.Mention mm1 = am1.myMention;
//				U.pl(am1);
				Set<arkref.data.Mention> corefs = eg.getLinkedMentions(mm1);
				stuff += U.sf("  %-30s | %-20s | %s\n", am1, corefs.size()==1 ? "singleton" : "entity_"+eg.entName(mm1),   mm1);
//				if (eg.mention2corefs.get(mm1).size()==1)
//					U.pl("Resolved as singleton");
//				else
//					U.pl("Resolved to  =>  " + eg.entName(mm1));
				for (int j=0; j < aceE.mentions.size(); j++) {
					if (i==j){
						continue;
					}
					AceDocument.Mention am2 = aceE.mentions.get(j);
					arkref.data.Mention mm2 = am2.myMention;					
					boolean match;
					if (mm1==null || mm2==null)
						match = false;
					else
						match = eg.mention2corefs.get(mm1).contains(mm2);
					if (match)
						cluster_tp++;
					else
						cluster_fn++;
				}
				
			}
			cluster_fn /= 2;
			cluster_tp /= 2;
			
			U.pf("%-10s", aceE);
			if (cluster_tp+cluster_fn > 0)
				U.pf("%3d / %-3d  missing links", cluster_fn, cluster_tp+cluster_fn);
			U.pf("\n");
			U.pl(stuff);
			
			gold_tp += cluster_tp;
			fn += cluster_fn;
		}
		
		int pred_tp=0, fp=0;
		
		U.pl("\n**  Analysis of predicted clusters  (for Precision)  **\n");
		for (EntityGraph.Entity myE : eg.sortedEntities()) {
			List<arkref.data.Mention> mentions = myE.sortedMentions();
			int cluster_tp=0, cluster_fp=0;
			
			if (myE.mentions.size()==1)  continue;
			
			String stuff="";
			for (int i=0; i < mentions.size(); i++) {
				arkref.data.Mention mm1 = mentions.get(i);
				AceDocument.Mention am1 = aceDoc.getAceMention(mm1);
				
				AceDocument.Entity goldEnt = aceDoc.getAceMention(mm1)==null ? null : aceDoc.getAceMention(mm1).entity;
				stuff += U.sf("  gold %-12s || %s\n",  goldEnt,   mm1);

				for (int j=0; j < mentions.size(); j++) {
					if (i==j) continue;
					arkref.data.Mention mm2 = mentions.get(j);
					AceDocument.Mention am2 = aceDoc.getAceMention(mm2);					
					boolean match;
					if (am1==null || am2==null)
						match = false;
					else
						match = am1.entity == am2.entity;
					
					if (match)
						cluster_tp++;
					else
						cluster_fp++;
				}	
			}
			cluster_fp /= 2;
			cluster_tp /= 2;

			U.pf("%-30s  %3d / %-3d  bad links\n", myE, cluster_fp, cluster_fp+cluster_tp);
			U.pl(stuff);
			
			pred_tp += cluster_tp;
			fp += cluster_fp;
		}
		
		assert pred_tp == gold_tp;
		
		U.pl("\n***  Numbers  ***\n");
		U.pf("Pairwise Eval:  tp=%-4d fp=%-4d fn=%-4d\t%s\n",  pred_tp, fp, fn,  aceDoc.document.docid);
		U.pf("Doc Prec = %.3f   Doc Rec = %.3f    \t%s\n", safeDivide(pred_tp, pred_tp+fp), safeDivide(gold_tp, (gold_tp+fn)), aceDoc.document.docid);
//		U.pf("pred_tp=%-4d fp=%-4d  =>  Precision = %.3f\n", pred_tp, fp,  pred_tp*1.0/(pred_tp+fp));
//		U.pf("gold_tp=%-4d fn=%-4d  =>  Recall = %.3f\n", gold_tp, fn,  gold_tp*1.0/(gold_tp+fn));
	}
	static double safeDivide(double x, double y) {
		if (y==0) return 0;
		return x/y;
	}
}

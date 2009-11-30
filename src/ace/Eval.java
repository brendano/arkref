package ace;

import java.util.List;
import java.util.Set;

import parsestuff.U;
import data.EntityGraph;

public class Eval {

	/**
	 *  we iterate through ACE's notions of entities and their mention members
	 *  and test to see if our system agreed in its pairwise decisions.
	 *  
	 *  could take just the RefGraph actually for this
	 *  
	 *  H&K has a weird definition of pairwise F1 that i don't understand for precision
	 */
	public static void pairwise(AceDocument aceDoc, EntityGraph eg) {
		U.pl("\n***  Pairwise Evaluation  ***\n");
		
		int gold_tp=0,fn=0;
		
		
		U.pl("\n**  Analysis of gold clusters  (for Recall)  **\n");
		for (AceDocument.Entity aceE : aceDoc.document.entities) {
			U.pl("");
			U.pl(aceE);
			int cluster_tp=0, cluster_fn=0;
			for (int i=0; i < aceE.mentions.size(); i++) {
				AceDocument.Mention am1 = aceE.mentions.get(i);
				data.Mention mm1 = am1.myMention;
				U.pl(am1);
				Set<data.Mention> corefs = eg.getLinkedMentions(mm1);
				U.pf("  %-20s | %s\n", corefs.size()==1 ? "singleton" : "entity_"+eg.entName(mm1),   mm1);
//				if (eg.mention2corefs.get(mm1).size()==1)
//					U.pl("Resolved as singleton");
//				else
//					U.pl("Resolved to  =>  " + eg.entName(mm1));
				for (int j=0; j < aceE.mentions.size(); j++) {
					if (i==j) continue;
					AceDocument.Mention am2 = aceE.mentions.get(j);
					data.Mention mm2 = am2.myMention;					
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
			U.pf("%3d/%-3d  missing links\n", cluster_fn, cluster_tp+cluster_fn);
			gold_tp += cluster_tp;
			fn += cluster_fn;
		}
		
		int pred_tp=0, fp=0;
		
		U.pl("\n**  Analysis of predicted clusters  (for Precision)  **\n");
		for (EntityGraph.Entity myE : eg.sortedEntities()) {
			List<data.Mention> mentions = myE.sortedMentions();
			int cluster_tp=0, cluster_fp=0;
			
			if (myE.mentions.size()==1)  continue;
			U.pf("%s", myE);
//			if (myE.mentions.size()==1)  {
//				U.pf("  skipping\n");
//				continue;
//			}
			U.pl("");
			for (int i=0; i < mentions.size(); i++) {
				data.Mention mm1 = mentions.get(i);
				AceDocument.Mention am1 = aceDoc.getAceMention(mm1);
				
				AceDocument.Entity goldEnt = aceDoc.getAceMention(mm1)==null ? null : aceDoc.getAceMention(mm1).entity;
				U.pf("  gold %-12s || %s\n",  goldEnt,   mm1);

				for (int j=0; j < mentions.size(); j++) {
					if (i==j) continue;
					data.Mention mm2 = mentions.get(j);
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
			U.pf("%3d/%-3d  bad links\n", cluster_fp, cluster_fp+cluster_tp);
			pred_tp += cluster_tp;
			fp += cluster_fp;		}
		
		U.pl("\n***  Numbers  ***\n");
		U.pf("pred_tp=%-4d fp=%-4d  =>  Precision = %.3f\n", pred_tp, fp,  pred_tp*1.0/(pred_tp+fp));
		U.pf("gold_tp=%-4d fn=%-4d  =>  Recall = %.3f\n", gold_tp, fn,  gold_tp*1.0/(gold_tp+fn));
	}
}

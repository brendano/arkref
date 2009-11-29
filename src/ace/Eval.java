package ace;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
		
		
		U.pl("\n**   Real positives   **\n");
		for (AceDocument.Entity aceE : aceDoc.document.entities) {
			for (int i=0; i < aceE.mentions.size(); i++) {
				for (int j=i+1; j < aceE.mentions.size(); j++) {
					AceDocument.Mention am1 = aceE.mentions.get(i);
					AceDocument.Mention am2 = aceE.mentions.get(j);
					data.Mention mm1 = am1.myMention;
					data.Mention mm2 = am2.myMention;
					
					boolean match;
					if (mm1==null || mm2==null)
						match = false;
					else
						match = eg.mention2corefs.get(mm1).contains(mm2);
					
					U.pl("");
					U.pf("%-5s ACE [[%s]]   -vs-   [[%s]]\n", match ? "TP" : "FN",  am1, am2);
					U.pf("%-5s MyM ((%s))\n%-5s MyM ((%s))\n", " ", mm1, " ", mm2);
					
					if (match)
						gold_tp++;
					else
						fn++;
				}
				
			}
		}
		
		int pred_tp=0, fp=0;
		
//		U.pl("\n**  Predicted positives  **\n");
//		for (EntityGraph.Entity myE : eg.entities.toArray(new EntityGraph.Entity[0])) {
//			List<data.Mention> mentions = new ArrayList();
//			for (data.Mention m : myE.mentions)  mentions.add(m);
//			
//			for (int i=0; i < mentions.size(); i++) {
//				for (int j=i+1; j < mentions.size(); j++) {
//					data.Mention mm1 = mentions.get(i);
//					data.Mention mm2 = mentions.get(j);
//					
//					AceDocument.Mention am1 = LOOKUP mm1;
//					AceDocument.Mention am2 = LOOKUP mm2;
//					
//					boolean match;
//					if (am1==null || am2==null)
//						match = false;
//					else
//						match = am1.entity == am2.entity;
//					
//					U.pl("");
//					U.pf("%-5s ACE [[%s]]   -vs-   [[%s]]\n", match ? "TP" : "FN",  am1, am2);
//					U.pf("%-5s MyM ((%s))\n%-5s MyM ((%s))\n", " ", mm1, " ", mm2);
//					
//					if (match)
//						pred_tp++;
//					else
//						fp++;
//				}
//				
//			}
//
//		}
		
		U.pl("\n***  Numbers  ***\n");
		U.pf("pred_tp=%-4d fp=%-4d\n", pred_tp, fp);
		U.pf("gold_tp=%-4d fn=%-4d\n", gold_tp, fn);
		
		U.pf("Recall = %.3f\n", gold_tp*1.0 / (gold_tp + fn));
		
		

	}
}

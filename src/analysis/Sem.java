package analysis;

import java.util.*;

import parsestuff.U;
import data.CDB;
import data.Mention;

public class Sem {

	public static boolean haveNP(Mention mention) {
		// TODO headword?!
		return CDB.I.haveNP(mention.getHeadWord());
		// TODO Auto-generated method stub
//		return false;
	}
	
	public static String getNPString(Mention mention) {
		if (mention.aceMention != null) {
			return mention.aceMention.head.charseq.text;
		}
		return mention.getHeadWord(); // egads, usually too small.
	}

	public static boolean areCompatible(Mention mention, Mention cand) {
		
		String np1 = getNPString(mention);
		String np2 = getNPString(cand);
		Set<String> cs1 = CDB.I.getContexts(np1);
		Set<String> cs2 = CDB.I.getContexts(np2);
		double jacc = CDB.jaccard(cs1, cs2);
		U.pf("JACC %.3f    %-10s --- %s\n", jacc, np1, np2);
		return jacc > 0.5;
	}

}

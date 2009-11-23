package sent;

import java.io.FileNotFoundException;

import parsestuff.AnalysisUtilities;
import parsestuff.U;

public class StanfordSent {
	public static void main(String[] args) throws FileNotFoundException {
		String text = U.readFile(args[0]);
		for(String s : AnalysisUtilities.getInstance().getSentencesStanford(text)) {
			U.pl(s);
		}
	}
}

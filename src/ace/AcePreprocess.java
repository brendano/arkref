package ace;

import java.io.File;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import parsestuff.U;

public class AcePreprocess {
	public static void go(String apfFileName) throws IOException {
		String path = apfFileName.replace("_APF.XML", "").replace("_A.XML", "");
		String sgmlFilename = path + ".SGM";
		assert new File(sgmlFilename).exists();
		if (!analysis.Preprocess.alreadyPreprocessed(path)){
			String sgml = U.readFile(sgmlFilename);
			Pattern p = Pattern.compile("<TEXT>(.*)</TEXT>", Pattern.DOTALL);
			Matcher m = p.matcher(sgml);
			m.find();
			String text = m.group(1);
			U.writeFile(text, path + ".txt");
			analysis.Preprocess.go(path + ".txt");
		}
	}
	public static void main(String args[]) throws IOException {
		go(args[0]);
	}
}

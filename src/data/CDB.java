package data;
import java.util.*;
import java.util.regex.Pattern;
import java.io.*;

import org.apache.commons.lang.StringUtils;

import parsestuff.U;

public class CDB {
	public static String BASE = "/d/rtw/databig/categories";
	public static CDB I = new CDB();
	Map<String,Integer> npIndex;
	RandomAccessFile npContextsFile;
	static Pattern TAB = Pattern.compile("\t");
	static Pattern DASH= Pattern.compile("---");

	public CDB() {
		try {
			loadIndex();
			npContextsFile = new RandomAccessFile(BASE + "/cat_pairs_np-idx.txt", "r");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public boolean haveNP(String np) {
		return npIndex.containsKey(np);
	}
	public Set<String> getContexts(String np) {
		assert haveNP(np);
		
		int offset = npIndex.get(np);
		String line = null;
		try {
			npContextsFile.seek(offset);
			line = npContextsFile.readLine();
		} catch (IOException e) {
			e.printStackTrace();  System.exit(-1);
		}
		HashSet<String> contexts = new HashSet();
		for (String part : TAB.split(line)) {
			String[] ctx_count = DASH.split(part);
			contexts.add(ctx_count[0]);
		}
		return contexts;
	}
	private void loadIndex() throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(BASE + "/np2c.index"));
		String line;
		npIndex = new HashMap();
		while ( (line=  br.readLine()) != null) {
			String[]parts = line.split("\t");
			npIndex.put(parts[0], Integer.parseInt(parts[1]));
		}
	}

	public static <T> double jaccard(Set<T> s1, Set<T> s2) {
		Set<T> tmp = new HashSet();
		tmp.addAll(s1);
		tmp.addAll(s2);
		int x_or_y = tmp.size();
		tmp.retainAll(s1);
		tmp.retainAll(s2);
		int x_and_y = tmp.size();
		return x_and_y*1.0 / x_or_y;
	}
	
	
	

	
	public static void main(String[]args) throws Exception {
		String np1 = args[0];
		String np2 = args[1];
		Set<String> cs1 = I.getContexts(np1);
		Set<String> cs2 = I.getContexts(np2);
		
		U.pf("JACC %.3f\n", jaccard(cs1, cs2));
		
		U.writeFile(StringUtils.join(cs1, "\n"), "/tmp/tmp1");
		U.writeFile(StringUtils.join(cs2, "\n"), "/tmp/tmp2");
//		Runtime.getRuntime().exec("setvenn /tmp/tmp1 /tmp/tmp2");
		
//		for (String np : args) {
//			U.pl(np);
//			for (String c : CDB.I.getContexts(np)) {
//				U.pl (" CTX " + c);
//			}
//			
//		}
	}

}

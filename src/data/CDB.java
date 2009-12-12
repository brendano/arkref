package data;
import java.util.*;
import java.util.regex.Pattern;
import java.io.*;

import parsestuff.U;

public class CDB {
	public static String BASE = "/d/rtw/databig/categories";
	Map<String,Integer> npIndex;
	Map<String,Integer> contextCounts;
	RandomAccessFile npContextsFile;
	static Pattern TAB = Pattern.compile("\t");
	static Pattern DASH= Pattern.compile("---");

	private static CDB INSTANCE=null;
	public static CDB I() { if(INSTANCE==null) INSTANCE = new CDB(); return INSTANCE; }

	public CDB() {
		try {
			U.pf("loading CDB index/count files... ");
			npIndex = loadNumberFile(BASE + "/np2c.index");
			contextCounts=loadNumberFile(BASE + "/cat_contexts.txt");
			U.pf("done.\n");
			npContextsFile = new RandomAccessFile(BASE + "/cat_pairs_np-idx.txt", "r");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public boolean haveNP(String np) {
		return npIndex.containsKey(np);
	}
	public Set<String> getContexts(String np) {
		String line = getContextLine(np);
		HashSet<String> contexts = new HashSet();
		for (String part : TAB.split(line)) {
			String[] ctx_count = DASH.split(part);
			contexts.add(ctx_count[0]);
		}
		return contexts;
	}
	public String getContextLine(String np) {
		assert haveNP(np);
		
		int offset = npIndex.get(np);
		try {
			npContextsFile.seek(offset);
			return npContextsFile.readLine();
		} catch (IOException e) {
			e.printStackTrace();  System.exit(-1);
		}
		return null;
	}
	public FV getContextVector(String np) {
		String line = getContextLine(np);
		FV fv = new FV();
		String[]parts = TAB.split(line);
		for (int i=1; i < parts.length; i++) {
			String[] ctx_count = DASH.split(parts[i]);
			if (ctx_count.length != 2) {
				U.pl("BAD ENTRY: " + parts[i]);
				continue;
			}
			if (ctx_count[0].equals("1")) continue; //bug in data
			fv.map.put(ctx_count[0], Integer.parseInt(ctx_count[1]));
		}
		return fv;
	}

	private Map<String,Integer> loadNumberFile(String filename) throws IOException {
		Map<String,Integer> counts = new HashMap();
		BufferedReader br = new BufferedReader(new FileReader(filename));
		String line;
		while((line= br.readLine()) != null) {
			String[] parts = line.split("\t");
			counts.put(parts[0],Integer.parseInt(parts[1]));
		}
		return counts;
	}
	
	/** sparse feature vector, supporting some set-like semantics ... just for fun? **/
	public static class FV {
		public HashMap<String,Integer> map;
		public FV(){ map = new HashMap(); }
		
		public static Set<String> keyIntersect(FV fv1, FV fv2) {
			Set<String> keys = new HashSet();
			keys.addAll( fv1.map.keySet() );
			keys.addAll( fv2.map.keySet() );
			keys.retainAll( fv1.map.keySet() );
			keys.retainAll( fv2.map.keySet() );
			return keys;
		}
		public String toString() {
			String[] keys = map.keySet().toArray(new String[0]);
			sortBy(keys, new Scorer<String>(){
				@Override
				public double score(String key) {
					return -map.get(key);
				}});
			StringBuffer ret = new StringBuffer();
			int maxlen = 50;
			if (keys.length > maxlen)
				ret.append(U.sf("top %d of %d\n", maxlen, keys.length));
			else
				ret.append(U.sf("size %d\n", keys.length));
			for (int i=0; i < Math.min(maxlen, keys.length); i++) {
				String key = keys[i];
				ret.append(map.get(key) + " " + key);
				if (i < Math.min(maxlen,keys.length) - 1)  ret.append(" | ");
			}
			return ret.toString();
		}
		public static void pairReport(final FV fv1, FV fv2) {
			int max=50;
			String[]keys = keyIntersect(fv1,fv2).toArray(new String[0]);
			sortBy(keys, new Scorer<String>(){public double score(String k){ 
				return -fv1.map.get(k); }} );
			U.pf("%8s  %4s %4s\n", "global", "fv1c", "fv2c");
			U.pf("%8s  %4s %4s\n", "------", "----", "----");
			int i=0;
			for (String k : keys) {
				if (i++ > max) break;
				Integer c = I().contextCounts.get(k);
				String sc = c==null ? "null" : U.sf("%.1e", c*1.0);
				U.pf("%8s  %4d %4d | %s\n", sc, fv1.map.get(k), fv2.map.get(k), k);
			}
		}
	}
	
	/** like python sort(key=) or ruby sort_by() **/
	static <T> void sortBy(T[] arr, final Scorer<T> scorer) {
		Arrays.sort(arr, new Comparator() {
			@Override
			public int compare(Object i1, Object i2) {
				Double s1 = scorer.score((T) i1);
				Double s2 = scorer.score((T) i2);
				return s1.compareTo(s2);
			}
		});		
	}
	interface Scorer<T> { double score(T o); }
	
	/** like R order(), spiked with Python sort(key=) ... 
	 * returns parallel array for projecting through to get input array in sorted order
	 **/
	static <T> Integer[] order(final T[] arr, final Scorer<T> scorer) {
		Integer[] order = box(intRange(arr.length));
		Arrays.sort(order, new Comparator() {
			@Override
			public int compare(Object i1, Object i2) {
				Double s1 = scorer.score(arr[(Integer) i1]);
				Double s2 = scorer.score(arr[(Integer) i2]);
				return s1.compareTo(s2);
			}
		});
		return order;
	}
	/** like python range(N)  **/
	public static int[] intRange(int end) {
		int[] ret = new int[end];
		for (int i=0; i<end; i++)   ret[i] = i;
		return ret;		
	}
	static Integer[] box(int[] array) {
		int num = array.length;
		Integer[] boxed = new Integer[num];
		for (int ct=0; ct<num; ++ct) {
			boxed[ct] = Integer.valueOf(array[ct]);
		}
		return boxed;
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
		
		FV fv1 = I().getContextVector(np1);
		U.pl(fv1);

		FV fv2 = I().getContextVector(np2);
		U.pl(fv2);
		
		FV.pairReport(fv1,fv2);

//		Set<String> cs1 = I.getContexts(np1);
//		Set<String> cs2 = I.getContexts(np2);
//		
//		U.pf("JACC %.3f\n", jaccard(cs1, cs2));
//		
//		U.writeFile(StringUtils.join(cs1, "\n"), "/tmp/tmp1");
//		U.writeFile(StringUtils.join(cs2, "\n"), "/tmp/tmp2");

		
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

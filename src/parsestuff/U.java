package parsestuff;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

public class U {
	public static <T> void pl(T s) { System.out.println(s); }
	public static <A> void pf(String pat, A a0) {  System.out.printf(pat, a0);  }
	public static <A,B> void pf(String pat, A a0, B a1) {  System.out.printf(pat, a0, a1);  }
	public static <A,B,C> void pf(String pat, A a0, B a1, C a2) {  System.out.printf(pat, a0, a1, a2);  }
	public static <A,B,C,D> void pf(String pat, A a0, B a1, C a2, D a3) {  System.out.printf(pat, a0, a1, a2, a3);  }
	public static <A,B,C,D,E> void pf(String pat, A a0, B a1, C a2, D a3, E a4) {  System.out.printf(pat, a0, a1, a2, a3, a4);  }
	public static <A,B,C,D,E,F> void pf(String pat, A a0, B a1, C a2, D a3, E a4, F a5) {  System.out.printf(pat, a0, a1, a2, a3, a4, a5);  }
	public static <A,B,C,D,E,F,G> void pf(String pat, A a0, B a1, C a2, D a3, E a4, F a5, G a6) {  System.out.printf(pat, a0, a1, a2, a3, a4, a5, a6);  }
	public static <A,B,C,D,E,F,G,H> void pf(String pat, A a0, B a1, C a2, D a3, E a4, F a5, G a6, H a7) {  System.out.printf(pat, a0, a1, a2, a3, a4, a5, a6, a7);  }
	public static <A,B,C,D,E,F,G,H,I> void pf(String pat, A a0, B a1, C a2, D a3, E a4, F a5, G a6, H a7, I a8) {  System.out.printf(pat, a0, a1, a2, a3, a4, a5, a6, a7, a8);  }
	public static <A,B,C,D,E,F,G,H,I,J> void pf(String pat, A a0, B a1, C a2, D a3, E a4, F a5, G a6, H a7, I a8, J a9) {  System.out.printf(pat, a0, a1, a2, a3, a4, a5, a6, a7, a8, a9);  }
	public static <A,B,C,D,E,F,G,H,I,J,K> void pf(String pat, A a0, B a1, C a2, D a3, E a4, F a5, G a6, H a7, I a8, J a9, K a10) {  System.out.printf(pat, a0, a1, a2, a3, a4, a5, a6, a7, a8, a9, a10);  }
	public static <A,B,C,D,E,F,G,H,I,J,K,L> void pf(String pat, A a0, B a1, C a2, D a3, E a4, F a5, G a6, H a7, I a8, J a9, K a10, L a11) {  System.out.printf(pat, a0, a1, a2, a3, a4, a5, a6, a7, a8, a9, a10, a11);  }
	public static <A,B,C,D,E,F,G,H,I,J,K,L,M> void pf(String pat, A a0, B a1, C a2, D a3, E a4, F a5, G a6, H a7, I a8, J a9, K a10, L a11, M a12) {  System.out.printf(pat, a0, a1, a2, a3, a4, a5, a6, a7, a8, a9, a10, a11, a12);  }
	public static <A,B,C,D,E,F,G,H,I,J,K,L,M,N> void pf(String pat, A a0, B a1, C a2, D a3, E a4, F a5, G a6, H a7, I a8, J a9, K a10, L a11, M a12, N a13) {  System.out.printf(pat, a0, a1, a2, a3, a4, a5, a6, a7, a8, a9, a10, a11, a12, a13);  }
	public static <A,B,C,D,E,F,G,H,I,J,K,L,M,N,O> void pf(String pat, A a0, B a1, C a2, D a3, E a4, F a5, G a6, H a7, I a8, J a9, K a10, L a11, M a12, N a13, O a14) {  System.out.printf(pat, a0, a1, a2, a3, a4, a5, a6, a7, a8, a9, a10, a11, a12, a13, a14);  }
	public static <A,B,C,D,E,F,G,H,I,J,K,L,M,N,O,P> void pf(String pat, A a0, B a1, C a2, D a3, E a4, F a5, G a6, H a7, I a8, J a9, K a10, L a11, M a12, N a13, O a14, P a15) {  System.out.printf(pat, a0, a1, a2, a3, a4, a5, a6, a7, a8, a9, a10, a11, a12, a13, a14, a15);  }
	public static <A,B,C,D,E,F,G,H,I,J,K,L,M,N,O,P,Q> void pf(String pat, A a0, B a1, C a2, D a3, E a4, F a5, G a6, H a7, I a8, J a9, K a10, L a11, M a12, N a13, O a14, P a15, Q a16) {  System.out.printf(pat, a0, a1, a2, a3, a4, a5, a6, a7, a8, a9, a10, a11, a12, a13, a14, a15, a16);  }
	public static <A,B,C,D,E,F,G,H,I,J,K,L,M,N,O,P,Q,R> void pf(String pat, A a0, B a1, C a2, D a3, E a4, F a5, G a6, H a7, I a8, J a9, K a10, L a11, M a12, N a13, O a14, P a15, Q a16, R a17) {  System.out.printf(pat, a0, a1, a2, a3, a4, a5, a6, a7, a8, a9, a10, a11, a12, a13, a14, a15, a16, a17);  }
	public static <A,B,C,D,E,F,G,H,I,J,K,L,M,N,O,P,Q,R,S> void pf(String pat, A a0, B a1, C a2, D a3, E a4, F a5, G a6, H a7, I a8, J a9, K a10, L a11, M a12, N a13, O a14, P a15, Q a16, R a17, S a18) {  System.out.printf(pat, a0, a1, a2, a3, a4, a5, a6, a7, a8, a9, a10, a11, a12, a13, a14, a15, a16, a17, a18);  }
	public static <A,B,C,D,E,F,G,H,I,J,K,L,M,N,O,P,Q,R,S,T> void pf(String pat, A a0, B a1, C a2, D a3, E a4, F a5, G a6, H a7, I a8, J a9, K a10, L a11, M a12, N a13, O a14, P a15, Q a16, R a17, S a18, T a19) {  System.out.printf(pat, a0, a1, a2, a3, a4, a5, a6, a7, a8, a9, a10, a11, a12, a13, a14, a15, a16, a17, a18, a19);  }


	public static String readFile(String filename) throws FileNotFoundException { 
		File file = new File(filename);
		return new Scanner(file).useDelimiter("\\Z").next();
	}
	public static void writeFile(String text, String file) throws IOException {
		writeFile(text, new File(file));
	}

	public static void writeFile(String text, File file) throws IOException {
		FileWriter fw = new FileWriter(file);
		fw.write(text);
		fw.close();
	}
}

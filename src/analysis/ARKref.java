package analysis;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.Properties;

import data.Document;

public class ARKref {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		Properties properties = new Properties();
		try{
			properties.load(new FileInputStream("config/arkref.properties"));
		}catch(Exception e){
			e.printStackTrace();
		}
		
		String inputFile = null;
		
		int i=0;
		while(i<args.length){
			if(args[i].equals("--inputFile")){
				inputFile = args[i+1];
				i++;
			}
			i++;
		}
		
		BufferedReader br;
		if(inputFile == null){
			br = new BufferedReader(new InputStreamReader(System.in));
		}else{
			br = new BufferedReader(new InputStreamReader(new FileInputStream(inputFile)));
		}
		String buf;
		String doc;
		
		while(true){
			doc = "";
			buf = "";
			
			//wait to read the first line
			buf = br.readLine();
			if(buf == null){
				break;
			}
			doc += buf;
			
			//read the rest of the input
			while(br.ready()){
				buf = br.readLine();
				if(buf == null){
					break;
				}
				doc += buf + " ";
			}
			if(doc.length() == 0){
				break;
			}
			
			//print the input to a temporary file
			File sentFile = File.createTempFile("ARKrefTempFile", ".sent");
			sentFile.deleteOnExit();
			PrintWriter pw = new PrintWriter(new FileOutputStream(sentFile));
			pw.println(doc);
			pw.close();
			
			//process the document
			Preprocess.go(sentFile.getAbsolutePath(), true);
			Document d = Document.loadFiles(sentFile.getAbsolutePath());
			_Pipeline.go(d);
			
		}
		

	}

}

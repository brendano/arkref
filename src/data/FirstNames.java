package data;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * For identifying the genders of given names.
 * Uses US Census data from 
 * 
 *	http://www.census.gov/genealogy/names/dist.female.first
 *	http://www.census.gov/genealogy/names/dist.male.first
 * 
 * @author Michael Heilman
 *
 */
public class FirstNames {

	private Map<String, Integer> genderMap;
	public static final int GENDER_UNKNOWN = 0;
	public static final int GENDER_MALE = 1;
	public static final int GENDER_FEMALE = 2;
	
	private static FirstNames instance;

	
	private FirstNames(){
		genderMap = new HashMap<String, Integer>();
		
		
		Properties properties = new Properties();
		try{
			properties.load(new FileInputStream("config/arkref.properties"));
		}catch(Exception e){
			e.printStackTrace();
		}
		String maleNamesPath = properties.getProperty("maleFirstNamesFile", "config/dist.male.first");
		String femaleNamesPath = properties.getProperty("femaleFirstNamesFile", "config/dist.female.first");
		
		
		//load U.S. census data 
		
		//Temporarily keep frequencies of male names to
		//make decisions about ambiguous names.
		Map<String, Double> maleFrequencies = loadNameFrequencies(maleNamesPath);
		Map<String, Double> femaleFrequencies = loadNameFrequencies(femaleNamesPath);
		
		//add male names
		for(Map.Entry<String, Double> entry: maleFrequencies.entrySet()){
			genderMap.put(entry.getKey(), FirstNames.GENDER_MALE);
		}
		
		//add female names, check frequencies for ambiguous names
		String name;
		Double freq;
		for(Map.Entry<String, Double> entry: femaleFrequencies.entrySet()){
			name = entry.getKey();
			freq = entry.getValue();
			
			if(maleFrequencies.get(name) == null || maleFrequencies.get(name) < freq){
				genderMap.put(name, FirstNames.GENDER_FEMALE);
			}
		}
		
	}

	private Map<String, Double> loadNameFrequencies(String path){
		Map<String, Double> res = new HashMap<String, Double>();
		
		String buf;
		String [] parts;
		String name;
		Double freq;
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(path)));
			while((buf=br.readLine()) != null){
				parts = buf.split("\\s+");
				name = parts[0].toLowerCase();
				freq = new Double(parts[1]);
				
				res.put(name, freq);
			}
			
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return res;
	}
	

	public static FirstNames getInstance() {
		if(instance == null){
			instance = new FirstNames();
		}
		return instance;
	}


	public Set<String> getMaleNames(){
		Set<String> res = new HashSet<String>();
		
		for(Map.Entry<String, Integer> entry: genderMap.entrySet()){
			if(entry.getValue() == FirstNames.GENDER_MALE){
				res.add(entry.getKey());
			}
		}
		
		return res;
	}
	
	public Set<String> getFemaleNames(){
		Set<String> res = new HashSet<String>();
		
		for(Map.Entry<String, Integer> entry: genderMap.entrySet()){
			if(entry.getValue() == FirstNames.GENDER_FEMALE){
				res.add(entry.getKey());
			}
		}
		
		return res;
	}
	
	public Set<String> getAllFirstNames(){
		Set<String> res = new HashSet<String>();
		
		for(Map.Entry<String, Integer> entry: genderMap.entrySet()){
			res.add(entry.getKey());
		}
		
		return res;
	}


	public int getGender(String name) {
		int res;
		Integer gender = genderMap.get(name.toLowerCase());
		if(gender == null){
			res = GENDER_UNKNOWN;
		}else{
			res = gender;
		}
		
		return res;
	}

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {	
		String buf;
		String genderS;
		
		//pre-load
		FirstNames.getInstance();
		
		System.err.println("Type names on standard input...");
		
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		while((buf = br.readLine()) != null){
			buf = buf.trim();
			int gender = FirstNames.getInstance().getGender(buf);
			if(gender == FirstNames.GENDER_MALE){
				genderS = "male";
			}else if (gender == FirstNames.GENDER_FEMALE){
				genderS = "female";
			}else{
				genderS = "unknown";
			}
			
			System.out.println(genderS);
		}

	}

}

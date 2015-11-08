package trabajo;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

public class Dictionare {
	
	public Map<String, Hashtable<String,Integer> > map;
	
	/**
	 * Introduce pares de objetos key-value
	 * 
	 */
	public Dictionare(){
		map = new HashMap<String,  Hashtable<String,Integer>>();
		fillIdentifier();
		fillNameEntities();
	}
	
	/**
	 * Introduce nombres propios en el diccionario
	 */
	private void fillNameEntities(){
		Hashtable<String, Integer> nameEntities = new Hashtable<String,Integer>();
		try {
			int i = 0;
			for (String line : Files.readAllLines(Paths.get("files/names.txt"))) {
			    nameEntities.put(line.trim(),i);
			    i++;
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		map.put("author", nameEntities);
		
	}
	
	private void fillIdentifier(){
		Hashtable<String, Integer> identifiers = new Hashtable<String,Integer>();
		identifiers.put("TESIS",0);
		identifiers.put("TFG",1);
		identifiers.put("TFM",2);
		identifiers.put("PFC",3);
		identifiers.put("trabajo de fin de grado",4);
		identifiers.put("trabajo de fin de master",5);
		identifiers.put("proyecto de fin de carrera",6);
		map.put("identifier",identifiers);
	}

}

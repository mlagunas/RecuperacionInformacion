package trabajo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Dictionare {
	
	public Map<String, List<String>> map;
	
	/**
	 * Introduce pares de objetos key-value
	 * 
	 */
	public Dictionare(){
		map = new HashMap<String, List<String>>();
		fillIdentifier();
		fillNameEntities();
	}
	
	/**
	 * Introduce nombres propios en el diccionario
	 */
	private void fillNameEntities(){
		List<String> nameEntities = new ArrayList<String>();
		nameEntities.add("Javier");
		nameEntities.add("Manuel");
		nameEntities.add("Daniel");
		map.put("author", nameEntities);
	}
	
	private void fillIdentifier(){
		List<String> identifiers = new ArrayList<String>();
		identifiers.add("TESIS");
		identifiers.add("TFG");
		identifiers.add("TFM");
		identifiers.add("PFC");
		map.put("identifier",identifiers);
	}

}

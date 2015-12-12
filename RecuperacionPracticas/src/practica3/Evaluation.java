package practica3;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.TreeMap;

public class Evaluation {

	// Path a los ficheros que usamos
	private static final String QRELS = "src/practica3/files/qrels.txt";
	private static final String RESULT = "src/practica3/files/results.txt";

	// Arraylist con los documentos Relevantes(R) y no relevantes(NR)
	private static HashMap<String, ArrayList<String>> qrelsR = new HashMap<String, ArrayList<String>>();
	private static HashMap<String, ArrayList<String>> qrelsNR = new HashMap<String, ArrayList<String>>();

	// Arraylist con pares necesidad - lista de ids
	private static HashMap<String, ArrayList<String>> resultRead = new HashMap<String, ArrayList<String>>();

	public static void main(String[] args) {
		readFiles();
	}

	public static double precision(int need,
			HashMap<String, ArrayList<String>> rel,
			HashMap<String, ArrayList<String>> recu) {
		int nRR = cuentaDeIntersección(rel.get(need), recu.get(need)); // num
																		// Relevantes
		// Recuperados
		int docR = recu.get(need).size(); // doc Recuperados
		return nRR / docR;
	}

	public static double recall(int need,
			HashMap<String, ArrayList<String>> rel,
			HashMap<String, ArrayList<String>> recu) {
		int nRR = cuentaDeIntersección(rel.get(need), recu.get(need)); // num
																		// Relevantes
		// Recuperados
		int nR = rel.get(need).size();// num Relevantes
		return nRR / nR;
	}

	public static double F1(double precision, double recall, int beta) {
		return ((beta * beta + 1) * precision * recall)
				/ ((beta * beta) * precision + recall);
	}

	public static double kPrecision(int k, int need,
			HashMap<String, ArrayList<String>> rel,
			HashMap<String, ArrayList<String>> recu) {
		int nR = cuentaDeIntersección(rel.get(need), (ArrayList<String>) recu
				.get(need).subList(0, k));// Numero de relevantes en coleccion
											// [0,..,k]
		return nR / k;
	}
	
	public static double kRecall(int k, int need,
			HashMap<String, ArrayList<String>> rel,
			HashMap<String, ArrayList<String>> recu) {
		int nR = cuentaDeIntersección(rel.get(need), (ArrayList<String>) recu
				.get(need).subList(0, k));// Numero de relevantes en coleccion
											// [0,..,k]
		int nRTotal = rel.get(need).size();// num Relevantes

		return nR / nRTotal;
	}

	public static double averagePrecision(int need,
			HashMap<String, ArrayList<String>> rel,
			HashMap<String, ArrayList<String>> recu) {
		ArrayList<String> recuperados = recu.get(need);
		ArrayList<String> relevantes = rel.get(need);
		int precisionFinal = 0;
		for(int i = 0; i < recuperados.size();i++){
			if(relevantes.contains(recuperados.get(i))){
				precisionFinal += kPrecision(i, need, rel, recu);
			}
		}
		return precisionFinal/cuentaDeIntersección(relevantes,recuperados);
	}
	
	public static TreeMap<Double,Double> recall_precision(int need,
			HashMap<String, ArrayList<String>> rel,
			HashMap<String, ArrayList<String>> recu) {
		ArrayList<String> recuperados = recu.get(need);
		ArrayList<String> relevantes = rel.get(need);
		TreeMap<Double,Double> points = new TreeMap<Double, Double>();
		for(int i = 0; i < recuperados.size();i++){
			if(relevantes.contains(recuperados.get(i))){
				double precision=kPrecision(i, need, rel, recu);
				double recall=kRecall(i, need, rel, recu);
				points.put(precision, recall);
			}
		}
		for(Entry<Double, Double> entry : points.entrySet()) {
			  Double key = entry.getKey();
			  Double value = entry.getValue();

			  System.out.println(key + " => " + value);
			}
		return points;
	}

	public static HashMap<Double,Double>  interpolatedRecallPrecision() {
		return null;
	}

	private static void readFiles() {
		try {
			// Estructuras auxiliares para guardar informacion
			String lastNeed = "";
			ArrayList<String> R = new ArrayList<String>();
			ArrayList<String> nR = new ArrayList<String>();

			// Iterar en el fichero
			for (String line : Files.readAllLines(Paths.get(QRELS), Charset.defaultCharset())) {
				Scanner s = new Scanner(line);

				// INFO_NEED, DOC_ID, RELEVANCY
				String need = s.next(), id = s.next(), relevancy = s.next();
				// Si la necesidad cambia -> almacenar ids de relevantes y no
				// relevantes
				if (lastNeed != need) {
					qrelsR.put(lastNeed, R);
					qrelsNR.put(lastNeed, nR);
					R.clear();
					lastNeed = need;
				} else {
					// Almacenar en el hashTable segun relevancia
					if (relevancy == "1")
						R.add(id);
					else
						nR.add(id);
				}
				s.close();
			}
			lastNeed = "";
			ArrayList<String> ids = new ArrayList<String>();

			for (String line : Files.readAllLines(Paths.get(RESULT),Charset.defaultCharset())) {
				Scanner s = new Scanner(line);
				// INFO_NEED, DOC_ID
				String need = s.next(), id = s.next();
				if (lastNeed != need) {
					resultRead.put(lastNeed, ids);
					ids.clear();
					lastNeed = need;
				} else {
					ids.add(id);
				}
				s.close();
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static int cuentaDeIntersección(ArrayList<String> rel,
			ArrayList<String> recu) {
		int count = 0;
		for (String id : rel) {
			if (recu.contains(id)) {
				count++;
			}
		}
		return count;
	}
}

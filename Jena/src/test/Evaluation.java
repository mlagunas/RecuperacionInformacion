package test;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.TreeMap;

public class Evaluation {

	// Path a los ficheros que usamos
	private static final String QRELS = "src/zaguanRels.txt";
	private static final String RESULT = "resultado.txt";

	private static final double beta = 1;

	// List con los documentos Relevantes(R) y no relevantes(NR)
	private static HashMap<String, List<String>> qrelsR = new HashMap<String, List<String>>();
	private static HashMap<String, List<String>> qrelsNR = new HashMap<String, List<String>>();

	// List con pares necesidad - lista de ids
	private static HashMap<String, List<String>> docRecup = new HashMap<String, List<String>>();

	public static void main(String[] args) {
		readFiles();
		for (String need : qrelsR.keySet()) {
			calculateAndPrint(need);
		}
		calculateAndPrint("-1");
	}

	public static double precision(String need,
			HashMap<String, List<String>> rel,
			HashMap<String, List<String>> recu) {
		List<String> recuperados = new ArrayList<String>();
		List<String> relevantes = new ArrayList<String>();

		if (need.equals("-1")) {
			for (String n : rel.keySet()) {
				relevantes.addAll(rel.get(n));
			}

			for (String n : recu.keySet()) {
				recuperados.addAll(recu.get(n));
			}
		} else {
			relevantes.addAll(rel.get(need));
			recuperados.addAll(recu.get(need));
		}
		// numRelevantesRecuperados
		double nRR = cuentaDeInterseccion(relevantes, recuperados);
		// Recuperados
		double docR = recuperados.size(); // doc Recuperados
		if(nRR==0) {
			return 0;
		}
		return nRR / docR;
	}

	public static double recall(String need, HashMap<String, List<String>> rel,
			HashMap<String, List<String>> recu) {
		List<String> recuperados = new ArrayList<String>();
		List<String> relevantes = new ArrayList<String>();

		if (need.equals("-1")) {
			for (String n : rel.keySet()) {
				relevantes.addAll(rel.get(n));
			}

			for (String n : recu.keySet()) {
				recuperados.addAll(recu.get(n));
			}
		} else {
			relevantes.addAll(rel.get(need));
			recuperados.addAll(recu.get(need));
		}
		// num Relevantes Recuperados
		double nRR = cuentaDeInterseccion(relevantes, recuperados);
		double nR = relevantes.size();// num Relevantes
		return nRR / nR;
	}

	public static double F1(double precision, double recall, double beta) {
		if (precision==0)
			return 0;
		return round(((beta * beta + 1) * precision * recall)
				/ ((beta * beta) * precision + recall), 3);
	}


	/*
	 * METODOS PRIVADOS
	 */

	private static void calculateAndPrint(String need) {
		double precision = precision(need, qrelsR, docRecup);
		double recall = recall(need, qrelsR, docRecup);
		double f1 = F1(precision, recall, beta);
		if(!need.equals("-1"))
		System.out.println("NECESIDAD : " + need + "\n"
				+ "=====================");
		else
			System.out.println("NECESIDAD : TOTAL \n"
				+ "=====================");

		System.out.println("Precision " + precision);
		System.out.println("Recall " + recall);
		System.out.println("F1 " + f1);
		System.out.println();
	}

	private static void readFiles() {
		try {
			// Estructuras auxiliares para guardar informacion
			String lastNeed = null;
			List<String> R = new ArrayList<String>();
			List<String> nR = new ArrayList<String>();

			// Iterar en el fichero
			for (String line : Files.readAllLines(Paths.get(QRELS),
					Charset.defaultCharset())) {
				Scanner s = new Scanner(line);

				// INFO_NEED, DOC_ID, RELEVANCY
				String need = s.next();
				String id = s.next();
				String relevancy = s.next();
				// Si la necesidad cambia -> almacenar ids de relevantes y no
				// relevantes
				if (lastNeed != null && !lastNeed.equals(need)) {
					qrelsR.put(lastNeed, R);
					qrelsNR.put(lastNeed, nR);
					R = new ArrayList<String>();
					nR = new ArrayList<String>();
					lastNeed = need;
				}
				// Almacenar en el hashTable segun relevancia
				if (relevancy.equals("1"))
					R.add(id);
				else
					nR.add(id);
				lastNeed = need;

				s.close();
			}
			qrelsR.put(lastNeed, R);
			qrelsNR.put(lastNeed, nR);

			lastNeed = null;
			List<String> ids = new ArrayList<String>();

			for (String line : Files.readAllLines(Paths.get(RESULT),
					Charset.defaultCharset())) {
				Scanner s = new Scanner(line);
				// INFO_NEED, DOC_ID
				String need = s.next();
				String id = s.next();
				if (lastNeed != null && !lastNeed.equals(need)) {
					docRecup.put(lastNeed, ids);
					ids = new ArrayList<String>();
					lastNeed = need;
				}
				ids.add(id);
				lastNeed = need;

				s.close();
			}
			docRecup.put(lastNeed, ids);

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static double cuentaDeInterseccion(List<String> rel,
			List<String> recu) {
		double count = 0;
		for (String id : rel) {
			if (recu.contains(id)) {
				count++;
			}
		}
		return count;
	}

	private static double round(double value, int places) {
		if (places < 0)
			throw new IllegalArgumentException();

		BigDecimal bd = new BigDecimal(value);
		bd = bd.setScale(places, RoundingMode.HALF_UP);
		return bd.doubleValue();
	}
}

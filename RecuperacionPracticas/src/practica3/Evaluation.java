package practica3;

import java.io.IOException;
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
	private static final String QRELS = "src/practica3/files/qrels.txt";
	private static final String RESULT = "src/practica3/files/results.txt";

	private static final double beta = 1;

	// List con los documentos Relevantes(R) y no relevantes(NR)
	private static HashMap<String, List<String>> qrelsR = new HashMap<String, List<String>>();
	private static HashMap<String, List<String>> qrelsNR = new HashMap<String, List<String>>();

	// List con pares necesidad - lista de ids
	private static HashMap<String, List<String>> docRecup = new HashMap<String, List<String>>();

	public static void main(String[] args) {
		readFiles();
		for (String need : qrelsR.keySet()) {
			double precision = precision(need, qrelsR, docRecup);
			double recall = recall(need, qrelsR, docRecup);
			double f1 = F1(precision, recall, beta);
			double prec_10 = kPrecision(10, need, qrelsR, docRecup);
			double averagePrec = averagePrecision(need, qrelsR, docRecup);

			System.out.println("NECESIDAD : " + need +"\n" +
							   "=====================");

			System.out.println("Precision " + precision);
			System.out.println("Recall " + recall);
			System.out.println("F1 " + f1);
			System.out.println("Precision@10 " + prec_10);
			System.out.println("Average precision " + averagePrec);

			System.out.println();

		}

	}

	public static double precision(String need,
			HashMap<String, List<String>> rel,
			HashMap<String, List<String>> recu) {
		double nRR = cuentaDeIntersección(rel.get(need), recu.get(need)); // num
																			// Relevantes
																			// Recuperados
		// Recuperados
		double docR = recu.get(need).size(); // doc Recuperados
		return nRR / docR;
	}

	public static double recall(String need,
			HashMap<String, List<String>> rel,
			HashMap<String, List<String>> recu) {
		double nRR = cuentaDeIntersección(rel.get(need), recu.get(need)); // num
																			// Relevantes
		// Recuperados
		double nR = rel.get(need).size();// num Relevantes
		return nRR / nR;
	}

	public static double F1(double precision, double recall, double beta) {
		return ((beta * beta + 1) * precision * recall)
				/ ((beta * beta) * precision + recall);
	}

	public static double kPrecision(int k, String need,
			HashMap<String, List<String>> rel,
			HashMap<String, List<String>> recu) {
		double nR = cuentaDeIntersección(rel.get(need),
				recu.get(need).subList(0, k));// Numero de
												// relevantes
												// en
												// coleccion
												// [0,..,k]
		return nR / k;
	}

	public static double kRecall(int k, String need,
			HashMap<String, List<String>> rel,
			HashMap<String, List<String>> recu) {
		double nR = cuentaDeIntersección(rel.get(need),
				(List<String>) recu.get(need).subList(0, k));// Numero de
																	// relevantes
																	// en
																	// coleccion
																	// [0,..,k]
		int nRTotal = rel.get(need).size();// num Relevantes

		return nR / nRTotal;
	}

	public static double averagePrecision(String need,
			HashMap<String, List<String>> rel,
			HashMap<String, List<String>> recu) {
		List<String> recuperados = recu.get(need);
		List<String> relevantes = rel.get(need);
		double precisionFinal = 0;
		for (int i = 0; i < recuperados.size(); i++) {
			if (relevantes.contains(recuperados.get(i))) {
				precisionFinal += kPrecision(i+1, need, rel, recu);
			}
		}
		return (precisionFinal / cuentaDeIntersección(relevantes, recuperados));
	}

	public static TreeMap<Double, Double> recall_precision(String need,
			HashMap<String, List<String>> rel,
			HashMap<String, List<String>> recu) {
		List<String> recuperados = recu.get(need);
		List<String> relevantes = rel.get(need);
		TreeMap<Double, Double> points = new TreeMap<Double, Double>();
		for (int i = 0; i < recuperados.size(); i++) {
			if (relevantes.contains(recuperados.get(i))) {
				double precision = kPrecision(i, need, rel, recu);
				double recall = kRecall(i, need, rel, recu);
				points.put(precision, recall);
			}
		}
		for (Entry<Double, Double> entry : points.entrySet()) {
			Double key = entry.getKey();
			Double value = entry.getValue();

			System.out.println(key + " => " + value);
		}
		return points;
	}

	public static HashMap<Double, Double> interpolatedRecallPrecision() {
		return null;
	}

	/*
	 * METODOS PRIVADOS
	 */
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

	private static double cuentaDeIntersección(List<String> rel,
			List<String> recu) {
		double count = 0;
		for (String id : rel) {
			if (recu.contains(id)) {
				count++;
			}
		}
		return count;
	}
}

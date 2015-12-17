package practica3;

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
	private static final String QRELS = "src/practica3/files/qrels.txt";
	private static final String RESULT = "src/practica3/files/results.txt";
	private static final double BETA = 1;

	// List con los documentos Relevantes(R) y no relevantes(NR)
	private static HashMap<String, List<String>> qrelsR = new HashMap<String, List<String>>();
	private static HashMap<String, List<String>> qrelsNR = new HashMap<String, List<String>>();

	// List con pares necesidad - lista de ids
	private static HashMap<String, List<String>> docRecup = new HashMap<String, List<String>>();

	// Variables que almacenan los resultados acumulados
	private static Double[] resultados = new Double[] { 0.0, 0.0, 0.0, 0.0, 0.0 };
	private static TreeMap<Double, Double> interpolatedTotal = new TreeMap<Double, Double>();

	/**
	 * main de ejemplo para la practica3
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		readFiles();
		for (String need : qrelsR.keySet()) {
			calculateAndWrite(need);
		}
		calculateAndWrite("-1");
	}

	/**
	 * Calculo de la precision
	 * 
	 * @param need
	 * @param rel
	 * @param recu
	 * @return
	 */
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

	/**
	 * Calculo del recall
	 * 
	 * @param need
	 * @param rel
	 * @param recu
	 * @return
	 */
	public static double recall(String need, HashMap<String, List<String>> rel,
			HashMap<String, List<String>> recu) {
		double nRR = cuentaDeIntersección(rel.get(need), recu.get(need)); // num
																			// Relevantes
		// Recuperados
		double nR = rel.get(need).size();// num Relevantes
		return nRR / nR;
	}

	/**
	 * Calculo de F1
	 * 
	 * @param precision
	 * @param recall
	 * @param beta
	 * @return
	 */
	public static double F1(double precision, double recall, double beta) {
		return round(((beta * beta + 1) * precision * recall)
				/ ((beta * beta) * precision + recall), 3);
	}

	/**
	 * Calculo de la precision para k elementos. 0--k
	 * 
	 * @param k
	 * @param need
	 * @param rel
	 * @param recu
	 * @return
	 */
	public static double kPrecision(int k, String need,
			HashMap<String, List<String>> rel,
			HashMap<String, List<String>> recu) {
		double nR = cuentaDeIntersección(rel.get(need),
				recu.get(need).subList(0, k));// Numero de
												// relevantes
												// en
												// coleccion
												// [0,..,k]
		return round(nR / k, 3);
	}

	/**
	 * Calcullo del recall para k elementos. 0--k
	 * 
	 * @param k
	 * @param need
	 * @param rel
	 * @param recu
	 * @return
	 */
	public static double kRecall(int k, String need,
			HashMap<String, List<String>> rel,
			HashMap<String, List<String>> recu) {
		double nR = cuentaDeIntersección(rel.get(need), (List<String>) recu
				.get(need).subList(0, k));// Numero de
											// relevantes
											// en
											// coleccion
											// [0,..,k]
		int nRTotal = rel.get(need).size();// num Relevantes
		return round(nR / nRTotal, 3);
	}

	/**
	 * Calculo de precision media
	 * 
	 * @param need
	 * @param rel
	 * @param recu
	 * @return
	 */
	public static double averagePrecision(String need,
			HashMap<String, List<String>> rel,
			HashMap<String, List<String>> recu) {
		List<String> recuperados = recu.get(need);
		List<String> relevantes = rel.get(need);
		double precisionFinal = 0;
		for (int i = 0; i < recuperados.size(); i++) {
			if (relevantes.contains(recuperados.get(i))) {
				precisionFinal += kPrecision(i + 1, need, rel, recu);
			}
		}
		return (precisionFinal / cuentaDeIntersección(relevantes, recuperados));
	}

	/**
	 * Calculo de los puntos de la grafica recall-precision
	 *
	 * @param need
	 * @param rel
	 * @param recu
	 * @return
	 */
	public static TreeMap<Double, Double> recall_precision(String need,
			HashMap<String, List<String>> rel,
			HashMap<String, List<String>> recu) {
		List<String> recuperados = recu.get(need);
		List<String> relevantes = rel.get(need);
		TreeMap<Double, Double> points = new TreeMap<Double, Double>();
		for (int i = 0; i < recuperados.size(); i++) {
			if (relevantes.contains(recuperados.get(i))) {
				double precision = kPrecision(i + 1, need, rel, recu);
				double recall = kRecall(i + 1, need, rel, recu);
				points.put(recall, precision);
			}
		}
		return points;
	}

	/**
	 * Calculo de los puntos de la grafica recall-precision interpolados.
	 * 
	 * @param need
	 * @param rel
	 * @param recu
	 * @return
	 */
	public static TreeMap<Double, Double> interpolated_recall_precision(
			String need, HashMap<String, List<String>> rel,
			HashMap<String, List<String>> recu) {

		TreeMap<Double, Double> recall_precision = recall_precision(need, rel,
				recu);
		TreeMap<Double, Double> points = new TreeMap<Double, Double>();
		for (double i = 0.0; i <= 1.0; i += 0.1) {
			double maxValue = -1;
			for (Entry<Double, Double> entry : recall_precision.entrySet()) {
				double value = entry.getValue();
				if (entry != null && entry.getKey() >= i && value > maxValue) {
					maxValue = value;
				}
			}
			if (maxValue != -1) {
				points.put(round(i, 1), maxValue);
			}
		}
		return points;
	}

	/*
	 * METODOS PRIVADOS
	 */

	/**
	 * Metodo encargado de dadas dos direcciones de ficheros rellenar
	 * respectivos HashMap con su información a cerca de los ficheros
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

	/**
	 * Cuenta elementos relevantes y recuperados, la interseccion de ambas
	 * listas
	 * 
	 * @param rel
	 * @param recu
	 * @return
	 */
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

	/**
	 * Metodo que recibe un número de necesidad o -1 en caso de hacer referencia
	 * a todas las necesidades y muestra por pantalla sus resultados con las
	 * diferentes medidas de evaluacion
	 * 
	 * @param need
	 */
	private static void calculateAndWrite(String need) {
		double precision;
		double recall;
		double f1;
		double prec_10;
		double averagePrec;
		TreeMap<Double, Double> points = null;
		TreeMap<Double, Double> points_interpolated = null;

		if (need.equals("-1")) {
			System.out.println("NECESIDAD : TOTAL\n" + "=====================");
			int divisor = qrelsR.keySet().size();
			precision = resultados[0] / divisor;
			recall = resultados[1] / divisor;
			f1 = resultados[2] / divisor;
			prec_10 = resultados[3] / divisor;
			averagePrec = resultados[4] / divisor;
			for (Entry<Double, Double> entry : interpolatedTotal.entrySet()) {
				Double key = entry.getKey();
				Double value = round(entry.getValue() / divisor, 3);
				interpolatedTotal.put(key, value);
			}
			points_interpolated = interpolatedTotal;

		} else {
			System.out.println("NECESIDAD : " + need + "\n"
					+ "=====================");
			precision = precision(need, qrelsR, docRecup);
			resultados[0] += precision;

			recall = recall(need, qrelsR, docRecup);
			resultados[1] += recall;

			f1 = F1(precision, recall, BETA);
			resultados[2] += f1;

			prec_10 = kPrecision(10, need, qrelsR, docRecup);
			resultados[3] += prec_10;

			averagePrec = averagePrecision(need, qrelsR, docRecup);
			resultados[4] += averagePrec;
			points = recall_precision(need, qrelsR, docRecup);
			points_interpolated = interpolated_recall_precision(need, qrelsR,
					docRecup);
		}

		System.out.println("Precision " + precision);
		System.out.println("Recall " + recall);
		System.out.println("F1 " + f1);
		System.out.println("Precision@10 " + prec_10);
		System.out.println("Average precision " + averagePrec);
		System.out.println("Recall_precision");
		if (!need.equals("-1"))
			for (Entry<Double, Double> entry : points.entrySet()) {
				Double key = entry.getKey();
				Double value = entry.getValue();
				System.out.println(key + "	" + value);
			}
		System.out.println("Interpolated_recall_precision");
		for (Entry<Double, Double> entry : points_interpolated.entrySet()) {
			Double key = entry.getKey();
			Double value = entry.getValue();
			interpolatedTotal.put(key,
					(interpolatedTotal.containsKey(key)) ? value
							+ interpolatedTotal.get(key) : value);
			System.out.println(key + "	" + value);
		}
		System.out.println();
	}

	/**
	 * Metodo que redondea un double un numero de decimales igual a places
	 * 
	 * @param value
	 * @param places
	 * @return
	 */
	private static double round(double value, int places) {
		if (places < 0)
			throw new IllegalArgumentException();

		BigDecimal bd = new BigDecimal(value);
		bd = bd.setScale(places, RoundingMode.HALF_UP);
		return bd.doubleValue();
	}
}

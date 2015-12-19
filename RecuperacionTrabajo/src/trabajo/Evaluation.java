package trabajo;

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
	private static final String RESULT = "src/equipo02.txt";

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
		double nRR = cuentaDeIntersección(relevantes, recuperados);

		// Recuperados
		double docR = recuperados.size(); // doc Recuperados
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
		double nRR = cuentaDeIntersección(relevantes, recuperados);
		double nR = relevantes.size();// num Relevantes
		return nRR / nR;
	}

	public static double F1(double precision, double recall, double beta) {
		return round(((beta * beta + 1) * precision * recall)
				/ ((beta * beta) * precision + recall), 3);
	}

	public static double kPrecision(int k, String need,
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
		List<String> krecuperados;
		if(recuperados.size()<k){
			krecuperados=recuperados;
		}
		else{
			krecuperados=recuperados.subList(0, k);
		}
		double nR = cuentaDeIntersección(relevantes,krecuperados);// Numero
		// de
		// relevantes
		// en
		// coleccion
		// [0,..,k]
		return round(nR / k, 3);
	}

	public static double kRecall(int k, String need,
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
		// Numero de relevantes encoleccion [0,..,k]
		double nR = cuentaDeIntersección(relevantes, recuperados.subList(0, k));
		int nRTotal = relevantes.size();// num Relevantes
		return round(nR / nRTotal, 3);
	}

	public static double averagePrecision(String need,
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

		double precisionFinal = 0;
		for (int i = 0; i < recuperados.size(); i++) {
			if (relevantes.contains(recuperados.get(i))) {
				precisionFinal += kPrecision(i + 1, need, rel, recu);
			}
		}
		if (cuentaDeIntersección(relevantes, recuperados)==0){
			return 0;
		}else{
			return (precisionFinal / cuentaDeIntersección(relevantes, recuperados));
		}
	}

	public static TreeMap<Double, Double> recall_precision(String need,
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
			else{
				points.put(round(i, 1), 0.0);

			}
		}
		return points;
	}

	/*
	 * METODOS PRIVADOS
	 */

	private static void calculateAndPrint(String need) {
		double precision = precision(need, qrelsR, docRecup);
		double recall = recall(need, qrelsR, docRecup);
		double f1 = F1(precision, recall, beta);
		double prec_10 = kPrecision(10, need, qrelsR, docRecup);
		double averagePrec = averagePrecision(need, qrelsR, docRecup);
		TreeMap<Double, Double> points = recall_precision(need, qrelsR,
				docRecup);
		TreeMap<Double, Double> points_interpolated = interpolated_recall_precision(
				need, qrelsR, docRecup);
		if(!need.equals("-1"))
		System.out.println("NECESIDAD : " + need + "\n"
				+ "=====================");
		else
			System.out.println("NECESIDAD : TOTAL \n"
				+ "=====================");

		System.out.println("Precision " + precision);
		System.out.println("Recall " + recall);
		System.out.println("F1 " + f1);
		System.out.println("Precision@10 " + prec_10);
		System.out.println("Average precision " + averagePrec);
		System.out.println("Recall_precision");
		for (Entry<Double, Double> entry : points.entrySet()) {
			Double key = entry.getKey();
			Double value = entry.getValue();

			System.out.println(key + "	" + value);
		}
		System.out.println("Interpolated_recall_precision");
		for (Entry<Double, Double> entry : points_interpolated.entrySet()) {
			Double key = entry.getKey();
			Double value = entry.getValue();

			System.out.println(key + "	" + value);
		}
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

	private static double round(double value, int places) {
		if (places < 0)
			throw new IllegalArgumentException();

		BigDecimal bd = new BigDecimal(value);
		bd = bd.setScale(places, RoundingMode.HALF_UP);
		return bd.doubleValue();
	}
}

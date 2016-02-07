package test;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.util.FileManager;

public class SemanticSearcher {

	private static String usage = "SemanticSearcher"
			+ " [-rdf RDF_PATH] [-rdfs RDFS_PATH] [-infoNeeds NEEDS_PATH]  [-output RESULTS_PATH]\n\n";

	/**
	 * ejecuci�n de consultas sparql
	 * 
	 * @throws IOException
	 */
	public static void main(String args[]) throws IOException {
		String rdfPath = null;
		String rdfsPath = null;
		String needsPath = null;
		String outputPath = null;
		for (int i = 0; i < args.length; i++) {
			if ("-rdf".equals(args[i])) {
				rdfPath = args[i + 1];
				i++;
			} else if ("-rdfs".equals(args[i])) {
				rdfsPath = args[i + 1];
				i++;
			} else if ("-infoNeeds".equals(args[i])) {
				needsPath = args[i + 1];
				i++;
			} else if ("-output".equals(args[i])) {
				outputPath = args[i + 1];
				i++;
			}
		}

		if (rdfPath == null || needsPath == null || outputPath == null
				|| rdfsPath == null) {
			System.err.println("Usage: " + usage);
			System.exit(1);
		}

		// cargamos el fichero deseado
		Model model = FileManager.get().loadModel(rdfPath);

		FileWriter bw = new FileWriter(outputPath);

		System.out.println("----------------------------------------");
		System.out.println("--------------CONSULTA 1----------------");
		System.out.println("----------------------------------------");

		Scanner S = new Scanner(new File(needsPath));
		S.next();
		String queryString = S.nextLine().trim();
		/*
		String queryString = "PREFIX dmrec: <http://www.recInfo.org/dm/> "
				+ "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
				+ "PREFIX skos: <http://www.w3.org/2004/02/skos/core#>  "
				+ "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> "
				+ "PREFIX dc: <http://purl.org/dc/elements/1.1/> "
				+ "SELECT DISTINCT ?id WHERE {  "
				+ "?doc rdf:type ?type . "
				+ "?type rdfs:subClassOf dmrec:document . "
				+ "?doc dmrec:creator ?autor . "
				+ "?autor dmrec:name ?name FILTER regex (?name, 'javier','i')  "
				+ "?doc dc:identifier ?id FILTER regex (?id,'^oai')  "
				+ "<http://example.com/RI/musica> skos:prefLabel ?prefLabel . "
				+ "<http://example.com/RI/musica> skos:narrower+/skos:prefLabel ?labels1 . "
				+ "<http://example.com/RI/sonido> skos:prefLabel ?prefLabel2 . "
				+ "<http://example.com/RI/sonido> skos:narrower+/skos:prefLabel ?labels2 . "
				+ "?doc dmrec:keyword ?keyword . "
				+ "?keyword skos:prefLabel ?keyLabel  "
				+ "FILTER ( (str(?keyLabel) = str(?labels1)) || "
				+ " (str(?keyLabel) = str(?labels2)) || (str(?keyLabel) = str(?prefLabel)) "
				+ " || (str(?keyLabel) = str(?prefLabel2))   ) " + " }";*/

		System.out.println(queryString);

		Query query = QueryFactory.create(queryString);
		QueryExecution qexec = QueryExecutionFactory.create(query, model);
		try {
			ResultSet results = qexec.execSelect();
			for (; results.hasNext();) {
				QuerySolution soln = results.nextSolution();
				Literal z = soln.getLiteral("id");
				String document = "13-2\t" + z.toString() + ".xml\n";
				bw.write(document.replaceAll(":", "_"));
			}
			System.out.println("RESULTS = " + results.getRowNumber());
		} finally {
			qexec.close();
		}

		System.out.println("----------------------------------------");
		System.out.println("--------------CONSULTA 2----------------");
		System.out.println("----------------------------------------");

		S.next();
		queryString = S.nextLine().trim();
		/*queryString = "PREFIX dmrec: <http://www.recInfo.org/dm/>"
				+ "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>"
				+ "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>"
				+ "PREFIX skos: <http://www.w3.org/2004/02/skos/core#>  "
				+ "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> "
				+ "PREFIX dc: <http://purl.org/dc/elements/1.1/> "
				+ " SELECT DISTINCT ?id WHERE { "
				+ "?doc rdf:type dmrec:TESIS . "
				+ "?doc dmrec:date ?date FILTER((?date > '2010'^^xsd:gYear) && (?date < '2016'^^xsd:gYear)) "
				+ "?doc dc:identifier ?id FILTER regex (?id,'^oai') "
				+ "<http://example.com/RI/energias_renovables> skos:prefLabel ?prefLabel . "
				+ "<http://example.com/RI/energias_renovables> skos:narrower+/skos:prefLabel ?labels1 . "
				+ "?doc dmrec:keyword ?keyword . "
				+ "?keyword skos:prefLabel ?keyLabel "
				+ "FILTER ( str(?keyLabel) = str(?labels1) "
				+ " || (str(?keyLabel) = str(?prefLabel))) " + "}";*/

		System.out.println(queryString);

		query = QueryFactory.create(queryString);
		qexec = QueryExecutionFactory.create(query, model);
		try {
			ResultSet results = qexec.execSelect();
			for (; results.hasNext();) {
				QuerySolution soln = results.nextSolution();
				Literal z = soln.getLiteral("id");
				// System.out.println(z.toString());
				String document = "09-3\t" + z.toString() + ".xml\n";
				bw.write(document.replaceAll(":", "_"));

			}
			System.out.println("RESULTS = " + results.getRowNumber());
		} finally {
			qexec.close();
		}

		System.out.println("----------------------------------------");
		System.out.println("--------------CONSULTA 3----------------");
		System.out.println("----------------------------------------");

		S.next();
		queryString = S.nextLine().trim();

		/*queryString = "PREFIX dmrec: <http://www.recInfo.org/dm/> "
				+ "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
				+ "PREFIX skos: <http://www.w3.org/2004/02/skos/core#>  "
				+ "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>"
				+ "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>  "
				+ "PREFIX dc: <http://purl.org/dc/elements/1.1/> "
				+ " SELECT DISTINCT ?id WHERE { "
				+ "?type rdfs:subClassOf dmrec:document . "
				+ "?doc dmrec:date ?date.FILTER (?date > '2010'^^xsd:gYear) "
				+ "?doc dc:identifier ?id FILTER regex (?id,'^oai') "
				+ "<http://example.com/RI/videojuegos> skos:prefLabel ?prefLabel . "
				+ "<http://example.com/RI/videojuegos> skos:narrower+/skos:prefLabel ?labels1 . "
				+ "<http://example.com/RI/diseñopersonajes> skos:prefLabel ?prefLabel2 . "
				+ "<http://example.com/RI/diseñopersonajes> skos:narrower+/skos:prefLabel ?labels2 . "
				+ "?doc dmrec:keyword ?keyword . "
				+ "?keyword skos:prefLabel ?keyLabel  "
				+ "FILTER ( (str(?keyLabel) = str(?labels1)) || "
				+ "(str(?keyLabel) = str(?labels2))  || (str(?keyLabel) = str(?prefLabel))  "
				+ " || (str(?keyLabel) = str(?prefLabel2))  )" + "}";*/

		System.out.println(queryString);

		query = QueryFactory.create(queryString);
		qexec = QueryExecutionFactory.create(query, model);
		try {
			ResultSet results = qexec.execSelect();
			for (; results.hasNext();) {
				QuerySolution soln = results.nextSolution();
				Literal z = soln.getLiteral("id");
				// System.out.println(z.toString());
				String document = "07-2\t" + z.toString() + ".xml\n";
				bw.write(document.replaceAll(":", "_"));

			}
			System.out.println("RESULTS = " + results.getRowNumber());
		} finally {
			qexec.close();
		}

		System.out.println("----------------------------------------");
		System.out.println("--------------CONSULTA 4----------------");
		System.out.println("----------------------------------------");
		
		S.next();
		queryString = S.nextLine().trim();

		/*queryString = "PREFIX dmrec: <http://www.recInfo.org/dm/> "
				+ "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
				+ "PREFIX skos: <http://www.w3.org/2004/02/skos/core#>  "
				+ "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>  "
				+ "PREFIX dc: <http://purl.org/dc/elements/1.1/> "
				+ " SELECT DISTINCT ?id WHERE { "
				+ "?doc rdf:type ?type . "
				+ "?type rdfs:subClassOf dmrec:document . "
				+ "?doc dc:identifier ?id FILTER regex (?id,'^oai') "
				+ "<http://example.com/RI/guerraIndependencia> skos:prefLabel ?prefLabel . "
				+ "<http://example.com/RI/guerraIndependencia> skos:narrower+/skos:prefLabel ?labels1 . "
				+ "?doc dmrec:keyword ?keyword . "
				+ "?keyword skos:prefLabel ?keyLabel "
				+ "FILTER (str(?keyLabel) = str(?labels1)  || (str(?keyLabel) = str(?prefLabel))  )"
				+ "}";*/

		System.out.println(queryString);

		query = QueryFactory.create(queryString);
		qexec = QueryExecutionFactory.create(query, model);
		try {
			ResultSet results = qexec.execSelect();
			for (; results.hasNext();) {
				QuerySolution soln = results.nextSolution();
				Literal z = soln.getLiteral("id");
				// System.out.println(z.toString());
				String document = "02-4\t" + z.toString() + ".xml\n";
				bw.write(document.replaceAll(":", "_"));

			}
			System.out.println("RESULTS = " + results.getRowNumber());
		} finally {
			qexec.close();
		}

		System.out.println("----------------------------------------");
		System.out.println("--------------CONSULTA 5----------------");
		System.out.println("----------------------------------------");

		S.next();
		queryString = S.nextLine().trim();

		/*queryString = "PREFIX dmrec: <http://www.recInfo.org/dm/> "
				+ "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
				+ "PREFIX skos: <http://www.w3.org/2004/02/skos/core#>  "
				+ "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>  "
				+ "PREFIX dc: <http://purl.org/dc/elements/1.1/> "
				+ " SELECT DISTINCT ?id WHERE { "
				+ "?doc rdf:type ?type . "
				+ "?type rdfs:subClassOf dmrec:document . "
				+ "?doc dc:identifier ?id FILTER regex (?id,'^oai') "
				+ "<http://example.com/RI/edadMedia> skos:prefLabel ?prefLabel . "
				+ "<http://example.com/RI/edadMedia> skos:narrower+/skos:prefLabel ?labels1 . "
				+ "<http://example.com/RI/Construccion> skos:prefLabel ?prefLabel2  . "
				+ "<http://example.com/RI/Construccion> skos:narrower+/skos:prefLabel ?labels2 . "
				+ "?doc dmrec:keyword ?keyword . "
				+ "?keyword skos:prefLabel ?keyLabel . "
				+ "FILTER (str(?keyLabel) = str(?labels1) || "
				+ "str(?keyLabel) = str(?labels2)  || (str(?keyLabel) = str(?prefLabel))  "
				+ " || (str(?keyLabel) = str(?prefLabel2))  )" + "}";*/

		System.out.println(queryString);

		query = QueryFactory.create(queryString);
		qexec = QueryExecutionFactory.create(query, model);
		try {
			ResultSet results = qexec.execSelect();
			for (; results.hasNext();) {
				QuerySolution soln = results.nextSolution();
				Literal z = soln.getLiteral("id");
				// System.out.println(z.toString());
				String document = "05-5\t" + z.toString() + ".xml\n";
				bw.write(document.replaceAll(":", "_"));

			}
			System.out.println("RESULTS = " + results.getRowNumber());
		} finally {
			qexec.close();
		}
		S.close();
		bw.close();
	}

}

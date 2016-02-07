package test;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.util.FileManager;

public class SPARQLTrabajo {

	/**
	 * ejecuci�n de consultas sparql
	 */
	public static void main(String args[]) {
		
		// cargamos el fichero deseado
		Model model = FileManager.get().loadModel("trabajo_rdf.rdf");
		Model skos = FileManager.get().loadModel("trabajo_skos.rdf");
		model.add(skos);
		
		System.out.println("----------------------------------------");		
		System.out.println("--------------CONSULTA 1----------------");
		System.out.println("----------------------------------------");

		 String queryString = "PREFIX dmrec: <http://www.recInfo.org/dm/> "
		 			+ "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
		 			+ "PREFIX skos: <http://www.w3.org/2004/02/skos/core#>  "
		 			+ "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>  "
			 		+ "SELECT DISTINCT ?doc WHERE {  "
			 		+ "?doc rdf:type ?type . "
			 		+ "?type rdfs:subClassOf dmrec:document . "
			 		+ "?doc dmrec:creator ?autor . "
			 		+ "?autor dmrec:name ?name FILTER regex (?name, 'javier','i')  "
			 		+ "?skosQ1 rdf:type skos:Concept . "
			 		+ "?skosQ1 skos:prefLabel ?prefLabel FILTER regex (?prefLabel,'musica','i')  "
			 		+ "?skosQ1 skos:narrower+/skos:prefLabel ?labels1 . "
			 		+ "?skosQ2 rdf:type skos:Concept . "
			 		+ "?skosQ2 skos:prefLabel ?prefLabel2 FILTER regex (?prefLabel2,'sonido','i')  "
			 		+ "?skosQ2 skos:narrower+/skos:prefLabel ?labels2 . "
			 		+ "?doc dmrec:keyword ?keyword . "
			 		+ "?keyword skos:prefLabel ?keyLabel  "
			 		+ "FILTER ( (str(?keyLabel) = str(?labels1)) || "
			 		+ " (str(?keyLabel) = str(?labels2)) ) "
			 		+ " }";
		 
		   Query query = QueryFactory.create(queryString) ;
		   QueryExecution qexec = QueryExecutionFactory.create(query, model) ;
		  try {
		    ResultSet results = qexec.execSelect() ;
		    for ( ; results.hasNext() ; )
		    {
		    	QuerySolution soln = results.nextSolution() ;
		    	Resource z = soln.getResource("doc"); 
		    	//System.out.println(z.toString());
		    }
		    System.out.println("RESULTS = "+results.getRowNumber());
		  } finally { qexec.close() ; }
		  
		System.out.println("----------------------------------------");		
		System.out.println("--------------CONSULTA 2----------------");
		System.out.println("----------------------------------------");

		 queryString = "PREFIX dmrec: <http://www.recInfo.org/dm/>"
		 			+ "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>"
		 			+ "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>"
		 			+ "PREFIX skos: <http://www.w3.org/2004/02/skos/core#>  "
			 		+ " SELECT DISTINCT ?doc WHERE { "
			 		+ "?doc rdf:type dmrec:TESIS . "
			 		+ "?doc dmrec:date ?date FILTER((?date > '2010'^^xsd:gYear) && (?date < '2016'^^xsd:gYear)) "
			 		+ "?skosQ1 rdf:type skos:Concept . "
			 		+ "?skosQ1 skos:prefLabel ?prefLabel FILTER regex (?prefLabel,'energias renovables','i') "
			 		+ "?skosQ1 skos:narrower+/skos:prefLabel ?labels1 . "
			 		+ "?doc dmrec:keyword ?keyword . "
			 		+ "?keyword skos:prefLabel ?keyLabel "
			 		+ "FILTER ( str(?keyLabel) = str(?labels1) ) "
			 		+ "}";
		 
		   query = QueryFactory.create(queryString) ;
		   qexec = QueryExecutionFactory.create(query, model) ;
		  try {
		    ResultSet results = qexec.execSelect() ;
		    for ( ; results.hasNext() ; )
		    {
		    	QuerySolution soln = results.nextSolution() ;
		    	Resource z = soln.getResource("doc"); 
		    	//System.out.println(z.toString());
		    }
		    System.out.println("RESULTS = "+results.getRowNumber());
		  } finally { qexec.close() ; }

		
		System.out.println("----------------------------------------");		
		System.out.println("--------------CONSULTA 3----------------");
		System.out.println("----------------------------------------");
		 
		 queryString = "PREFIX dmrec: <http://www.recInfo.org/dm/> "
		 			+ "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
		 			+ "PREFIX skos: <http://www.w3.org/2004/02/skos/core#>  "
		 			+ "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>"
		 			+ "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>  "
			 		+ " SELECT DISTINCT ?doc WHERE { "
			 		+ "?type rdfs:subClassOf dmrec:document . "
			 		+ "?doc dmrec:date ?date.FILTER (?date > '2010'^^xsd:gYear) "
			 		+ "?skosQ1 rdf:type skos:Concept . "
			 		+ "?skosQ1 skos:prefLabel ?prefLabel FILTER regex(?prefLabel,'videojuegos')"
			 		+ "?skosQ1 skos:narrower+/skos:prefLabel ?labels1 . "
			 		+ "?skosQ2 rdf:type skos:Concept . "
			 		+ "?skosQ2 skos:prefLabel ?prefLabel2 FILTER regex(?prefLabel2,'diseño de personajes')"
			 		+ "?skosQ2 skos:narrower+/skos:prefLabel ?labels2 . "
			 		+ "?doc dmrec:keyword ?keyword . "
			 		+ "?keyword skos:prefLabel ?keyLabel  "
			 		+ "FILTER ( (str(?keyLabel) = str(?labels1)) || "
			 		+ "(str(?keyLabel) = str(?labels2)) )"
			 		+ "}";
		 
		  query = QueryFactory.create(queryString) ;
		   qexec = QueryExecutionFactory.create(query, model) ;
		  try {
		    ResultSet results = qexec.execSelect() ;
		    for ( ; results.hasNext() ; )
		    {
		    	QuerySolution soln = results.nextSolution() ;
		    	Resource z = soln.getResource("doc"); 
		    	//System.out.println(z.toString());
		    }
		    System.out.println("RESULTS = "+results.getRowNumber());
		  } finally { qexec.close() ; }
		 
		  System.out.println("----------------------------------------");		
		  System.out.println("--------------CONSULTA 4----------------");
		  System.out.println("----------------------------------------");
			 
		 queryString = "PREFIX dmrec: <http://www.recInfo.org/dm/> "
		 			+ "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
		 			+ "PREFIX skos: <http://www.w3.org/2004/02/skos/core#>  "
		 			+ "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>  "
			 		+ " SELECT DISTINCT ?doc WHERE { "
			 		+ "?doc rdf:type ?type . "
			 		+ "?type rdfs:subClassOf dmrec:document . "
			 		+ "?skosQ1 rdf:type skos:Concept . "
			 		+ "?skosQ1 skos:prefLabel ?prefLabel FILTER regex(?prefLabel,'Guerra de la Independencia','i')"
			 		+ "?skosQ1 skos:narrower+/skos:prefLabel ?labels1 . "
			 		+ "?doc dmrec:keyword ?keyword . "
			 		+ "?keyword skos:prefLabel ?keyLabel "
			 		+ "FILTER (str(?keyLabel) = str(?labels1))"
			 		+ "}";
		 
		  query = QueryFactory.create(queryString) ;
		   qexec = QueryExecutionFactory.create(query, model) ;
		  try {
		    ResultSet results = qexec.execSelect() ;
		    for ( ; results.hasNext() ; )
		    {
		    	QuerySolution soln = results.nextSolution() ;
		    	Resource z = soln.getResource("doc"); 
		    	//System.out.println(z.toString());
		    }
		    System.out.println("RESULTS = "+results.getRowNumber());
		  } finally { qexec.close() ; }
		 
		 System.out.println("----------------------------------------");		
		 System.out.println("--------------CONSULTA 5----------------");
		 System.out.println("----------------------------------------");
		 
		 queryString = "PREFIX dmrec: <http://www.recInfo.org/dm/> "
		 			+ "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
		 			+ "PREFIX skos: <http://www.w3.org/2004/02/skos/core#>  "
		 			+ "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>  "
			 		+ " SELECT DISTINCT ?doc WHERE { "
			 		+ "?doc rdf:type ?type . "
			 		+ "?type rdfs:subClassOf dmrec:document . "
			 		+ "?skosQ1 rdf:type skos:Concept . "
			 		+ "?skosQ1 skos:prefLabel ?prefLabel FILTER regex(?prefLabel,'Edad Media','i')"
			 		+ "?skosQ1 skos:narrower+/skos:prefLabel ?labels1 . "
			 		+ "?skosQ2 rdf:type skos:Concept . "
			 		+ "?skosQ2 skos:prefLabel ?prefLabel2 FILTER regex(?prefLabel2,'construccion','i')"
			 		+ "?skosQ2 skos:narrower+/skos:prefLabel ?labels2 . "
			 		+ "?doc dmrec:keyword ?keyword . "
			 		+ "?keyword skos:prefLabel ?keyLabel . "
			 		+ "FILTER (str(?keyLabel) = str(?labels1) || "
			 		+ "str(?keyLabel) = str(?labels2))"
			 		+ "}";
		
		  query = QueryFactory.create(queryString) ;
		   qexec = QueryExecutionFactory.create(query, model) ;
		  try {
		    ResultSet results = qexec.execSelect() ;
		    for ( ; results.hasNext() ; )
		    {
		    	QuerySolution soln = results.nextSolution() ;
		    	Resource z = soln.getResource("doc"); 
		    	//System.out.println(z.toString());
		    }
		    System.out.println("RESULTS = "+results.getRowNumber());
		  } finally { qexec.close() ; }
		 
	}
	
}

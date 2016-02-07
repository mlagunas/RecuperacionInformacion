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

		 String queryString = 
				 	  
				 	"PREFIX dmrec: <http://www.recInfo.org/dm/> "
		 			+ "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
		 			+ "PREFIX skos: <http://www.w3.org/2004/02/skos/core#>  "
		 			+ "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>  "
			 		
		 			+ "SELECT ?narrower WHERE {  "
			 		+ "?skosQ1 rdf:type skos:concept ."
			 		+ "?skosQ1 skos:prefLabel ?label . "
			 		
			 		+ "?skosQ1 skos:narrower+ ?narrower ."
			 		//+ "?narrower skos:prefLabel ?prefLabel "
			 		//+ "FILTER regex (?prefLabel,'cancion','i')  "
			 		+ " }";
		 String aqueryString = "PREFIX dmrec: <http://www.recInfo.org/dm/> "
		 			+ "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
		 			+ "PREFIX skos: <http://www.w3.org/2004/02/skos/core#>  "
		 			+ "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>  "
			 		+ "SELECT ?label WHERE {  "
			 		+ "?doc rdf:type ?type . "
			 		+ "?type rdfs:subClassOf dmrec:document . "
			 		+ "?doc dmrec:creator ?autor . "
			 		+ "?autor dmrec:name ?name FILTER regex (?name, 'javier','i')  "
//			 		+ "?skosQ1 rdf:type skos:concept . "
//			 		+ "?skosQ1 skos:prefLabel ?prefLabel FILTER regex (?prefLabel,'musica','i')  "
			 		+ "?doc dmrec:keyword ?key . "
			 		+ "?key skos:narrower+/skos:prefLabel ?label "
			 		+ "FILTER regex (?label,'musica@sp') "

//			 		+ "?skosQ1 skos:narrower+/skos:prefLabel ?labels1 . "
//			 		+ "?doc dmrec:keyword ?key . "
//			 		+ "?key skos:prefLabel ?keyLabel . "
//					+ "FILTER (str(?keyLabel) = str(?labels1))"
//			 		+ "?skosQ2 skos:prefLabel ?prefLabel FILTER regex (?prefLabel,'sonido','i')  "
//			 		+ "?skosQ2 skos:narrower+/skos:prefLabel ?labels2 . "
//			 		+ "?doc dmrec:keyword ?keyword . "
//			 		+ "?keyword skos:prefLabel ?keyLabel  "
//			 		+ "FILTER ( lcase(str(?keyLabel)) = lcase(str(?labels1)) || "
//			 		+ " lcase(str(?keyLabel)) = lcase(str(?labels2)) ) "
			 		+ " }";
		 
		//ejecutamos la consulta y obtenemos los resultados
		   Query query = QueryFactory.create(queryString) ;
		   QueryExecution qexec = QueryExecutionFactory.create(query, model) ;
		  try {
		    ResultSet results = qexec.execSelect() ;
		    for ( ; results.hasNext() ; )
		    {
		    	QuerySolution soln = results.nextSolution() ;
		    	Literal z = soln.getLiteral("narrower"); 
		    	System.out.println(z.toString());
		    }
		    System.out.println("RESULTS = "+results.getRowNumber());
		  } finally { qexec.close() ; }

		 queryString = "PREFIX dmrec: <http://www.recInfo.org/dm/>"
		 			+ "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>"
			 		+ " SELECT ?doc WHERE { "
			 		+ "?doc rdf:type dmrec:TESIS ."
			 		+ "?doc dmrec:date ?date.FILTER((?date > 2010) && (?date < 2015))"
			 		+ "?skosQ1 rdf:type skos:concept . "
			 		+ "?skosQ1 skos:prefLabel ?prefLabel.FILTER regex(?prefLabel,'energias renovables')"
			 		+ "?skosQ1 skos:narrower+/skos:prefLabel ?labels1"
			 		+ "?doc dmrec:keyword ?key"
			 		+ "?keyword skos:prefLabel ?keyLabel"
			 		+ "FILTER regex(lcase(str(?keyLabel)) = lcase(str(?labels1)))"
			 		+ "}";
		 
		 queryString = "PREFIX dmrec: <http://www.recInfo.org/dm/>"
			 		+ " SELECT ?doc WHERE { "
			 		+ "?doc rdf:type dmrec:documento"
			 		+ "?doc dmrec:date ?date.FILTER(?date > 2010)"
			 		+ "?skosQ1 rdf:type skos:concept . "
			 		+ "?skosQ1 skos:prefLabel ?prefLabel.FILTER regex(?prefLabel,'videojuegos')"
			 		+ "?skosQ1 skos:narrower+/skos:prefLabel ?labels1"
			 		+ "?skosQ2 rdf:type skos:concept . "
			 		+ "?skosQ2 skos:prefLabel ?prefLabel.FILTER regex(?prefLabel,'diseño de personajes')"
			 		+ "?skosQ2 skos:narrower+/skos:prefLabel ?labels2"
			 		+ "?doc dmrec:keyword ?keyword"
			 		+ "?keyword skos:prefLabel ?keyLabel"
			 		+ "FILTER regex(lcase(str(?keyLabel)) = lcase(str(?labels1)) || "
			 		+ "lcase(str(?keyLabel)) = lcase(str(?keyLabel)))"
			 		+ "}";
		 
		 queryString = "PREFIX dmrec: <http://www.recInfo.org/dm/>"
			 		+ " SELECT ?doc WHERE { "
			 		+ "?doc rdf:type dmrec:documento"
			 		+ "?skosQ1 rdf:type skos:concept . "
			 		+ "?skosQ1 skos:prefLabel ?prefLabel.FILTER regex(?prefLabel,'Guerra de la Independencia')"
			 		+ "?skosQ1 skos:narrower+/skos:prefLabel ?labels1"
			 		+ "?doc dmrec:keyword ?key"
			 		+ "?keyword skos:prefLabel ?keyLabel"
			 		+ "FILTER regex(lcase(str(?keyLabel)) = lcase(str(?labels1)))"
			 		+ "}";
		 
		 queryString = "PREFIX dmrec: <http://www.recInfo.org/dm/>"
			 		+ " SELECT ?doc WHERE { "
			 		+ "?doc rdf:type dmrec:documento"
			 		+ "?skosQ1 rdf:type skos:concept . "
			 		+ "?skosQ1 skos:prefLabel ?prefLabel.FILTER regex(?prefLabel,'Edad Media')"
			 		+ "?skosQ1 skos:narrower+/skos:prefLabel ?labels1"
			 		+ "?skosQ2 rdf:type skos:concept . "
			 		+ "?skosQ2 skos:prefLabel ?prefLabel.FILTER regex(?prefLabel,'construccion')"
			 		+ "?skosQ2 skos:narrower+/skos:prefLabel ?labels2"
			 		+ "?doc dmrec:keyword ?keyword"
			 		+ "?keyword skos:prefLabel ?keyLabel"
			 		+ "FILTER regex(lcase(str(?keyLabel)) = lcase(str(?labels1)) || "
			 		+ "lcase(str(?keyLabel)) = lcase(str(?keyLabel)))"
			 		+ "}";
		
	}
	
}

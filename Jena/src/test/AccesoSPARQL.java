package test;


import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.util.FileManager;

public class AccesoSPARQL {

	/**
	 * ejecuci�n de consultas sparql
	 */
	public static void main(String args[]) {
		
		// cargamos el fichero deseado
		Model model = FileManager.get().loadModel("card.rdf");

		//definimos la consulta (tipo query)
		String queryString = "Select ?x ?y ?z WHERE  {?x ?y ?z }" ;
		
		//ejecutamos la consulta y obtenemos los resultados
		  Query query = QueryFactory.create(queryString) ;
		  QueryExecution qexec = QueryExecutionFactory.create(query, model) ;
		  try {
		    ResultSet results = qexec.execSelect() ;
		    for ( ; results.hasNext() ; )
		    {
		      QuerySolution soln = results.nextSolution() ;
		      Resource x = soln.getResource("x");
		      Resource y = soln.getResource("y");
		      RDFNode z = soln.get("z") ;  
		      if (z.isLiteral()) {
					System.out.println(x.getURI() + " - "
							+ y.getURI() + " - "
							+ z.toString());
				} else {
					System.out.println(x.getURI() + " - "
							+ y.getURI() + " - "
							+ z.asResource().getURI());
				}
		    }
		  } finally { qexec.close() ; }
		
		System.out.println("----------------------------------------");

		//definimos la consulta (tipo describe)
		queryString = "Describe <http://www.w3.org/People/Berners-Lee/card#i>" ;
		query = QueryFactory.create(queryString) ;
		qexec = QueryExecutionFactory.create(query, model) ;
		Model resultModel = qexec.execDescribe() ;
		qexec.close() ;
		resultModel.write(System.out);
		
		System.out.println("----------------------------------------");

		
		//definimos la consulta (tipo ask)
		queryString = "ask {<http://www.w3.org/People/Berners-Lee/card#i> ?x ?y}" ;
		query = QueryFactory.create(queryString) ;
		qexec = QueryExecutionFactory.create(query, model) ;
		System.out.println( qexec.execAsk()) ;
		qexec.close() ;
		
		System.out.println("----------------------------------------");
	
		//definimos la consulta (tipo cosntruct)
		queryString = "construct {?x <http://miuri/inverseSameAs> ?y} where {?y <http://www.w3.org/2002/07/owl#sameAs> ?x}" ;
		query = QueryFactory.create(queryString) ;
		qexec = QueryExecutionFactory.create(query, model) ;
		resultModel = qexec.execConstruct() ;
		qexec.close() ;
		resultModel.write(System.out);
		
		System.out.println("----------------------------------------");

		//Consulta pregunta 1 p5
		 queryString = "SELECT ?literal WHERE { ?x ?y ?literal.FILTER regex(?literal, 'Berners-Lee' )}";
		

		//ejecutamos la consulta y obtenemos los resultados
		   query = QueryFactory.create(queryString) ;
		   qexec = QueryExecutionFactory.create(query, model) ;
		  try {
		    ResultSet results = qexec.execSelect() ;
		    for ( ; results.hasNext() ; )
		    {
		      QuerySolution soln = results.nextSolution() ;
		      RDFNode z = soln.get("literal") ; 
		      
		      System.out.println(z.toString());
		    }
		  } finally { qexec.close() ; }
		
		System.out.println("----------------------------------------");
		
		//Consulta pregunta 2 p5
		 queryString = "PREFIX dc: <http://purl.org/dc/elements/1.1/> SELECT ?title WHERE { ?title dc:creator 'http://www.w3.org/People/Berners-Lee/card#i'}";
		

		//ejecutamos la consulta y obtenemos los resultados
		   query = QueryFactory.create(queryString) ;
		   qexec = QueryExecutionFactory.create(query, model) ;
		  try {
		    ResultSet results = qexec.execSelect() ;
		    for ( ; results.hasNext() ; )
		    {
		      QuerySolution soln = results.nextSolution() ;
		      
		      Resource z = soln.getResource("title") ; 
		      
		      System.out.println(z.toString());
		    }
		  } finally { qexec.close() ; }
		
		System.out.println("----------------------------------------");
		
		 queryString = "PREFIX dmrec: <http://www.recInfo.org/dm/>"
			 		+ " SELECT ?doc WHERE { "
			 		+ "?doc rdf:type dmrec:document"
			 		+ "?doc dmrec:creator ?autor"
			 		+ "?autor dmrec:name ?name.FILTER regex(?name, 'Javier')"
			 		+ "?skosQ1 skos:prefLabel ?prefLabel.FILTER regex(?prefLabel,'musica')"
			 		+ "?skosQ1 skos:narrower+/skos:prefLabel ?labels1"
			 		+ "?skosQ2 skos:prefLabel ?prefLabel.FILTER regex(?prefLabel,'sonido')"
			 		+ "?skosQ2 skos:narrower+/skos:prefLabel ?labels2"
			 		+ "?doc dmrec:keyword ?keyword"
			 		+ "?keyword skos:prefLabel ?keyLabel"
			 		+ "FILTER regex(lcase(str(?keyLabel)) = lcase(str(?labels1)) || "
			 		+ "lcase(str(?keyLabel)) = lcase(str(?keyLabel)))"
			 		+ "}";

		 queryString = "PREFIX dmrec: <http://www.recInfo.org/dm/>"
			 		+ " SELECT ?doc WHERE { "
			 		+ "?doc rdf:type dmrec:TESIS"
			 		+ "?doc dmrec:date ?date.FILTER((?date > 2010) && (?date < 2015))"
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
			 		+ "?skosQ1 skos:prefLabel ?prefLabel.FILTER regex(?prefLabel,'videojuegos')"
			 		+ "?skosQ1 skos:narrower+/skos:prefLabel ?labels1"
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
			 		+ "?doc dmrec:keyword ?key"
			 		+ "?key skos:prefLabel ?label.FILTER regex(?label,'Guerra de Independencia')"
			 		+ "}";
		 
		 queryString = "PREFIX dmrec: <http://www.recInfo.org/dm/>"
			 		+ " SELECT ?doc WHERE { "
			 		+ "?doc rdf:type dmrec:documento"
			 		+ "?skosQ1 skos:prefLabel ?prefLabel.FILTER regex(?prefLabel,'Edad Media')"
			 		+ "?skosQ1 skos:narrower+/skos:prefLabel ?labels1"
			 		+ "?skosQ2 skos:prefLabel ?prefLabel.FILTER regex(?prefLabel,'construcciones')"
			 		+ "?skosQ2 skos:narrower+/skos:prefLabel ?labels2"
			 		+ "?doc dmrec:keyword ?keyword"
			 		+ "?keyword skos:prefLabel ?keyLabel"
			 		+ "FILTER regex(lcase(str(?keyLabel)) = lcase(str(?labels1)) || "
			 		+ "lcase(str(?keyLabel)) = lcase(str(?keyLabel)))"
			 		+ "}";
		
	}
	
}

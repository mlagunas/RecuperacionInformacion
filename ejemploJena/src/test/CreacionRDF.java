/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package test;

import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.VCARD;

public class CreacionRDF {
	
	/**
	 * muestra un modelo de jena de ejemplo por pantalla
	 */
	public static void main (String args[]) {
        Model model = CreacionRDF.generarEjemplo();
        // write the model in the standar output
        model.write(System.out); 
    }
	
	/**
	 * Genera un modelo de jena de ejemplo
	 */
	public static Model generarEjemplo(){
		// definiciones
        String personURI    = "http://somewhere/JohnSmith";
        String givenName    = "John";
        String familyName   = "Smith";
        String fullName     = givenName + " " + familyName;
        
        String personURI2   = "http://somewhere/HanSolo";
        String givenName2    = "Han";
        String familyName2   = "Solo";
        String fullName2     = givenName + " " + familyName;
        
        String personURI3    = "http://somewhere/ShoHai";
        String givenName3    = "Sho";
        String familyName3   = "Hai";
        String fullName3     = givenName + " " + familyName;

        // crea un modelo vacio
        Model model = ModelFactory.createDefaultModel();
        Resource namespace=model.createResource("http://xmlns.com/foaf/0.1/");

        Property type = model.createProperty("http://www.w3.org/1999/02/22-rdf-syntax-ns#type");
        Resource person = model.createResource("http://xmlns.com/foaf/0.1/person");
        
        Property knows = model.createProperty("http://xmlns.com/foaf/0.1/knows");


        // le añade las propiedades
        Resource johnSmith  = model.createResource(personURI)
             .addProperty(VCARD.FN, fullName);
//             .addProperty(VCARD.N, 
//                      model.createResource()
//                           .addProperty(VCARD.Given, givenName)
//                           .addProperty(VCARD.Family, familyName));
        
        model.add(johnSmith,type,person);
        
        Resource hanSolo  = model.createResource(personURI2)
                .addProperty(VCARD.FN, fullName2);
//                .addProperty(VCARD.N, 
//                         model.createResource()
//                              .addProperty(VCARD.Given, givenName)
//                              .addProperty(VCARD.Family, familyName));
           
           model.add(hanSolo,type,person);
           
           Resource shoHai  = model.createResource(personURI3)
                   .addProperty(VCARD.FN, fullName3);
//                   .addProperty(VCARD.N, 
//                            model.createResource()
//                                 .addProperty(VCARD.Given, givenName)
//                                 .addProperty(VCARD.Family, familyName));
              
              model.add(shoHai,type,person);
              
             model.add(shoHai,knows,hanSolo);
             model.add(johnSmith,knows,shoHai);
             model.add(hanSolo,knows,johnSmith);
             
        return model;
	}
	
	
}

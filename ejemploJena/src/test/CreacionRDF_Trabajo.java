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

public class CreacionRDF_Trabajo {
	
	/**
	 * muestra un modelo de jena de ejemplo por pantalla
	 */
	public static void main (String args[]) {
        Model model = CreacionRDF_Trabajo.generarEjemplo();
        // write the model in the standar output
        model.write(System.out); 
    }
	
	/**
	 * Genera un modelo de jena de ejemplo
	 */
	public static Model generarEjemplo(){
		// definiciones
        String trabajoURI    = "https://zaguan.unizar.es/record/32726";
        String t    = "Web 2.0 y relaciones sociales";
        String author   = "Vázquez Toledo, Sandra";
        String ty= "TFG";

        // crea un modelo vacio
        Model model = ModelFactory.createDefaultModel();

        Property type = model.createProperty("http://www.w3.org/1999/02/22-rdf-syntax-ns#type");
        Resource person = model.createResource("http://xmlns.com/foaf/0.1/person");
        Property knows = model.createProperty("http://xmlns.com/foaf/0.1/knows");
        
        Property title= model.createProperty("http://purl.org/dc/elements/1.1/title");
        Property creator= model.createProperty("http://purl.org/dc/elements/1.1/creator");
        Property identifier= model.createProperty("http://purl.org/dc/elements/1.1/identifier");
        Property subject= model.createProperty("http://purl.org/dc/elements/1.1/subject");
        Property typeOf= model.createProperty("http://purl.org/dc/elements/1.1/type");
        Property description= model.createProperty("http://purl.org/dc/elements/1.1/description");
        Property publisher= model.createProperty("http://purl.org/dc/elements/1.1/publisher");
        Property format= model.createProperty("http://purl.org/dc/elements/1.1/format");
        Property language= model.createProperty("http://purl.org/dc/elements/1.1/language");

        
        Resource trabajo = model.createResource();
        
        Resource t1=model.createResource(trabajoURI).addProperty(title, t)
        		.addProperty(creator, author).addProperty(typeOf, ty);
        
        return model;
	}
	
	
}

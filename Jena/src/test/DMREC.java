package test;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;

public class DMREC {

	/**
	 * The RDF model that holds the SKOS entities
	 */
	private static final Model m = ModelFactory.createDefaultModel();
	/**
	 * The namespace of the SKOS vocabulary as a string
	 */
	public static final String uri = "http://www.recInfo.org/dm/";

	/**
	 * Returns the namespace of the SKOS schema as a string
	 * 
	 * @return the namespace of the SKOS schema
	 */
	public static String getURI() {
		return uri;
	}

	public static final Property DMdate = m.createProperty(uri + "date");
	public static final Property DMcreator = m.createProperty(uri + "creator");
	public static final Property DMpublisher = m.createProperty(uri + "publisher");
	public static final Property DMname = m.createProperty(uri + "name");
	public static final Property DMkeyword = m.createProperty(uri + "keyword");

}

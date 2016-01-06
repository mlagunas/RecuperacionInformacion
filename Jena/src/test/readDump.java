package test;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Scanner;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.sparql.vocabulary.FOAF;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.VCARD;

public class readDump {

	private static Boolean finFile = false;
	private String dumpPath;

	private static ArrayList<String> title = new ArrayList<String>();
	private static ArrayList<String> identifier = new ArrayList<String>();
	private static ArrayList<String> language = new ArrayList<String>();
	private static ArrayList<String> description = new ArrayList<String>();
	private static ArrayList<String> creator = new ArrayList<String>();
	private static ArrayList<String> publisher = new ArrayList<String>();
	private static ArrayList<String> date = new ArrayList<String>();

	public static void main(String args[]) {
		readDump rd = new readDump("dump");
		for (String s : title) {
			System.out.println(s);
		}
		Model model=createRDF();
		model.write(System.out);
	}
	
	public static  Model createRDF(){
		 Model model = ModelFactory.createDefaultModel();

	    Property type = model.createProperty("http://www.w3.org/1999/02/22-rdf-syntax-ns#type");
	    Resource person = model.createResource("http://xmlns.com/foaf/0.1/person");
	    //Creator hace referencia a un recurso organization
	    Resource organization = model.createResource("http://xmlns.com/foaf/0.1/organization");
	    // Publisher hace referencia a un recurso organization
	    Resource agent = model.createResource("http://xmlns.com/foaf/0.1/agent");
		
		
		Property title_p= model.createProperty("http://purl.org/dc/elements/1.1/title");
        Property creator_p= model.createProperty("http://purl.org/dc/elements/1.1/creator");
        Property identifier_p= model.createProperty("http://purl.org/dc/elements/1.1/identifier");
        Property typeOf_p= model.createProperty("http://purl.org/dc/elements/1.1/type");
        Property description_p= model.createProperty("http://purl.org/dc/elements/1.1/description");
        Property publisher_p= model.createProperty("http://purl.org/dc/elements/1.1/publisher");
        Property language_p= model.createProperty("http://purl.org/dc/elements/1.1/language");
        Property date_p =model.createProperty("http://purl.org/dc/elements/1.1/date");
        
		for (int i=0;i<title.size();i++){
			
			String tit=title.get(i);
			String id=identifier.get(i);
			String lang=language.get(i);
			String desc=description.get(i);
			String creat=creator.get(i);
			String publi=publisher.get(i);
			String dat=date.get(i);
			
			Resource org=null;
			Resource toSearch = ResourceFactory.createResource()
	        		.addProperty(FOAF.name, publi)
	        		.addProperty(RDF.type, organization);
			if(!model.containsResource(toSearch)){
					org = model.createResource()
		        		.addProperty(FOAF.name, publi)
		        		.addProperty(RDF.type, organization);
			}
			else{
				//linkear al recurso que ya esta en el modelo
			}

			Resource auth=null;
	        toSearch =ResourceFactory.createResource()
	        		.addProperty(VCARD.FN, creat)
	        		.addProperty(RDF.type, person);
	        
			if(!model.containsResource(toSearch)){
			     auth =model.createResource()
			        		.addProperty(VCARD.FN, creat)
			        		.addProperty(RDF.type, person);
			}
			else{
				//linkear al recurso que ya esta en el modelo
			}

	        Literal year = model.createTypedLiteral(dat, XSDDatatype.XSDgYear);

	        Resource doc=model.createResource(id)
	        		.addProperty(title_p, tit)
	        		.addProperty(creator_p, auth)
	        		.addProperty(typeOf_p, "TFG")
	        		.addProperty(publisher_p, org)
	        		.addProperty(date_p, year)
	        		.addProperty(language_p, lang)
	        		.addProperty(identifier_p, id)
	        		.addProperty(description_p, desc);
	        
		}
		
		return model;
		
	}

	/**
	 * Objeto que analiza un archivo dump para extraer su contenido, el cual
	 * debe estar previamente etiquetado, y lo almacena siguiendo el patron de
	 * sus etiquetas en respectivos ArrayList
	 * 
	 * @param dumpPath
	 */
	public readDump(String dumpPath) {
		this.dumpPath=dumpPath;
	}
	
	public void read(){
		try {
			this.dumpPath = dumpPath;
			if (dumpPath.isEmpty()) {
				System.err.println("Usage: dumpPath must be a valid path");
				System.exit(1);
			}
			final File docDir = new File(dumpPath);
			if (!docDir.exists() || !docDir.canRead()) {
				System.out
						.println("Document directory '"
								+ docDir.getAbsolutePath()
								+ "' does not exist or is not readable, please check the path");
				System.exit(1);
			}
			System.out.println("Getting data from dump...");
			indexDump(docDir);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	/**
	 * Dado un fichero, con un formato especifico devuelve el contenido de una
	 * etiqueta dada.
	 * 
	 * @param br
	 * @param tag
	 * @return
	 * @throws IOException
	 */
	private static String obtenerEtiquetas(BufferedReader br, String tag)
			throws IOException {
		String result = "";
		for (String line; (line = br.readLine()) != null;) {
			Scanner s = new Scanner(line);
			if (s.next().matches(tag)) {
				if (s.hasNext())
					result += s.nextLine();
				while ((line = br.readLine()) != null) {
					s = new Scanner(line);
					if (s.hasNext()) {
						String t = s.next();
						if (!t.matches("[a-zA-Z]+:")
								&& !t.matches("[a-zA-Z]+::")) {
							result += line;
						} else
							break;

					} else
						result += line;
				}
				break;
			}
			s.close();
		}
		if (br.readLine() == null)
			finFile = true;

		return result;
	}

	/**
	 * Realiza la indexacion de documentos para un archivo dump generado a
	 * partir de nutch
	 * 
	 * @param writer
	 * @param file
	 * @throws IOException
	 */
	private static void indexDump(File file) throws IOException {
		try (BufferedReader br = new BufferedReader(new FileReader(file))) {
			while (!finFile) {
				String contenido = obtenerEtiquetas(br, "Content:");

				insertIndexTag("title", contenido, title);
				insertIndexTag("identifier", contenido, identifier);
				insertIndexTag("language", contenido, language);
				insertIndexTag("description", contenido, description);
				insertIndexTag("creator", contenido, creator);
				insertIndexTag("publisher", contenido, publisher);
				insertIndexTag("date", contenido, date);
			}
		}
	}

	/**
	 * 
	 * 
	 * @param tag
	 *            - Etiqueta a buscar dentro del xml
	 * @param file
	 *            - Fichero xml a parsear para buscar el contenido de las
	 *            etiquetas
	 * @return devuelve una nodelist con todos los elementos encontrados en file
	 *         parseandoloe y buscando los campos con el tag dado.
	 */
	private static boolean insertIndexTag(String tag, String s,
			ArrayList<String> array) {
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory
					.newInstance();

			DocumentBuilder builder = factory.newDocumentBuilder();
			InputStream in = new ByteArrayInputStream(
					s.getBytes(StandardCharsets.UTF_8));
			InputSource inputSource = new InputSource(new InputStreamReader(in));
			org.w3c.dom.Document docu = builder.parse(inputSource);
			NodeList conditionList = docu.getElementsByTagName("dc:" + tag);

			for (int k = 0; k < conditionList.getLength(); ++k) {
				Element condition = (Element) conditionList.item(k);
				if (condition != null && condition.getFirstChild() != null)
					array.add(condition.getFirstChild().getNodeValue());
				else
					array.add("");
			}
			return true;

		} catch (Exception ex) {
			ex.printStackTrace();
			return false;
		}
	}
}

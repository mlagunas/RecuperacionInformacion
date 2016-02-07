package test;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.DC;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;


public class readDump {

	private static Boolean finFile = false;
	private String dumpPath;

	private static List<List<String>> title = new ArrayList<List<String>>();
	private static List<List<String>> identifier = new ArrayList<List<String>>();
	private static List<String> language = new ArrayList<String>();
	private static List<String> description = new ArrayList<String>();
	private static List<List<String>> creator = new ArrayList<List<String>>();
	private static List<String> publisher = new ArrayList<String>();
	private static List<String> date = new ArrayList<String>();
	private static List<String> conceptos = new ArrayList<String>();
	private static int iter = 0;

	public static void main(String args[]) throws FileNotFoundException {
		new readDump("dump");
		System.out.println(title.size());
		try {
			Model m = readXml(new File("skos.rdf"));
			m.write(new FileOutputStream(new File("skos_rdf.rdf")));
			Model model = createRDF(m);
			model.write(new FileOutputStream(new File("trabajo_rdf.rdf")));
			m.write(new FileOutputStream(new File("trabajo_skos.rdf")));

		} catch (ParserConfigurationException e1) {
			e1.printStackTrace();
		}

		/*
		 * new readDump("dump"); System.out.println(title.size());
		 * System.out.println(language.size()); System.out.println(date.size());
		 * System.out.println(publisher.size());
		 * System.out.println(creator.size());
		 * System.out.println(identifier.size());
		 * System.out.println(description.size());
		 * 
		 * Model model = createRDF(); try { model.write(new
		 * OutputStreamWriter(new FileOutputStream(new File(
		 * "trabajo_docs.rdf")), "UTF-8")); } catch (FileNotFoundException |
		 * UnsupportedEncodingException e) { // TODO Auto-generated catch block
		 * e.printStackTrace(); }
		 */
	}

	public static Model createRDF(Model skos) {
		Model model = ModelFactory.createDefaultModel();

		model.setNsPrefix("dmrec", DMREC.uri);
		model.setNsPrefix("skos", SKOS.uri);
		
		final Resource document = model.createResource(DMREC.uri+"document");
        final Resource TFG = model.createResource(DMREC.uri+"TFG");
        final Resource TESIS = model.createResource(DMREC.uri+"TESIS");
        final Resource PFC = model.createResource(DMREC.uri+"PFC");
        final Resource TFM = model.createResource(DMREC.uri+"TFM");
        
        TFG.addProperty(RDFS.subClassOf, document);
        TFM.addProperty(RDFS.subClassOf, document);
        TESIS.addProperty(RDFS.subClassOf, document);
        PFC.addProperty(RDFS.subClassOf, document);

		final Resource person = model.createResource(DMREC.uri+"person");
		final Resource organization = model.createResource(DMREC.uri+"organization");

		for (int i = 1; i < title.size(); i++) {
			if (identifier.get(i) == null) {
				continue;
			}
			String desc = "";
			if (description.get(i) != null) {
				desc = description.get(i);
			}
			List<String> id = identifier.get(i);
			String tit = title.get(i).get(0);
			String lang = "";
			if (language.get(i) != null) {
				lang = language.get(i);
			}
			List<String> creat = creator.get(i);
			String publi = publisher.get(i);
			String dat = date.get(i);

			/*
			 * ORGANIZATION ADDED TO RDF SCHEME
			 */
			Resource org = null;
			List<Resource> l = model.listResourcesWithProperty(DMREC.DMname,
					publi).toList();

			if (l.isEmpty()) {
				org = model.createResource(DMREC.uri+publi.replaceAll("\\p{Z}","")).addProperty(DMREC.DMname, publi)
						.addProperty(RDF.type, organization);
			} else {
				org = l.get(0);
			}

			Literal year = model.createTypedLiteral(dat, XSDDatatype.XSDgYear);

			Resource doc = model
					// Added URI identificator(0) = URL to Zaguan
					.createResource(id.get(0)).addProperty(DC.title, tit)
					.addProperty(DMREC.DMpublisher, org)
					.addProperty(DMREC.DMdate, year)
					.addProperty(DC.language, lang)
					.addProperty(DC.description, desc);
			// Identeificacion del segundo identificador
			if (id.size() >= 2 && id.get(1) != null && !id.get(1).isEmpty()) {
				String[] splitted = id.get(1).split("-");
				if (splitted.length >= 3) {
					String type = " ";
					if (splitted.length == 3)
						type = splitted[0];
					else if (splitted.length == 4)
						type = splitted[1];
					type.trim();
					switch(type){
					case "TFG":
						doc.addProperty(RDF.type, TFG);
						break;
					case "TFM":
						doc.addProperty(RDF.type, TFM);
						break;
					case "PFC":
						doc.addProperty(RDF.type, PFC);
						break;
					case "TESIS":
						doc.addProperty(RDF.type, TESIS);
						break;
					default:
						break;
					}
					doc.addProperty(DC.identifier, id.get(1));
				}
				doc.addProperty(DC.identifier, id.get(1));
			}

			if (id.size() >= 3) {
				for (int j = 2; j < id.size(); j++) {
					doc.addProperty(DC.identifier, id.get(j));
				}
			}

			/*
			 * AUTHOR ADDED TO THE RDF SCHEME
			 */
			for (int j = 0; j < creat.size(); j++) {
				Resource auth = null;
				l = model.listResourcesWithProperty(DMREC.DMname, creat.get(j))
						.toList();

				if (l.isEmpty()) {
					auth = model.createResource(DMREC.uri+creat.get(j).replaceAll("\\p{Z}",""))
							.addProperty(DMREC.DMname, creat.get(j))
							.addProperty(RDF.type, person);
				} else {
					auth = l.get(0);
				}
				doc.addProperty(DMREC.DMcreator, auth);
			}

			/*
			 * Busca si el documento tiene palabras clave y se asocia a su
			 * correspondiente concepto de skos
			 */
			for (String s : conceptos) {
				if (desc.toLowerCase().contains(s.toLowerCase())
						|| tit.toLowerCase().contains(s.toLowerCase())) {
					l = skos.listResourcesWithProperty(SKOS.prefLabel,
							s + "@sp").toList();
					if (!l.isEmpty()) {
						Resource key = l.get(0);
						doc.addProperty(DMREC.DMkeyword, key);
						//System.out.println(tit +"\nCointains--> "+s);
						//System.out.println(tit + "\nCointains--> " + s);
						// System.out.println(tit + "\nCointains--> " + s);
						iter++;

					}
				}
			}

		}
		System.out.println(iter);
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
		this.dumpPath = dumpPath;
		read();
	}

	/**
	 * Inicia el proceso en el cual se volcara en contenido de un fichero dump
	 * en diferentes ArrayList de acuerdo al etiquetado que se identifique
	 * dentro de dicho archivo
	 */
	public void read() {
		try {
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
			int i = 0;
			while (!finFile) {
				String contenido = obtenerEtiquetas(br, "Content:");
				if (i > 1) {
					insertIndexTag("title", contenido, title, true);
					insertIndexTag("identifier", contenido, identifier, true);
					insertIndexTag("language", contenido, language, false);
					insertIndexTag("description", contenido, description, false);
					insertIndexTag("creator", contenido, creator, true);
					insertIndexTag("publisher", contenido, publisher, false);
					insertIndexTag("date", contenido, date, false);
				}
				i++;
			}
			System.out.println("TOTAL: " + (i - 2));
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
	private static boolean insertIndexTag(String tag, String s, List array,
			Boolean LdeL) {
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory
					.newInstance();

			DocumentBuilder builder = factory.newDocumentBuilder();
			InputStream in = new ByteArrayInputStream(
					s.getBytes(StandardCharsets.UTF_8));
			InputSource inputSource = new InputSource(new InputStreamReader(in));
			org.w3c.dom.Document docu = builder.parse(inputSource);
			NodeList conditionList = docu.getElementsByTagName("dc:" + tag);

			ArrayList<String> aux = new ArrayList<String>();
			Boolean added = false;
			for (int k = 0; k < conditionList.getLength(); ++k) {
				Element condition = (Element) conditionList.item(k);
				if (condition != null && condition.getFirstChild() != null) {
					if (LdeL)
						aux.add(condition.getFirstChild().getNodeValue());
					else {
						added = true;
						array.add(condition.getFirstChild().getNodeValue());
					}
				}
			}
			if (LdeL && !aux.isEmpty()) {
				added = true;
				array.add(aux);
			}
			if (!added)
				if (!LdeL)
					array.add("");
				else
					array.add(null);
			return true;

		} catch (Exception ex) {
			if (LdeL)
				array.add(null);
			else
				array.add("");
			ex.printStackTrace();
			return false;
		}
	}

	public static Model readXml(File fXmlFile)
			throws ParserConfigurationException {

		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		Document doc = null;
		try {
			doc = dBuilder.parse(fXmlFile);
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		doc.getDocumentElement().normalize();

		System.out.println("Root element:: "
				+ doc.getDocumentElement().getNodeName());

		NodeList nList = doc.getElementsByTagName("skos:concept");

		// Cracion del Modelo de SKOS
		Model m = ModelFactory.createDefaultModel();

		m.setNsPrefix("skos", SKOS.uri);		
		ArrayList<Resource> skosConcepts = new ArrayList<Resource>();
		HashMap<String, Resource> skosNarrowers = new HashMap<String, Resource>();

		for (int temp = 0; temp < nList.getLength(); temp++) {

			Node nNode = nList.item(temp);
			// Obtenemos URI del concept a a�adir al modelo
			String conceptURI = nNode.getAttributes().getNamedItem("rdf:about")
					.getNodeValue();
			//System.out.println("\n" + nNode.getNodeName() + " :: " + conceptURI);
			Resource concept = m.createResource(conceptURI);
			m.add(concept, RDF.type, SKOS.Concept);

			if (nNode.getNodeType() == Node.ELEMENT_NODE) {
				// Iteramos por los elementos que incluyen a concept
				Element eElement = (Element) nNode;

				// Obtenemos las prefLabel
				NodeList prefLabel = eElement
						.getElementsByTagName("skos:prefLabel");
				for (int x = 0, size = prefLabel.getLength(); x < size; x++) {
					String lang = prefLabel.item(x).getAttributes()
							.getNamedItem("xml:lang").getNodeValue();
					String textLab = prefLabel.item(x).getTextContent();
					String label = textLab + "@" + lang;
					concept.addLiteral(SKOS.prefLabel, label);
					if (lang.equalsIgnoreCase("sp")) {
						conceptos.add(textLab);
					}
				}

				// Obtenemos las altLabel
				NodeList altLabel = eElement
						.getElementsByTagName("skos:altLabel");
				for (int x = 0, size = altLabel.getLength(); x < size; x++) {
					String lang = altLabel.item(x).getAttributes()
							.getNamedItem("xml:lang").getNodeValue();
					String textLab = altLabel.item(x).getTextContent();
					String label = textLab + "@" + lang;
					concept.addLiteral(SKOS.altLabel, label);
				}

				// Obtenemos inScheme
				NodeList inScheme = eElement
						.getElementsByTagName("skos:inScheme");
				for (int x = 0, size = inScheme.getLength(); x < size; x++) {
					String scheme = inScheme.item(x).getAttributes()
							.getNamedItem("rdf:resource").getNodeValue();
					concept.addProperty(SKOS.inScheme, scheme);
				}

				// Obtenemos topConceptOF
				NodeList topConceptOf = eElement
						.getElementsByTagName("skos:topConceptOf");
				for (int x = 0, size = topConceptOf.getLength(); x < size; x++) {
					String topC = topConceptOf.item(x).getAttributes()
							.getNamedItem("rdf:resource").getNodeValue();
					concept.addProperty(SKOS.topConceptOf, topC);
				}

				// Obtenemos los narrower
				NodeList narrower = eElement
						.getElementsByTagName("skos:narrower");
				for (int x = 0, size = narrower.getLength(); x < size; x++) {
					String narr = narrower.item(x).getAttributes()
							.getNamedItem("rdf:resource").getNodeValue();
					skosNarrowers.put(narr, concept);
				}
			}
			skosConcepts.add(concept);
		}

		Iterator<Map.Entry<String, Resource>> it = skosNarrowers.entrySet()
				.iterator();
		while (it.hasNext()) {
			Map.Entry<String, Resource> pair = (Map.Entry<String, Resource>) it
					.next();
			if (skosConcepts.contains(pair.getValue())) {
				for (Resource concept : skosConcepts) {
					if (concept.getURI().equals(pair.getKey())) {
						pair.getValue().addProperty(SKOS.narrower, concept);
						break;
					}
				}
			}
			it.remove();
		}
		return m;
	}
}

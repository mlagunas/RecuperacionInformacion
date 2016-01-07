package test;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.hp.hpl.jena.datatypes.RDFDatatype;
import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.rdf.model.AnonId;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.RDFVisitor;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.sparql.vocabulary.FOAF;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;
import com.hp.hpl.jena.util.iterator.Filter;
import com.hp.hpl.jena.vocabulary.DC;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.VCARD;

public class readDump {

	private static Boolean finFile = false;
	private String dumpPath;

	private static List<String> title = new ArrayList<String>();
	private static List<List<String>> identifier = new ArrayList<List<String>>();
	private static List<String> language = new ArrayList<String>();
	private static List<String> description = new ArrayList<String>();
	private static List<List<String>> creator = new ArrayList<List<String>>();
	private static List<String> publisher = new ArrayList<String>();
	private static List<String> date = new ArrayList<String>();

	public static void main(String args[]) {
		readDump rd = new readDump("dump");
		Model model = createRDF();
		model.write(System.out);
	}

	public static Model createRDF() {
		Model model = ModelFactory.createDefaultModel();

		for (int i = 0; i < title.size(); i++) {

			String tit = title.get(i);
			List<String> id = identifier.get(i);
			String lang = language.get(i);
			String desc = description.get(i);
			List<String> creat = creator.get(i);
			String publi = publisher.get(i);
			String dat = date.get(i);

			/*
			 * ORGANIZATION ADDED TO RDF SCHEME
			 */
			Resource org = null;
			List<Resource> l = model
					.listResourcesWithProperty(RDF.type, FOAF.Organization)
					.filterKeep(new Filter<Resource>() {
						@Override
						public boolean accept(Resource arg0) {
							return arg0.hasProperty(FOAF.name, publi);
						}

					}).toList();
			boolean hasIt = false;
			for (Resource r : l) {
				AnonId aId = r.getId();
				if (aId.getLabelString().equals("Organization")) {
					hasIt = true;
					break;
				}
			}
			if (hasIt) {
				// Linkearlo de alguna forma
			} else
				org = model.createResource(new AnonId("Organization"))
						.addProperty(FOAF.name, publi)
						.addProperty(RDF.type, FOAF.Organization);

			/*
			 * AUTHOR ADDED TO THE RDF SCHEME
			 */
			Resource auth = null;
			hasIt = false;
			l = model.listResourcesWithProperty(VCARD.FN, creat)
					.filterKeep(new Filter<Resource>() {
						@Override
						public boolean accept(Resource arg0) {
							return arg0.hasProperty(RDF.type, FOAF.Person);
						}

					}).toList();
			for (Resource r : l) {
				AnonId aId = r.getId();
				if (aId.getLabelString().equals("Creator")) {
					hasIt = true;
					break;
				}
			}
			if (hasIt) {
				// Linkearlo de alguna forma
			} else
				auth = model.createResource("Creator")
						// CAMBIAR ESTA PUESTO CREATOR GET 0
						.addProperty(VCARD.FN, creat.get(0))
						.addProperty(RDF.type, FOAF.Person);

			Literal year = model.createTypedLiteral(dat, XSDDatatype.XSDgYear);

			Resource doc = model
					.
					// CAMBIAR ESTA PUESTO IDENTIFICADOR 0
					createResource(id.get(0)).addProperty(DC.title, tit)
					.addProperty(DC.creator, auth).addProperty(DC.type, "TFG")
					.addProperty(DC.publisher, org).addProperty(DC.date, year)
					.addProperty(DC.language, lang)
					.addProperty(DC.identifier, id.get(0))
					.addProperty(DC.description, desc);

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
				insertIndexTag("title", contenido, title, false);
				insertIndexTag("identifier", contenido, identifier, true);
				insertIndexTag("language", contenido, language, false);
				insertIndexTag("description", contenido, description, false);
				insertIndexTag("creator", contenido, creator, true);
				insertIndexTag("publisher", contenido, publisher, false);
				insertIndexTag("date", contenido, date, false);
				i++;
			}
			System.out.println(i);
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
				array.add("");
			return true;

		} catch (Exception ex) {
			array.add("");
			ex.printStackTrace();
			return false;
		}
	}
}

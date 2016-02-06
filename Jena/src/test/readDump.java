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
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.sparql.vocabulary.FOAF;
import com.hp.hpl.jena.vocabulary.DC;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.VCARD;

public class readDump {

	private static Boolean finFile = false;
	private String dumpPath;

	private final static String DMREC = "http://www.recInfo.org/dm/";
	private static List<List<String>> title = new ArrayList<List<String>>();
	private static List<List<String>> identifier = new ArrayList<List<String>>();
	private static List<String> language = new ArrayList<String>();
	private static List<String> description = new ArrayList<String>();
	private static List<List<String>> creator = new ArrayList<List<String>>();
	private static List<String> publisher = new ArrayList<String>();
	private static List<String> date = new ArrayList<String>();

	public static void main(String args[]) {
		new readDump("dump");
		System.out.println(title.size());
		System.out.println(language.size());
		System.out.println(date.size());
		System.out.println(publisher.size());
		System.out.println(creator.size());
		System.out.println(identifier.size());
		System.out.println(description.size());

		Model model = createRDF();
		try {
			model.write(new OutputStreamWriter(new FileOutputStream(new File(
					"trabajo_docs.rdf")), "UTF-8"));
		} catch (FileNotFoundException | UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static Model createRDF() {
		Model model = ModelFactory.createDefaultModel();

		// String skos = "http://www.w3.org/2004/02/skos/core#";
		model.setNsPrefix("foaf", FOAF.NS);
		model.setNsPrefix("dmrec", DMREC);

		Property DMdate = model
				.createProperty("http://www.recInfo.org/dm/date");
		Property DMcreator = model
				.createProperty("http://www.recInfo.org/dm/creator");
		Property DMpublisher = model
				.createProperty("http://www.recInfo.org/dm/publisher");
		Property DMname = model
				.createProperty("http://www.recInfo.org/dm/name");
		Property DMkeyword = model
				.createProperty("http://www.recInfo.org/dm/keyword");

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
			List<Resource> l = model.listResourcesWithProperty(DMname, publi)
					.toList();

			if (l.isEmpty()) {
				org = model.createResource().addProperty(DMname, publi)
						.addProperty(RDF.type, "dmrec:organization");
			} else {
				org = l.get(0);
			}

			Literal year = model.createTypedLiteral(dat, XSDDatatype.XSDgYear);

			Resource doc = model
					// Added URI identificator(0) = URL to Zaguan
					.createResource(id.get(0)).addProperty(DC.title, tit)
					.addProperty(DMpublisher, org).addProperty(DMdate, year)
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
					doc.addProperty(RDF.type, "dmrec:" + type);
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
				l = model.listResourcesWithProperty(DMname, creat.get(j))
						.toList();

				if (l.isEmpty()) {
					auth = model.createResource()
							.addProperty(VCARD.FN, creat.get(j))
							.addProperty(RDF.type, "dmrec:person");
				} else {
					auth = l.get(0);
				}
				doc.addProperty(DMcreator, auth);

			}

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
}

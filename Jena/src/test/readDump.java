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
		new readDump("dump");
		for (String s : title) {
			System.out.println(s);
		}
	}

	/**
	 * Objeto que analiza un archivo dump para extraer su contenido, el cual
	 * debe estar previamente etiquetado, y lo almacena siguiendo el patron de
	 * sus etiquetas en respectivos ArrayList
	 * 
	 * @param dumpPath
	 */
	public readDump(String dumpPath) {
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
			// TODO Auto-generated catch block
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

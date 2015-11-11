package trabajo;

/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.es.SpanishAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.NumericRangeQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/** Simple command-line based search demo. */
/**
 * Run configurations arguments -> -index index -infoNeeds src/info_needs.xml
 * -output src/output.xml
 * 
 * @author Manuel
 *
 */
public class SearchFiles {

	/** Simple command-line based search demo. */
	@SuppressWarnings("deprecation")
	public static void main(String[] args) throws Exception {
		String usage = "Usage:\tjava org.apache.lucene.trabajo.SearchFiles -index <indexPath> -infoNeeds <infoNeedsFile> -output <resultsFile>\n\n";
		if (args.length > 0
				&& ("-h".equals(args[0]) || "-help".equals(args[0]))) {
			System.out.println(usage);
			System.exit(0);
		}

		String index = "index";
		String infoNeeds = null;
		String output = null;
		int hitsPerPage = 100000;

		for (int i = 0; i < args.length; i++) {
			if ("-index".equals(args[i])) {
				index = args[i + 1];
				i++;
			} else if ("-infoNeeds".equals(args[i])) {
				infoNeeds = args[i + 1];
				i++;
			} else if ("-output".equals(args[i])) {
				output = args[i + 1];
				i++;
			}

		}
		IndexReader reader = DirectoryReader.open(FSDirectory.open(new File(
				index)));
		IndexSearcher searcher = new IndexSearcher(reader);
		Analyzer analyzer = new SpanishAnalyzer(Version.LUCENE_44);

		BufferedReader in = null;
		FileWriter bw = new FileWriter(output);
		
		if (infoNeeds != null) {
			in = new BufferedReader(new InputStreamReader(new FileInputStream(
					infoNeeds), "UTF-8"));
		} else {
			in = new BufferedReader(new InputStreamReader(System.in, "UTF-8"));
		}

		QueryParser parser = new MultiFieldQueryParser(Version.LUCENE_44,
				new String[] { "description", "title" }, analyzer);

		QueryParser lineParser = new QueryParser(Version.LUCENE_44, "title",
				analyzer);
		ArrayList<String> aux = getNeeds(infoNeeds);
		ArrayList<String> ids = getIds(infoNeeds);
		int numNeed=0;
		for (String need : aux) {
			String id=ids.get(numNeed);
			numNeed++;
			while (true) {
				if (infoNeeds == null && output == null) { // prompt the
																// user
					System.out.println("Enter query: ");
				}

				String line = need;
				if (line == null || line.length() == -1) {
					break;
				}

				line = line.trim();
				if (line.length() == 0) {
					break;
				}

				// String lineParsed = lineParser.parse(line).toString("title");

				ArrayList<String> author = new ArrayList<String>();
				ArrayList<String> tipo = new ArrayList<String>();
				ArrayList<String> date = new ArrayList<String>();
				ArrayList<String> interval = new ArrayList<String>();

				Dictionare d = new Dictionare();
				if (line.endsWith(".")) {
					line = line.substring(0, line.length() - 1);
				}
				//Separa la cadena comparando con la expresion regular de manera que optimice
				//diferentes tipos de busquedas futuras
				String[] palabras = line.split("[^a-zA-Z0-9·ÈÌÛ˙¡…Õ”⁄¸‹Ò]+");
				System.out.println(line);
				for (int j = 0; j < palabras.length; j++) {
					// CREATOR
					if (d.map.get("author").containsKey(palabras[j])) {
						author.add(palabras[j]);
					}
					// IDENTIFIER
					if (d.map.get("identifier").containsKey(palabras[j])) {
						tipo.add(palabras[j]);
					}
					// DATE
					if (isValidDate(palabras[j])) {
						int nIntervalo = j + 4;
						if (j + 4 > palabras.length) {
							nIntervalo = palabras.length - j;
						}
						for (int h = j + 1; h < nIntervalo; h++) { // Mira las
																	// siguientes
																	// fechas en
																	// busca
																	// de
																	// intervalos
							if (isValidDate(palabras[h])) {
								interval.add(palabras[j]);
								interval.add(palabras[h]);
							}
						}
						date.add(palabras[j]);

					}

				}

				/*
				 * Creamos Queries booleanas en caso de que hayamos detectado
				 * algun autor o tipo de trabajo y las ponemos con nivel de
				 * ocurrencia "MUST"
				 */
				BooleanQuery bool = new BooleanQuery();
				if (!author.isEmpty()) {
					Term t = new Term("creator", arrayToQuery(author));
					TermQuery termQuery = new TermQuery(t);
					bool.add(termQuery, BooleanClause.Occur.SHOULD);
				}
				if (!tipo.isEmpty()) {
					Term t = new Term("identifier", arrayToQuery(tipo));
					TermQuery termQuery = new TermQuery(t);
					bool.add(termQuery, BooleanClause.Occur.SHOULD);
				}

				if (!date.isEmpty()) {
					Term t = new Term("date", arrayToQuery(date));
					TermQuery termQuery = new TermQuery(t);
					bool.add(termQuery, BooleanClause.Occur.SHOULD);
				}
				
				if (!interval.isEmpty()) {
					for (int i = 0; i < interval.size(); i += 2) {
						NumericRangeQuery<Integer> intervalQuery = NumericRangeQuery
								.newIntRange("date",
										Integer.parseInt(interval.get(i)),
										Integer.parseInt(interval.get(i + 1)),
										true, true);
						bool.add(intervalQuery, BooleanClause.Occur.SHOULD);
					}

				}
				Query query = parser.parse(line);

				/*
				 * Query de la frase entera en campos "title" y "description" y
				 * nivel de ocurrencia "SHOULD"
				 */
				bool.add(query, BooleanClause.Occur.SHOULD);
				doPagingSearch(in, searcher, bool, hitsPerPage,
						infoNeeds == null && output == null,bw,id);

				if (output != null) {
					break;
				}
			}
		}
		reader.close();
		bw.close();
	}

	private static ArrayList<String> getIds(String infoNeeds) {
		ArrayList<String> result = new ArrayList<String>();
		try {

			DocumentBuilderFactory factory = DocumentBuilderFactory
					.newInstance();
			DocumentBuilder builder;

			builder = factory.newDocumentBuilder();

			InputSource inputSource = new InputSource(new InputStreamReader(
					new FileInputStream(infoNeeds), "UTF-8"));
			org.w3c.dom.Document docu = builder.parse(inputSource);
			NodeList conditionList = docu.getElementsByTagName("identifier");

			for (int k = 0; k < conditionList.getLength(); ++k) {
				String text = "";
				Element condition = (Element) conditionList.item(k);
				if (condition != null && condition.getFirstChild() != null) {
					text = condition.getFirstChild().getNodeValue();
					System.out.println(text);
					result.add(text);
				}
			}
			return result;
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
			return result;
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return result;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return result;
		} catch (SAXException e) {
			e.printStackTrace();
			return result;
		} catch (IOException e) {
			e.printStackTrace();
			return result;
		}
	}

	private static ArrayList<String> getNeeds(String infoNeeds) {
		ArrayList<String> result = new ArrayList<String>();
		try {

			DocumentBuilderFactory factory = DocumentBuilderFactory
					.newInstance();
			DocumentBuilder builder;

			builder = factory.newDocumentBuilder();

			InputSource inputSource = new InputSource(new InputStreamReader(
					new FileInputStream(infoNeeds), "UTF-8"));
			org.w3c.dom.Document docu = builder.parse(inputSource);
			NodeList conditionList = docu.getElementsByTagName("text");

			for (int k = 0; k < conditionList.getLength(); ++k) {
				String text = "";
				Element condition = (Element) conditionList.item(k);
				if (condition != null && condition.getFirstChild() != null) {
					text = condition.getFirstChild().getNodeValue();
					System.out.println(text);
					result.add(text);
				}
			}
			return result;
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
			return result;
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return result;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return result;
		} catch (SAXException e) {
			e.printStackTrace();
			return result;
		} catch (IOException e) {
			e.printStackTrace();
			return result;
		}
	}

	public static boolean isValidDate(String dateString) {
		if (dateString == null
				|| (dateString.length() != "yyyyMMdd".length() && dateString
						.length() != "yyyy".length())) {
			return false;
		}
		if (dateString.length() != "yyyy".length()) {
			int date;
			try {
				date = Integer.parseInt(dateString);
			} catch (NumberFormatException e) {
				return false;
			}

			int year = date / 10000;
			int month = (date % 10000) / 100;
			int day = date % 100;

			boolean yearOk = (year >= 1581) && (year <= 2500);
			boolean monthOk = (month >= 1) && (month <= 12);
			boolean dayOk = (day >= 1) && (day <= daysInMonth(year, month));

			return (yearOk && monthOk && dayOk);
		} else {
			int year;
			try {
				year = Integer.parseInt(dateString);
			} catch (NumberFormatException e) {
				return false;
			}
			boolean yearOk = (year >= 1581) && (year <= 2500);
			return (yearOk && dateString.length() == 4);
		}
	}

	private static int daysInMonth(int year, int month) {
		int daysInMonth;
		switch (month) {
		case 1: // fall through
		case 3: // fall through
		case 5: // fall through
		case 7: // fall through
		case 8: // fall through
		case 10: // fall through
		case 12:
			daysInMonth = 31;
			break;
		case 2:
			if (((year % 4 == 0) && (year % 100 != 0)) || (year % 400 == 0)) {
				daysInMonth = 29;
			} else {
				daysInMonth = 28;
			}
			break;
		default:
			// returns 30 even for nonexistant months
			daysInMonth = 30;
		}
		return daysInMonth;
	}

	private static String arrayToQuery(ArrayList<String> array) {
		String result = "";
		for (String s : array) {
			result += s.toLowerCase();
		}
		return result;
	}

	/**
	 * This demonstrates a typical paging search scenario, where the search
	 * engine presents pages of size n to the user. The user can then go to the
	 * next page if interested in the next hits.
	 * 
	 * When the query is executed for the first time, then only enough results
	 * are collected to fill 5 result pages. If the user wants to page beyond
	 * this limit, then the query is executed another time and all hits are
	 * collected.
	 * 
	 */
	public static void doPagingSearch(BufferedReader in,
			IndexSearcher searcher, Query query, int hitsPerPage,
			boolean interactive,FileWriter bw, String id) throws IOException {

		searcher.setSimilarity(new BM25Similarity());
		// Collect enough docs to show 5 pages
		TopDocs results = searcher.search(query, 5 * hitsPerPage);
		ScoreDoc[] hits = results.scoreDocs;

		int numTotalHits = results.totalHits;
		System.out.println(numTotalHits + " total matching documents");

		int start = 0;
		int end = numTotalHits;

		while (true) {

			if (end > hits.length) {
				System.out
						.println("Only results 1 - " + hits.length + " of "
								+ numTotalHits
								+ " total matching documents collected.");
				hits = searcher.search(query, numTotalHits).scoreDocs;
			}

			end = Math.min(hits.length, start + hitsPerPage);

			for (int i = start; i < end; i++) {
				// System.out.println(searcher.explain(query, hits[i].doc));

				// if (false) { // output raw format
				// System.out.println("doc="+hits[i].doc+" score="+hits[i].score);
				// continue;
				// }

				Document doc = searcher.doc(hits[i].doc);
				String path = doc.get("path");
				if (path != null) {
					System.out.println(id+"\t"+path.substring(22, path.length()));
					bw.write(id+"\t"+path.substring(22, path.length())+"\n");
				} else {
					System.out.println((i + 1) + ". "
							+ "No path for this document");
				}

			}

			if (!interactive || end == 0) {
				break;
			}

			if (numTotalHits >= end) {
				boolean quit = false;
				while (true) {
					System.out.print("Press ");
					if (start - hitsPerPage >= 0) {
						System.out.print("(p)revious page, ");
					}
					if (start + hitsPerPage < numTotalHits) {
						System.out.print("(n)ext page, ");
					}
					System.out
							.println("(q)uit or enter number to jump to a page.");

					String line = in.readLine();
					if (line.length() == 0 || line.charAt(0) == 'q') {
						quit = true;
						break;
					}
					if (line.charAt(0) == 'p') {
						start = Math.max(0, start - hitsPerPage);
						break;
					} else if (line.charAt(0) == 'n') {
						if (start + hitsPerPage < numTotalHits) {
							start += hitsPerPage;
						}
						break;
					} else {
						int page = Integer.parseInt(line);
						if ((page - 1) * hitsPerPage < numTotalHits) {
							start = (page - 1) * hitsPerPage;
							break;
						} else {
							System.out.println("No such page");
						}
					}
				}
				if (quit)
					break;
				end = Math.min(numTotalHits, start + hitsPerPage);
			}
		}
	}

	public static boolean isNumber(String string) {
		if (string == null || string.isEmpty()) {
			return false;
		}
		int i = 0;
		if (string.charAt(0) == '-') {
			if (string.length() > 1) {
				i++;
			} else {
				return false;
			}
		}
		for (; i < string.length(); i++) {
			if (!Character.isDigit(string.charAt(i))) {
				return false;
			}
		}
		return true;
	}
}

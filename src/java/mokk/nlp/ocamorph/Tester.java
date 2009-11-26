package mokk.nlp.ocamorph;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.TermAttribute;
import org.apache.lucene.util.AttributeImpl;
import org.apache.lucene.util.Version;

public class Tester {

	public static void hahahah(){
		Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_CURRENT); // or any other analyzer
	      TokenStream ts = analyzer.tokenStream("myfield", new StringReader("some text goes here"));
	      TermAttribute termAtt = ts.addAttribute(TermAttribute.class);
	      try {
			while (ts.incrementToken()) {
			    System.out.println("--------token: "+ts);
			    System.out.println("TermAtt: "+termAtt.term());
			  }
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}

	/**
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		hahahah();
	}

	private static void testStemmer(String[] args) throws IOException {
		boolean blocking = false; // true;
		boolean stopAtFirst = false;

		OcamorphWrapper ocamorphWrapper = new OcamorphWrapper(args[0],
				blocking, stopAtFirst, // Compounds.No, Guess.NoGuess);
				Compounds.Allow, Guess.NoGuess);
		ocamorphWrapper.setDebug(false);
		OcamorphStemmer stemmer = new OcamorphStemmer(ocamorphWrapper);
		stemmer.setDebug(false);
		String encoding = ocamorphWrapper.getEncoding();
		String dir = args[1]; // "/home/bpgergo/hunglish_tools/ocamorph/ocamorph/src/bindings/java/src/java/mokk/nlp/ocamorph/";

		// OcamorphCachedStemmer cachedStemmer = new
		// OcamorphCachedStemmer(dir+"cache.txt", encoding, stemmer);
		// cachedStemmer.setDebug(false);

		File file;
		BufferedReader fileInput;
		File fileOut;
		BufferedWriter output;

		// ---
		file = new File(dir + "Tolkien_1.hu.tok");
		fileInput = new BufferedReader(new InputStreamReader(
				new FileInputStream(file), "iso-8859-2"));
		fileOut = new File(dir + "Tolkien_1.hu.cache");
		output = new BufferedWriter(new OutputStreamWriter(
				new FileOutputStream(fileOut), "UTF-8"));
		createCacheAndWrite(stemmer, fileInput, output);

		// cachedStemmer.writeResourceFromCache();
	}

	static String TAB = "\t";
	static String COLON = ":";
	static String NEWLINE = "\n";

	private static void createCacheAndWrite(OcamorphStemmer stemmer,
			BufferedReader input, BufferedWriter out) throws IOException {
		String line = null;
		Map<String, Set<String>> stemCache = new HashMap<String, Set<String>>();
		// Set<String> stems;
		System.out.println("hehe1");
		while ((line = input.readLine()) != null) {
			// out.write(line);
			// out.write(colon);
			for (String word : line.split(" ")) {

				// stems = stemmer.getStems(s);
				// stems.remove(line);
				stemCache.put(word, stemmer.getStems(word));
				// out.write("\n");
			}
		}
		System.out.println("hehe2");
		for (String word : stemCache.keySet()) {
			out.write(word);
			out.write(COLON);
			for (String stem : stemCache.get(word)) {
				out.write(stem);
				out.write(TAB);
			}
			out.write(NEWLINE);
		}

		out.close();
	}

}

/*
 * file = new File(dir+"tester-iso88592.txt"); fileInput = new
 * BufferedReader(new InputStreamReader( new FileInputStream(file), encoding));
 * fileOut = new File(dir+"out-iso88592.txt"); output = new BufferedWriter(new
 * OutputStreamWriter( new FileOutputStream(fileOut), encoding));
 * writeToFile(cachedStemmer, fileInput, output);//
 */

// ---
/*
 * file = new File(dir+"tester-utf8.txt"); fileInput = new BufferedReader(new
 * InputStreamReader( new FileInputStream(file), "utf-8")); fileOut = new
 * File(dir+"out-tester-utf8.txt"); output = new BufferedWriter(new
 * OutputStreamWriter( new FileOutputStream(fileOut), "UTF-8"));
 * writeToFile(cachedStemmer, fileInput, output); //
 */


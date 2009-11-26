package mokk.nlp.ocamorph;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.text.BreakIterator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * This class is actually used in the pipeline.
 * It is used to stem an input file useing a stemtable.
 * stemtable file format:
 * word1<TAB>stem1.1<TAB>stem1.2<TAB>stem1.3
 * word2<TAB>stem2.1<TAB>stem2.2
 * word3<TAB>stem3
 * 
 * @author bpgergo
 *
 */
public class CachedFileStemmer {
	protected static String defaultEncoding = "ISO-8859-2"; 
	protected static String SPACE = " ";
	protected static String WHITESPACE = "\\s";
	protected static String TAB = "\t";
	protected static String EMPTY = "";

	private Map<String, Set<String>> stemCache;

	/**
	 * path of the ocamorph binary resource
	 */
	protected String ocamorphResource;

	/**
	 * this is the stemmer to get the stem of a token
	 */
	protected OcamorphStemmer stemmer;
	
	/**
	 * encoding of the input file
	 */
	protected String fromEncoding; 
	/**
	 * encoding of the output file
	 */
	protected String toEncoding;

	/**
	 * The separator separates the word and the stems in the stemtable
	 */
	protected String separator; 
	
	private BreakIterator wordIterator = BreakIterator.getWordInstance();


	protected boolean debug = false;

	public CachedFileStemmer(){
		super();
		this.ocamorphResource = null;
		this.fromEncoding = defaultEncoding;
		this.toEncoding = defaultEncoding;
		this.separator = TAB;
	}

	public CachedFileStemmer(String separator){
		super();
		this.ocamorphResource = null;
		this.fromEncoding = defaultEncoding;
		this.toEncoding = defaultEncoding;
		this.separator = separator;
	}

	public CachedFileStemmer(String encoding, String separator){
		super();
		this.ocamorphResource = null;
		this.fromEncoding = encoding;
		this.toEncoding = encoding;
		this.separator = separator;
	}

	public CachedFileStemmer(String fromEncoding,
			String toEncoding, String separator){
		super();
		this.ocamorphResource = null;
		this.fromEncoding = fromEncoding;
		this.toEncoding = toEncoding;
		this.separator = separator;
	}
	
	public CachedFileStemmer(String ocamorphResource, String fromEncoding,
			String toEncoding, String separator) {
		super();
		this.ocamorphResource = ocamorphResource;
		this.fromEncoding = fromEncoding;
		this.toEncoding = toEncoding;
		this.separator = separator;
	}

	/**
	 * return the longest stem, useful with verbal particle (like
	 * széthajtogatták:szét,hajtogat)
	 * 
	 * @param word
	 * @return
	 */
	public String getStemFromCache(String word) {
		String result = EMPTY;
		Set<String> stems = stemCache.get(word);
		if (stems != null && !stems.isEmpty())
			for (String stem : stems) {
				if (stem.length() > result.length()) {
					result = stem;
				}
			}
		return result;
	}

	/**
	 * stem an input stream and write the stemmed result to the output stream
	 * 
	 * @param inputStream
	 * @param outputStream
	 */
	public void stemStream(InputStream inputStream, OutputStream outputStream) {
		if (stemCache == null){
			throw new RuntimeException("stemStream: where is the stem cache?");			
		}
		BufferedReader input = getBufferedReaderFromInputStream(inputStream);
		BufferedWriter out = getBufferedWriterFromOutputStream(outputStream);
		String line = null;
		try {
			try {
				while ((line = input.readLine()) != null) {

					wordIterator.setText(line);
					int start = wordIterator.first();
					int end = wordIterator.next();
					while (end != BreakIterator.DONE) {
						String word = line.substring(start, end);
						if (Character.isLetterOrDigit(word.charAt(0))) {
							word = getStemFromCache(word);
						}
						out.write(word);
						start = end;
						end = wordIterator.next();
					}
					out.newLine();
				}

			} finally {
				// ... Close reader and writer.
				input.close(); // Close to unlock.
				out.close(); // Close to unlock and flush to disk.
			}
		} catch (IOException e) {
			throw new RuntimeException("stemFile I/O Exception", e);
		}

	}

	/**
	 * write out the cache to the output stream
	 * 
	 * @param outputStream
	 */
	public void writeCache(OutputStream outputStream) {
		if (stemCache == null){
			throw new RuntimeException("writeCache: where is the stem cache?");			
		}
		
		BufferedWriter out = getBufferedWriterFromOutputStream(outputStream);
		try {
			try {
				for (String word : stemCache.keySet()) {
					out.write(word);
					for (String stem : stemCache.get(word)) {
						out.write(TAB);
						out.write(stem);
					}
					out.newLine();
				}
			} finally {
				out.close();
			}
		} catch (IOException e) {
			throw new RuntimeException("writeCache I/O exception", e);
		}
	}

	/**
	 * create a cache from an input stream
	 * 
	 * @param inputStream
	 */
	public void createCache(InputStream inputStream) {
		if (stemmer == null){
			throw new RuntimeException("createCache: where is the stemmer instance?");
		}
		BufferedReader input = getBufferedReaderFromInputStream(inputStream);
		String line = null;
		stemCache = new HashMap<String, Set<String>>();
		try {
			try {
				while ((line = input.readLine()) != null) {

					// instead of line.split(WHITESPACE) let us use
					// BreakIterator!
					// for (String word : line.split(WHITESPACE)){
					// //do not call stemmer if the word is already in cache
					// if (!stemCache.containsKey(word)){
					// stemCache.put(word, stemmer.getStems(word));
					// }
					// }

					wordIterator.setText(line);
					int start = wordIterator.first();
					int end = wordIterator.next();
					while (end != BreakIterator.DONE) {
						String word = line.substring(start, end);
						if (Character.isLetterOrDigit(word.charAt(0))) {
							// do not call stemmer if the word is already in
							// cache
							if (!stemCache.containsKey(word)) {
								stemCache.put(word, stemmer.getStems(word));
							}
						}
						start = end;
						end = wordIterator.next();
					}
				}
			} finally {
				input.close();
			}
		} catch (IOException e) {
			throw new RuntimeException("createCache I/O exception", e);
		}
	}

	/**
	 * Read stem cache from input stream. Cache file format:
	 * 
	 * word1<TAB>stem1.1<TAB>stem1.2<TAB>stem1.3
	 * 
	 * word2<TAB>stem2.1<TAB>stem2.2
	 * 
	 * word3<TAB>stem3
	 */
	public void readCache(InputStream inputStream) {
		stemCache = new HashMap<String, Set<String>>();
		BufferedReader reader = getBufferedReaderFromInputStream(inputStream);
		String line = null;
		try {
			try {
				while ((line = reader.readLine()) != null) {
					if (line.length() > 0) {
						boolean first = true;
						Set<String> set = null;
						for (String token : line.split(TAB)) {
							if (first) {
								first = false;
								set = stemCache.get(token);
								if (set == null) {
									set = new HashSet<String>();
									stemCache.put(token, set);
								}
							} else {
								set.add(token);
							}
						}
					}
				}
			} finally {
				reader.close();
			}
		} catch (IOException e) {
			throw new RuntimeException("readCache: I/O exception", e);
		}
	}

	private BufferedWriter getBufferedWriterFromOutputStream(
			OutputStream outputStream) {
		BufferedWriter writer = null;
		try {
			writer = new BufferedWriter(new OutputStreamWriter(outputStream,
					toEncoding));
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException("unsupported output file encoding:"
					+ fromEncoding, e);
		}
		return writer;
	}

	private BufferedReader getBufferedReaderFromInputStream(
			InputStream inputStream) {
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new InputStreamReader(inputStream,
					fromEncoding));
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException("unsupported input file encoding:"
					+ fromEncoding, e);
		}
		return reader;
	}

	public OcamorphStemmer getStemmer() {
		return stemmer;
	}

	public void setStemmer(OcamorphStemmer stemmer) {
		this.stemmer = stemmer;
	}

	public String getFromEncoding() {
		return fromEncoding;
	}

	public void setFromEncoding(String fromEncoding) {
		this.fromEncoding = fromEncoding;
	}

	public String getToEncoding() {
		return toEncoding;
	}

	public void setToEncoding(String toEncoding) {
		this.toEncoding = toEncoding;
	}

	public boolean isDebug() {
		return debug;
	}

	public void setDebug(boolean debug) {
		this.debug = debug;
	}

	public String getOcamorphResource() {
		return ocamorphResource;
	}

	public void setOcamorphResource(String ocamorphResource) {
		this.ocamorphResource = ocamorphResource;
	}

	public String getSeparator() {
		return separator;
	}

	public void setSeparator(String separator) {
		this.separator = separator;
	}

}

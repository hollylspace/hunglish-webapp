package mokk.nlp.ocamorph;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

/**
 * This class is actually used in the pipeline.
 * Stemming the input files is done in two steps.
 * First a stemtable is created which contains the stems
 * for every unique word.
 * The second step is done by this class: every word is 
 * replaced by its stem.
 * 
 * The first argument must be tokenized file, the second must be the cache file.
 * The input and output encoding will be ISO-8859-2 
 * 
 * @author bpgergo
 *
 */
public class FileStemmer {

	public static void main(String[] args) throws FileNotFoundException {
		
		CachedFileStemmer cachedFileStemmer = new CachedFileStemmer();
		
		if (args.length < 2) {
			usage();
		} 
		
		cachedFileStemmer.setStemmer(null);
		
		cachedFileStemmer.readCache(new FileInputStream(args[1]));
		
		cachedFileStemmer.stemStream(new FileInputStream(args[0]), System.out);
	}

	private static void usage(){
		System.err.println("This program stems a tokenized file, result is written to standard output \n" +
				"the first argument must be tokenized file, the second must be the cache file \n");
		System.err.println("example: mokk.nlp.ocamorph.FileStemmer input.tokenized.file cache.file > output.file < \n");
		System.exit(-1);
	}
	
}

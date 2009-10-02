package mokk.nlp.ocamorph;

public class CacheBuilder {

	/**
	 * ocamorph param
	 */
	protected static boolean blocking = false;//true;
	/**
	 * ocamorph param
	 */
	protected static boolean stopAtFirst = false;
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		CachedFileStemmer cachedFileStemmer = new CachedFileStemmer();
		
		if (args.length < 1) {
			usage();
		} else {
			cachedFileStemmer.setOcamorphResource(args[0]);
		}

		OcamorphWrapper ocamorphWrapper = new OcamorphWrapper(cachedFileStemmer.getOcamorphResource(),
				blocking, stopAtFirst, Compounds.No, Guess.NoGuess);
		ocamorphWrapper.setDebug(false);
		OcamorphStemmer stemmer = new OcamorphStemmer(ocamorphWrapper);
		stemmer.setDebug(false);
		
		cachedFileStemmer.setStemmer(stemmer);
		cachedFileStemmer.createCache(System.in);
		cachedFileStemmer.writeCache(System.out);
	}

	private static void usage(){
		System.err.println("This program builds a stem cache from an input file\n" +
				"usage: you must specify an ocamorph binary lexicon resource\n" +
				"input will be read from standard in, result will be written to standard out\n");
		System.err.println("example: mokk.nlp.ocamorph.CacheBuilder [ocamorph-resource] > cacheoutput.file < input.file\n");
		System.exit(-1);
	}

	
	public boolean isBlocking() {
		return blocking;
	}

	public void setBlocking(boolean blocking) {
		CacheBuilder.blocking = blocking;
	}

	public boolean isStopAtFirst() {
		return stopAtFirst;
	}

	public void setStopAtFirst(boolean stopAtFirst) {
		CacheBuilder.stopAtFirst = stopAtFirst;
	}
	
}

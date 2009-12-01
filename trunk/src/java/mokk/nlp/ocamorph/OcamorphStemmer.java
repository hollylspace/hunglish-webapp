package mokk.nlp.ocamorph;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * This class adds stemming feature to OcamorphWrapper. 
 * wrapper.analyze is called and 
 * analysis results will be returned through the callback function.
 * 
 * @author bpgergo
 * 
 */
public class OcamorphStemmer {
	private boolean debug = false;

	long analyzer = 0;
	OcamorphWrapper wrapper = null;
	String encoding;
	private int dashCompoundLengthLimit = 3;
	private boolean handleDashCompound = true;
	private static String DASH = "-";
	private static String PER = "/";
	private static String TAB = "\t";
	private static String QMARK = "?";

	public OcamorphStemmer(OcamorphWrapper wrapper) {
		this.wrapper = wrapper;
		this.analyzer = wrapper.getAnalyzerId();
		if (wrapper.getEncoding() == null) {
			throw new RuntimeException(
					"Ocamorph stemmer constructor: ocamorph wrapper string encoding must not be null");
		}
		this.encoding = wrapper.getEncoding();
	}

	
	/**
	* split the 
	*/
	public Set<String> getStems(String word) {
		if (handleDashCompound){
			return getStemsHandleDash(word);
		} else {
			return internalGetStems(word);
		}
	}
	
	/**
	 * get stems for a word and handle dash compounds, like
	 * Morpheus-szal, képviselő-testülettel, kisebb-nagyobb, Oscar-díjas, Orbán-kormánnyal
	 * @param word
	 * @return
	 */
	public Set<String> getStemsHandleDash(String word) {
		Set<String> stems = null;
		if (word != null){
			stems = new HashSet<String>();
			String[] splitted =  word.split(DASH);
			if (splitted != null && splitted.length > 0){
				for (String w : splitted){
					if (w.length() > dashCompoundLengthLimit){
						stems.addAll(internalGetStems(w));
					}
				}
			}
		}
		return stems;
	}

	private Set<String> internalGetStems(String word) {
		debug("internalGetStems:"+word);
		Set<String> stems = null;
		if (word != null) {
			List<String> analyzeResult = wrapper.analyze(word);
			
			stems = new HashSet<String>();
			for (String a : analyzeResult) {
				//String noTags = cutTags(a);
				//if (!stems.contains(noTags)) { //(!word.equals(noTags)) && 
				stems.addAll(cutTags(a));//noTags);
				//}
			}
			//if and only if the stems set is empty then add the original word
			if (stems.isEmpty()){
				stems.add(word);
			}
		}
		return stems;
	}

	/**
	 * cut the POS tags, they come after the first slash
	 * @param stemWithTags
	 * @return
	 */
	private Set<String> cutTags(String stemWithTags) {
		Set<String> stems = new HashSet<String>();
		
		String[] arr = stemWithTags.split(PER);
		if (arr != null && arr.length > 0) {
			stems.add(arr[0]);
		} else {
			arr = stemWithTags.split(TAB);
			if (arr != null && arr.length > 0) {
				for (int i = 0; i < arr.length; i++){
					String[] arr2 = arr[i].split(QMARK);
					if (arr2 != null && arr2.length > 0) {
						stems.add(arr2[0]);
					}					
				}
			} else {
				arr = stemWithTags.split(QMARK);
				if (arr != null && arr.length > 0) {
					stems.add(arr[0]);
				}					
				
			}			
		}
		return stems;
	}
	
	public boolean isHandleDashCompound() {
		return handleDashCompound;
	}

	public void setHandleDashCompound(boolean handleDashCompound) {
		this.handleDashCompound = handleDashCompound;
	}
	private void debug(String val){
		if (debug){
			//System.out.printlnln(val);
		}
	}

	public boolean isDebug() {
		return debug;
	}

	public void setDebug(boolean debug) {
		this.debug = debug;
	}

}

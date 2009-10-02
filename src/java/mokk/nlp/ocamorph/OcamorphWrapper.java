package mokk.nlp.ocamorph;

import java.io.UnsupportedEncodingException;
import java.util.LinkedList;
import java.util.List;

/**
 * JNI interface for Ocamorph. Constructor loads ocamorph engine and a specified binary resource.
 * 
 * @author bpgergo
 *
 */
public class OcamorphWrapper {
	
	private long analyzerId;
	private long engineId;

	
	private native static void initIDs();

	private native long init(String bin);

	// const ocamorph_engine engine, const int blocking, const int compounds,
	// const int stop_at_first, const int guess
	// valami hiba van az ocamorph-ban, mert a stop_at_first vezerli az
	// osszetettszosagot
	private native long make_analyzer(long engine, int blocking, int compounds,
			int stop_at_first, int guess);

	private native void analyze(long analyzer, byte[] word);
	
	static {
		//TODO FIXME how to define the library dynamically?
		System.loadLibrary("ocamorph");
		//initIDs();
	}

	/**
	 * the encoding required by the ocamorph lib
	 */
	private static String encoding = "ISO-8859-2";
	
	//private static boolean debug = false;
	
	/**
	 * analyze result (the callback will add the result strings)
	 */
	private List<String> analyzeResult = null;
	
	/**
	 * Loads a new Ocamorph engine, using the given binary resource and the arguments.
	 *  
	 * @param bin
	 * @param blocking
	 * @param stopAtFirst
	 * @param compounds
	 * @param guess
	 */
	public OcamorphWrapper(String bin, boolean blocking, boolean stopAtFirst, 
			Compounds compounds, Guess guess) {
		super();
		engineId = init(bin);
		int comp = compounds2Code(compounds);
		int gu = guessToCode(guess);
		analyzerId = make_analyzer(engineId, boolean2Code(blocking), boolean2Code(stopAtFirst),
				comp, gu);
		//debug("engineId:"+engineId);
		//debug("analyzerId:"+analyzerId);
		//debug = false;
	}
	
	
	/**
	 * This is the interface method for ocamorph analysis for the java side.
	 * @param ba
	 */
	public List<String> analyze(String word) {
		//debug("analyze:");
		analyzeResult = new LinkedList<String>();
		byte[] ba = null;
		try {
			ba = word.getBytes(encoding);
		} catch (UnsupportedEncodingException e1) {
			//System.err.println("Ocamorph analyze UnsupportedEncodingException: ");
			//e1.printStackTrace();
			throw new RuntimeException("OcamorphWrapper.analyze UnsupportedEncodingException", e1);
		}
		if (ba != null){
			//debug //printBytes(ba, "analizze:");
			analyze(analyzerId, ba);
		}
		return analyzeResult;
	}
	
	/**
	 * The C interface will call this method to return analysis results
	 */
	@SuppressWarnings("unused")
	private void callback(byte[] ana) {

		String s = null;
		try {
			// bpgergo 20090618 this was a bug
			// s = new String(ana);
			s = new String(ana, encoding);
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException("OcamorphWrapper.callback UnsupportedEncodingException", e);
			//System.err.println("callback new String(ana, encoding) UnsupportedEncodingException:");
			//e.printStackTrace();
		}
		
		analyzeResult.add(s);
		
		//if (s != null) {
			//debug("!callback recieved: ");
			// debug //printBytes(ana, s);
		//} else {
			//debug("callback s == null");
		//}
	}

	/* static argument conversion methods */
	
	private static int boolean2Code(boolean bool){
		if (bool){
			return 1; 
		} else {
			return 0;
		}
			
	}
	private static int compounds2Code(Compounds compounds){
		int comp = 0;
		switch (compounds) {
		case No:
			comp = 0;
			break;
		case Allow:
			comp = 1;
			break;
		}
		return comp;
	}

	private static int guessToCode(Guess guess){
		int gu = 0;
		switch (guess) {
		case NoGuess:
			gu = 0;
			break;
		case Fallback:
			gu = 1;
			break;
		case Global:
			gu = 2;
			break;
		}
		return gu;
	}

	public String getEncoding() {
		return encoding;
	}
	public long getAnalyzerId() {
		return analyzerId;
	}


	/*private static void debug(String string) {
		if (debug) {
			System.out.println(string);
		}
	}*/

	/* getter/setter methods */
		
	public boolean isDebug() {
		return false; //debug;
	}

	public void setDebug(boolean debug) {
		//OcamorphWrapper.debug = debug;
	}

	/* static debug methods */
	
	/*public static void printBytes(byte[] array, String name) {
		if (debug) {
			for (int k = 0; k < array.length; k++) {
				debug(name + "[" + k + "] = " + "0x" + byteToHex(array[k]));
			}
		}
	}*/

	/*static public String byteToHex(byte b) {
		// Returns hex String representation of byte b
		char hexDigit[] = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
				'a', 'b', 'c', 'd', 'e', 'f' };
		char[] array = { hexDigit[(b >> 4) & 0x0f], hexDigit[b & 0x0f] };
		return new String(array);
	}*/

	/*static public String charToHex(char c) {
		// Returns hex String representation of char c
		byte hi = (byte) (c >>> 8);
		byte lo = (byte) (c & 0xff);
		return byteToHex(hi) + byteToHex(lo);
	}*/


	
}

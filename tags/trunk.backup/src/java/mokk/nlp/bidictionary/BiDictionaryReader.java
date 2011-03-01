/*
 * Created on Dec 2, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package mokk.nlp.bidictionary;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * @author hp
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class BiDictionaryReader {
	
	
	public BiDictionaryReader() {
		
	}
	public BiDictionary readFromText(String fileName) throws IOException {
		BiDictionary dict = new BiDictionary();
		
		BufferedReader r = new BufferedReader(
				new InputStreamReader(new FileInputStream(fileName), "ISO-8859-2"));
		
		String line = null;
		
		while((line = r.readLine()) != null ) {
			int i = line.indexOf("@");
			if(i < 0) {
				throw new IOException("no @ found in the line:" + line);
			}
		
			String left = line.substring(0, i);
			String right = line.substring(i+1);
			
			dict.addTranslation(left, right);
		}
		r.close();
		return dict;
	}	
}

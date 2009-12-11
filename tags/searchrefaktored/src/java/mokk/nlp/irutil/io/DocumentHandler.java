/*
 * Created by Peter Halacsy <peter at halacsy.com>
 * 
 * This work is licensed under the Creative Commons 
 * Attribution License. To view a copy of this license, 
 * visit http://creativecommons.org/licenses/by/2.0/ 
 * or send a letter to Creative Commons, 559 Nathan Abbott Way, 
 * Stanford, California 94305, USA.
 * 
 * Created on Mar 23, 2005
 *
 */

package mokk.nlp.irutil.io;
import mokk.nlp.irutil.Document;

/*
 * @author hp
 *
 * Az implementalo osztalyoknak adja at a DocumentSource a beolvasott Document
 * objektumokat egyenkent. 
 */
public interface DocumentHandler {

    	public void processDocument(Document d) throws ProcessingException;
}

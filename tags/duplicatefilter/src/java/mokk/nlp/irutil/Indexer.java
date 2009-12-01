/*
 * Created by Peter Halacsy <peter at halacsy.com>
 * 
 * This work is licensed under the Creative Commons 
 * Attribution License. To view a copy of this license, 
 * visit http://creativecommons.org/licenses/by/2.0/ 
 * or send a letter to Creative Commons, 559 Nathan Abbott Way, 
 * Stanford, California 94305, USA.
 * 
 * Created on Apr 25, 2005
 *
 */

package mokk.nlp.irutil;

import java.io.IOException;

import mokk.nlp.irutil.io.ProcessingException;



/*
 * Indexer egyszeruen valamilyen forrasbol szarmazo dokumentumokat
 * indexeli. Feltehetoleg eloszor egy mappert fog keresni.
 *
 */
public interface Indexer {
    public String ROLE = Indexer.class.getName();

   public void index() throws ProcessingException, IOException;
}

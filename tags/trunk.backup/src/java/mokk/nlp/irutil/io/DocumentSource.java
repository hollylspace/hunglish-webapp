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

import java.io.IOException;



/*
 * @author hp
 *
 * Ezt implementalo osztalyok olvassak be a nyers dokumentumokat.
 */
public interface DocumentSource {
    public String ROLE = DocumentSource.class.getName();
    
    public void setHandler(DocumentHandler handler);
    
    public DocumentHandler getHandler();
    
    public void read() throws ProcessingException, IOException ;
    
}

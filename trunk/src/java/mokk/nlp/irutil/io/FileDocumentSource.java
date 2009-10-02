/*
 * Created by Peter Halacsy <peter at halacsy.com>
 * 
 * This work is licensed under the Creative Commons 
 * Attribution License. To view a copy of this license, 
 * visit http://creativecommons.org/licenses/by/2.0/ 
 * or send a letter to Creative Commons, 559 Nathan Abbott Way, 
 * Stanford, California 94305, USA.
 * 
 * Created on May 2, 2005
 *
 */

package mokk.nlp.irutil.io;

import java.io.File;
import java.io.IOException;

/*
 * DocumentSource that reads the documents from a file.
 * 
 */
public interface FileDocumentSource extends DocumentSource {
  
    public String ROLE = FileDocumentSource.class.getName();
    
    /*
     * Read and parse the file.
     */
    // public void read(String file) throws ProcessingException;
    
    /*
     * Read and parse the file.
     */
    public void read(File file) throws ProcessingException, IOException;
    
    /*
     * Sets the input file to read. setFile(f) and read() is the same
     * to read(f)
     */
    public void setInputFile(File file);
    
}

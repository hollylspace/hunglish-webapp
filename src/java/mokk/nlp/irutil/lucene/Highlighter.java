/*
 * Created by Peter Halacsy <peter at halacsy.com>
 * 
 * This work is licensed under the Creative Commons 
 * Attribution License. To view a copy of this license, 
 * visit http://creativecommons.org/licenses/by/2.0/ 
 * or send a letter to Creative Commons, 559 Nathan Abbott Way, 
 * Stanford, California 94305, USA.
 * 
 * Created on May 6, 2005
 *
 */

package mokk.nlp.irutil.lucene;

import org.apache.lucene.search.Query;


public interface Highlighter {

    public String ROLE = Highlighter.class.getName();
    
    public String highlight(String field, String text, Query q);
}

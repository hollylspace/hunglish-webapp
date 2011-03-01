/*
 * Created by Peter Halacsy <peter at halacsy.com>
 * 
 * This work is licensed under the Creative Commons 
 * Attribution License. To view a copy of this license, 
 * visit http://creativecommons.org/licenses/by/2.0/ 
 * or send a letter to Creative Commons, 559 Nathan Abbott Way, 
 * Stanford, California 94305, USA.
 * 
 * Created on Nov 28, 2004
 *
 */

package mokk.nlp.irutil;


/**
 * @author hp
 *
 */
public abstract interface Searcher {
    public String ROLE = Searcher.class.getName();
	
    public abstract SearchResult search(String left, String right, int start, int n) throws SearchException ;
}

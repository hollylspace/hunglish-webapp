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

package mokk.nlp.bicorpus.index;
import mokk.nlp.irutil.SearchException;
import mokk.nlp.irutil.SearchResult;
/**
 * @author hp
 *
 */
public  interface BiCorpusSearcher {
    public String ROLE = BiCorpusSearcher.class.getName();
	
    public  SearchResult search(SearchRequest request) throws SearchException ;
    
   
}

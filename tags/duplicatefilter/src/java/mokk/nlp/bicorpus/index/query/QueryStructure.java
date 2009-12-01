/*
 * Created by Peter Halacsy <peter at halacsy.com>
 * 
 * This work is licensed under the Creative Commons 
 * Attribution License. To view a copy of this license, 
 * visit http://creativecommons.org/licenses/by/2.0/ 
 * or send a letter to Creative Commons, 559 Nathan Abbott Way, 
 * Stanford, California 94305, USA.
 * 
 * Created on Jul 1, 2005
 *
 */

package mokk.nlp.bicorpus.index.query;
import java.util.*;
/**
 * @author hp
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class QueryStructure {
    List phrases;
    
    public QueryStructure() {
        phrases = new ArrayList();
    }
    
    public void addPhrase(QueryPhrase phrase) {
        phrases.add(phrase);
    }
    
    public QueryPhrase[] getPhrases() {
        return (QueryPhrase[]) phrases.toArray(new QueryPhrase[0]);
    }
    
    public String toString() {
        StringBuffer buff = new StringBuffer();
        
        Iterator it = phrases.iterator();
        
        while(it.hasNext()) {
            buff.append(it.next());
        }
        
        return buff.toString();
    }
}

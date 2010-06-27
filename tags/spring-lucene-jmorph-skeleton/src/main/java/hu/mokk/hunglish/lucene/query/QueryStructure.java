/**
 * 
 */
package hu.mokk.hunglish.lucene.query;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author Peter Halacsy <peter at halacsy.com>
 *
 */
public class QueryStructure {
    List<QueryPhrase> phrases;
    
    public QueryStructure() {
        phrases = new ArrayList<QueryPhrase>();
    }
    
    public void addPhrase(QueryPhrase phrase) {
        phrases.add(phrase);
    }
    
    public QueryPhrase[] getPhrases() {
        return (QueryPhrase[]) phrases.toArray(new QueryPhrase[0]);
    }
    
    public String toString() {
        StringBuffer buff = new StringBuffer();
        
        Iterator<QueryPhrase> it = phrases.iterator();
        
        while(it.hasNext()) {
            buff.append(it.next().toString());
        }
        
        return buff.toString();
    }
}

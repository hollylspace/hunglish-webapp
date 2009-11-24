/*
 * Created by Peter Halacsy <peter at halacsy.com>
 * 
 * This work is licensed under the Creative Commons 
 * Attribution License. To view a copy of this license, 
 * visit http://creativecommons.org/licenses/by/2.0/ 
 * or send a letter to Creative Commons, 559 Nathan Abbott Way, 
 * Stanford, California 94305, USA.
 * 
 * Created on Jun 15, 2005
 *
 */

package mokk.nlp.irutil.lucene.highlight;

import org.apache.lucene.search.Query;
import org.apache.lucene.search.highlight.QueryTermExtractor;
import org.apache.lucene.search.highlight.WeightedTerm;

/**
 * @author hp
 * 
 *         TODO To change the template for this generated type comment go to
 *         Window - Preferences - Java - Code Style - Code Templates
 */
public class SimpleQueryTermExtractor {
	public static String[] getTerms(Query q) {
		WeightedTerm[] weightedTerms = QueryTermExtractor.getTerms(q);

		String[] terms = new String[weightedTerms.length];

		for (int i = 0; i < weightedTerms.length; i++) {
			terms[i] = weightedTerms[i].getTerm();
			// System.out.println("query: " + terms[i]);
		}

		return terms;
	}
}

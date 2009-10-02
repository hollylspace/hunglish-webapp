/*
 * Created by Peter Halacsy <peter at halacsy.com>
 * 
 * This work is licensed under the Creative Commons 
 * Attribution License. To view a copy of this license, 
 * visit http://creativecommons.org/licenses/by/2.0/ 
 * or send a letter to Creative Commons, 559 Nathan Abbott Way, 
 * Stanford, California 94305, USA.
 * 
 * Created on Jun 12, 2005
 *
 */

package mokk.nlp.bicorpus.index.lucene;

import java.io.IOException;
import java.util.BitSet;
import java.util.Map;
import java.util.WeakHashMap;

import org.apache.lucene.index.IndexReader;

/**
 * @author hp
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class CachingSourceFilter extends SourceFilter {

	private final static String SOURCE_FIELD = "source";


	private transient Map cache;
	
	private SourceFilter filter = null;
	
	public CachingSourceFilter(String sourceId) {
	    
        super(sourceId);
        filter = new SourceFilter(sourceId);
	}

	public BitSet bits(IndexReader reader) throws IOException {
	    if (cache == null) {
	        cache = new WeakHashMap();
	      }

	      synchronized (cache) {  // check cache
	        BitSet cached = (BitSet) cache.get(reader);
	        if (cached != null) {
	          return cached;
	        }
	      }

	      final BitSet bits = filter.bits(reader);

	      synchronized (cache) {  // update cache
	        cache.put(reader, bits);
	      }

	      return bits;
	}


}


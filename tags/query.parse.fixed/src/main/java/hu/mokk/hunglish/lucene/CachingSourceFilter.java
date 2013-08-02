/**
 * 
 */
package hu.mokk.hunglish.lucene;

import java.io.IOException;
import java.util.BitSet;
import java.util.Map;
import java.util.WeakHashMap;

import org.apache.lucene.index.IndexReader;

/**
 * @author hp, bpgergo
 *
 */
public class CachingSourceFilter extends SourceFilter {

	private transient Map cache;
	
	private SourceFilter filter = null;
	
	public CachingSourceFilter(String fieldName, String fieldValue) {
	    
        super(fieldName, fieldValue);
        filter = new SourceFilter(fieldName, fieldValue);
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


/**
 * 
 */
package hu.mokk.hunglish.lucene;


import hu.mokk.hunglish.domain.Bisen;

import java.io.IOException;
import java.util.BitSet;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermDocs;
import org.apache.lucene.search.DocIdSet;
import org.apache.lucene.search.Filter;
import org.apache.lucene.util.DocIdBitSet;

/**
 * @author bpgergo
 * 
 * Users can provide a fieldName with the constructor.
 * If null, then a filter will be created on the Bisen.genreFieldName field
 */
public class SourceFilter extends Filter {

	protected String filterValue = null;
	protected String fieldName = Bisen.genreFieldName;

	public SourceFilter(String fieldName, String fieldValue) {
		this.filterValue = fieldValue;
		if (fieldName != null){
			this.fieldName = fieldName;
		}
	}

	public BitSet bits(IndexReader reader) throws IOException {
		BitSet bits = new BitSet(reader.maxDoc());
		
		TermDocs termDocs = reader.termDocs(new Term(fieldName, filterValue));

		if (termDocs == null) {
			return bits;
		}

		try {
			while (termDocs.next())
				bits.set(termDocs.doc());
		} finally {
			termDocs.close();
		}

		return bits;
	}

	@Override
	public DocIdSet getDocIdSet(IndexReader reader) throws IOException {
		return new DocIdBitSet(bits(reader));
	}

}

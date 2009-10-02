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

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermDocs;
import org.apache.lucene.search.Filter;

public class SourceFilter extends Filter {

    		protected final static String SOURCE_FIELD = "source";
       
    		protected String sourceId  = null;


        public SourceFilter(String sourceId) {
                this.sourceId = sourceId;
             
        }

        public BitSet bits(IndexReader reader) throws IOException {
                BitSet bits = new BitSet(reader.maxDoc());

                TermDocs termDocs = reader.termDocs(new Term(SOURCE_FIELD, sourceId));

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

}

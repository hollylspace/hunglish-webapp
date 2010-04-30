/**
 * 
 */
package hu.mokk.hunglish.lucene;

import java.io.File;
import java.io.IOException;

import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.SimpleFSDirectory;

/**
 * @author bpgergo
 * 
 */
public class Searcher {

	private String indexDir;
	private IndexSearcher searcher;
	private IndexReader indexReader;
	private LuceneQueryBuilder queryBuilder;
	
	synchronized public void initSearcher() {
		boolean readOnly = true;
		if (indexReader == null) {
			try {
				indexReader = IndexReader.open(new SimpleFSDirectory(new File(
						indexDir)), readOnly);
			} catch (CorruptIndexException e) {
				throw new RuntimeException("Cannot open index directory.", e);
			} catch (IOException e) {
				throw new RuntimeException("Cannot open index directory.", e);
			}
			searcher = new IndexSearcher(indexReader);
		}
		if (searcher == null){
			searcher = new IndexSearcher(indexReader);
		}
		if (queryBuilder == null){
			queryBuilder = new LuceneQueryBuilder();
		}
	}

	public void setIndexDir(String indexDir) {
		this.indexDir = indexDir;
	}

	public void setSearcher(IndexSearcher searcher) {
		this.searcher = searcher;
	}

	public void setIndexReader(IndexReader indexReader) {
		this.indexReader = indexReader;
	}

}

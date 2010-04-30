/**
 * 
 */
package hu.mokk.hunglish.lucene.analysis;

import hu.mokk.hunglish.domain.Bisen;
import hu.mokk.hunglish.jmorph.AnalyzerProvider;

import java.io.File;
import java.io.IOException;

import net.sf.jhunlang.jmorph.parser.ParseException;

import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.store.LockObtainFailedException;
import org.apache.lucene.store.SimpleFSDirectory;

/**
 * @author bpgergo
 * 
 */
public class Indexer {

	/**
	 * create a new index in the tmp dir or append docs to an index already
	 * existing in the main index dir
	 * 
	 * @author bpgergo
	 * 
	 */
	public enum CreateOrAppend {
		Create, Append
	}

	private AnalyzerProvider analyzerProvider;

	private Integer mergeFactor = 100;
	private Integer maxBufferedDocs = 1000;
	private String indexDir;
	private String tmpIndexDir;
	private IndexWriter indexWriter;

	synchronized public void initIndexWriter(CreateOrAppend createOrAppend)
			throws CorruptIndexException, LockObtainFailedException,
			IOException, IllegalAccessException, InstantiationException,
			ParseException {
		if (analyzerProvider == null) {
			throw new IllegalStateException(
					"Cannot create indexWriter. The analyzerProvider is null.");
		}
		boolean create = createOrAppend.compareTo(CreateOrAppend.Create) == 0;
		String dir = create ? tmpIndexDir : indexDir;
		if (dir == null) {
			throw new IllegalStateException(
					"Cannot create indexWriter. The directory is null.");
		}
		indexWriter = new IndexWriter(new SimpleFSDirectory(
				new File(dir)), analyzerProvider.getAnalyzer(), create,
				IndexWriter.MaxFieldLength.UNLIMITED);
		indexWriter.setMergeFactor(mergeFactor);
		indexWriter.setMaxBufferedDocs(maxBufferedDocs);

	}

	public void indexAll(CreateOrAppend createOrAppend)
			throws CorruptIndexException, LockObtainFailedException,
			IOException, IllegalAccessException, InstantiationException,
			ParseException {
		initIndexWriter(createOrAppend);
		Bisen.indexAll(indexWriter);
	}

	public void setAnalyzerProvider(AnalyzerProvider analyzerProvider) {
		this.analyzerProvider = analyzerProvider;
	}

}

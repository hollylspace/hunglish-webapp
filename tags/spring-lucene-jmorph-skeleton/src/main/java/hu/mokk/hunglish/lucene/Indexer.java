/**
 * 
 */
package hu.mokk.hunglish.lucene;

import hu.mokk.hunglish.domain.Bisen;
import hu.mokk.hunglish.jmorph.AnalyzerProvider;

import java.io.File;
import java.io.IOException;

import net.sf.jhunlang.jmorph.parser.ParseException;

import org.apache.commons.io.FileUtils;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.store.LockObtainFailedException;
import org.apache.lucene.store.SimpleFSDirectory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

/**
 * @author bpgergo
 * 
 */
@Configurable
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

	@Autowired
	private AnalyzerProvider analyzerProvider;

	private Integer mergeFactor = 100;
	private Integer maxBufferedDocs = 1000;
	private String indexDir;
	private String tmpIndexDir;

	private IndexWriter indexWriter;
	//private CreateOrAppend createOrAppend = CreateOrAppend.Create;

	private void initIndexer(CreateOrAppend createOrAppend) {
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
		try {
			if (create) {
				deleteTmpDirectory();
			}
			indexWriter = new IndexWriter(new SimpleFSDirectory(new File(dir)),
					analyzerProvider.getAnalyzer(), create,
					IndexWriter.MaxFieldLength.UNLIMITED);
		} catch (CorruptIndexException e) {
			throw new RuntimeException("Cannot open index directory.", e);
		} catch (LockObtainFailedException e) {
			throw new RuntimeException("Cannot open index directory.", e);
		} catch (IOException e) {
			throw new RuntimeException("Cannot open index directory.", e);
		}
		if (mergeFactor != null) {
			indexWriter.setMergeFactor(mergeFactor);
		}
		indexWriter.setMaxBufferedDocs(maxBufferedDocs);
	}

	private void reCreateDir(File dir) {
		try {
			FileUtils.deleteQuietly(dir);
			FileUtils.forceMkdir(dir);
		} catch (IOException e) {
			throw new RuntimeException("Cannot recreate directory:" + dir, e);
		}
	}

	public void deleteTmpDirectory() throws IOException {
		reCreateDir(new File(tmpIndexDir));
	}

	synchronized public void indexAll(CreateOrAppend createOrAppend)
			throws CorruptIndexException, LockObtainFailedException,
			IOException, IllegalAccessException, InstantiationException,
			ParseException {
		initIndexer(createOrAppend);
		Bisen.indexAll(indexWriter);
		indexWriter.close();
	}

	synchronized public void indexDoc(Long docId) throws CorruptIndexException,
			LockObtainFailedException, IOException, IllegalAccessException,
			InstantiationException, ParseException {
		initIndexer(CreateOrAppend.Create);
		Bisen.indexDoc(indexWriter, docId);
		indexWriter.close();
	}

	synchronized public void mergeTmpIndex() {
		boolean readOnly = true;
		try {
			IndexReader indexReader = IndexReader.open(new SimpleFSDirectory(
					new File(tmpIndexDir)), readOnly);
			initIndexer(CreateOrAppend.Append);
			indexWriter.addIndexes(indexReader);
			indexReader.close();
			indexWriter.close();
		} catch (CorruptIndexException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public void setAnalyzerProvider(AnalyzerProvider analyzerProvider) {
		this.analyzerProvider = analyzerProvider;
	}

	public void setMergeFactor(Integer mergeFactor) {
		this.mergeFactor = mergeFactor;
	}

	public void setMaxBufferedDocs(Integer maxBufferedDocs) {
		this.maxBufferedDocs = maxBufferedDocs;
	}

	public void setIndexDir(String indexDir) {
		this.indexDir = indexDir;
	}

	public void setTmpIndexDir(String tmpIndexDir) {
		this.tmpIndexDir = tmpIndexDir;
	}

}

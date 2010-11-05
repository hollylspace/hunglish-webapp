/**
 * 
 */
package hu.mokk.hunglish.lucene;

import hu.mokk.hunglish.domain.Bisen;
import hu.mokk.hunglish.jmorph.AnalyzerProvider;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

import net.sf.jhunlang.jmorph.parser.ParseException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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

	transient private static Log logger = LogFactory.getLog(Bisen.class);

	@Autowired
	private AnalyzerProvider analyzerProvider;

	private Integer mergeFactor = 100;
	private Integer maxBufferedDocs = 1000;
	private String indexDir;
	private String tmpIndexDir;

	private IndexWriter indexWriter;
	//private CreateOrAppend createOrAppend = CreateOrAppend.Create;

	private void initIndexer(Boolean tmp) {
		if (analyzerProvider == null) {
			throw new IllegalStateException(
					"Cannot create indexWriter. The analyzerProvider is null.");
		}
		String dir = tmp ? tmpIndexDir : indexDir;
		if (dir == null) {
			throw new IllegalStateException(
					"Cannot create indexWriter. The directory is null.");
		}
		try {
			if (tmp) {
				deleteTmpDirectory();
			}
		} catch (IOException e) {
			throw new RuntimeException("IOException, cannot delete tmp directory.", e);
		}

		try {
			indexWriter = new IndexWriter(new SimpleFSDirectory(new File(dir)),
					analyzerProvider.getAnalyzer(), tmp,
					IndexWriter.MaxFieldLength.UNLIMITED);
		} catch (CorruptIndexException e) {
			throw new RuntimeException("CorruptIndexException, cannot open index directory: "+dir, e);
		} catch (LockObtainFailedException e) {
			throw new RuntimeException("LockObtainFailedException, cannot open index directory: "+dir, e);
		} catch (IOException e) {
			throw new RuntimeException("IOException, cannot open index directory: "+dir, e);
		}
		if (mergeFactor != null) {
			indexWriter.setMergeFactor(mergeFactor);
		}
		indexWriter.setMaxBufferedDocs(maxBufferedDocs);
	}

	private void reCreateDir(File dir) {
		try {
			Collection coll = FileUtils.listFiles(dir, null, false);
			if (coll.size() > 0){
				FileUtils.deleteQuietly(dir);
				FileUtils.forceMkdir(dir);
			}
		} catch (IOException e) {
			throw new RuntimeException("Cannot recreate directory:" + dir, e);
		}
	}

	public void deleteTmpDirectory() throws IOException {
		File dir = new File(tmpIndexDir); 
		logger.info("exists? "+dir.exists());
		logger.info("is it a dir? "+dir.isDirectory());
		logger.info("recreate dir: "+dir.getAbsolutePath());
		
		reCreateDir(dir);
	}

	synchronized public void indexAll(boolean tmp)
			throws CorruptIndexException, LockObtainFailedException,
			IOException, IllegalAccessException, InstantiationException,
			ParseException {
		initIndexer(tmp);
		logger.info("init indexer done");
		Bisen.indexAll(indexWriter);
		logger.info("index all done");
		indexWriter.optimize();
		indexWriter.close();
	}

	/*
	synchronized public void indexDoc(Long docId, boolean tmp) throws CorruptIndexException,
			LockObtainFailedException, IOException, IllegalAccessException,
			InstantiationException, ParseException {
		initIndexer(tmp);
		Bisen.indexDoc(indexWriter, docId);
		indexWriter.close();
	} //*/

	synchronized public void mergeTmpIndex() {
		boolean readOnly = true;
		try {
			IndexReader indexReader = IndexReader.open(new SimpleFSDirectory(
					new File(tmpIndexDir)), readOnly);
			initIndexer(false);
			indexWriter.addIndexes(indexReader);
			indexReader.close();
			indexWriter.close();
		} catch (CorruptIndexException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			logger.error("Index merge error", e);
			throw new RuntimeException(e);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			logger.error("Index merge error", e);
			throw new RuntimeException(e);			
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

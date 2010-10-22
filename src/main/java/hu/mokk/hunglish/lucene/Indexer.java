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
			indexWriter = new IndexWriter(new SimpleFSDirectory(new File(dir)),
					analyzerProvider.getAnalyzer(), tmp,
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

	synchronized public void indexAll(boolean tmp)
			throws CorruptIndexException, LockObtainFailedException,
			IOException, IllegalAccessException, InstantiationException,
			ParseException {
		initIndexer(tmp);
System.out.println("-----------------------------------------------------------------------------");		
System.out.println("--------------------------init indexer done ---------------------------------");		
System.out.println("-----------------------------------------------------------------------------");		
		Bisen.indexAll(indexWriter);
System.out.println("-----------------------------------------------------------------------------");		
System.out.println("--------------------------indexerall   done ---------------------------------");		
System.out.println("-----------------------------------------------------------------------------");		
		indexWriter.close();
	}

	synchronized public void indexDoc(Long docId, boolean tmp) throws CorruptIndexException,
			LockObtainFailedException, IOException, IllegalAccessException,
			InstantiationException, ParseException {
		initIndexer(tmp);
		Bisen.indexDoc(indexWriter, docId);
		indexWriter.close();
	}

	synchronized public void mergeTmpIndex() {
		boolean readOnly = true;
		try {
			IndexReader indexReader = IndexReader.open(new SimpleFSDirectory(
					new File(tmpIndexDir)), readOnly);
			initIndexer(false);
System.out.println("-----------------------------------------------------------------------------");		
System.out.println("--------------------------merge init indexer done ---------------------------------");		
System.out.println("-----------------------------------------------------------------------------");		
			
			indexWriter.addIndexes(indexReader);
System.out.println("-----------------------------------------------------------------------------");		
System.out.println("--------------------------merge done ---------------------------------");		
System.out.println("-----------------------------------------------------------------------------");		
			
			indexReader.close();
			indexWriter.close();
System.out.println("-----------------------------------------------------------------------------");		
System.out.println("--------------------------merge closed ---------------------------------");		
System.out.println("-----------------------------------------------------------------------------");		
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

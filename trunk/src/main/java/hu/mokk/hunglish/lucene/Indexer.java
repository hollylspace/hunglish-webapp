/**
 * 
 */
package hu.mokk.hunglish.lucene;

import hu.mokk.hunglish.domain.Bisen;
import hu.mokk.hunglish.domain.BisenState;
import hu.mokk.hunglish.domain.BisenState.BisenOperation;
import hu.mokk.hunglish.domain.BisenState.State;
import hu.mokk.hunglish.jmorph.AnalyzerProvider;
import hu.mokk.hunglish.job.MockJob;
import hu.mokk.hunglish.util.IndexerHelper;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.spell.Dictionary;
import org.apache.lucene.search.spell.LuceneDictionary;
import org.apache.lucene.search.spell.SpellChecker;
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

	transient private static Log logger = LogFactory.getLog(Indexer.class);

	@Autowired
	private AnalyzerProvider analyzerProvider;

	private Integer mergeFactor;
	private Integer mergeFactorTemp;
	private Integer maxBufferedDocs;
	private String indexDir;
	private String tmpIndexDir;
	private String spellIndexDir;
	private String spellIndexDirHu;
	private String uploadDir;
	private String uploadJobPath;

	@Autowired
	private DataSource dataSource;

	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	@Autowired
	private MockJob mockJob;

	/**
	 * batch size for indexing operations
	 */
	private Integer dbBatchSize;

	@Autowired
	private EntityManagerFactory entityManagerFactory;

	private static Indexer instance;

	public Indexer() {
		if (instance == null) {
			instance = this;
		} else {
			throw new IllegalStateException("Indexer is singleton");
		}
	}

	private IndexWriter initIndexer(Boolean temporary) {

		IndexWriter indexWriter = null;
		if (analyzerProvider == null) {
			throw new IllegalStateException(
					"Cannot create indexWriter. The analyzerProvider is null.");
		}
		String dir = temporary ? tmpIndexDir : indexDir;
		if (dir == null) {
			throw new IllegalStateException(
					"Cannot create indexWriter. The directory is null.");
		}
		if (temporary) {
			deleteTmpDirectory();
		}

		try {
			indexWriter = new IndexWriter(new SimpleFSDirectory(new File(dir)),
					analyzerProvider.getAnalyzer(), temporary,
					IndexWriter.MaxFieldLength.UNLIMITED);
		} catch (FileNotFoundException e) {
			try {
				logger.warn(
						"Error while opening index. Now trying to create new empty index.",
						e);
				indexWriter = new IndexWriter(new SimpleFSDirectory(new File(
						dir)), analyzerProvider.getAnalyzer(), true,
						IndexWriter.MaxFieldLength.UNLIMITED);
			} catch (Exception e1) {
				throw new RuntimeException(
						"Previous index not found. Cannot create new empty index: "
								+ dir, e1);
			}

		} catch (CorruptIndexException e) {
			throw new RuntimeException(
					"CorruptIndexException, cannot open index directory: "
							+ dir, e);
		} catch (LockObtainFailedException e) {
			throw new RuntimeException(
					"LockObtainFailedException, cannot open index directory: "
							+ dir, e);
		} catch (IOException e) {
			throw new RuntimeException(
					"IOException, cannot open index directory: " + dir, e);
		}
		if (temporary) {
			if (mergeFactor != null) {
				indexWriter.setMergeFactor(mergeFactorTemp);
			}
		} else {
			if (mergeFactor != null) {
				indexWriter.setMergeFactor(mergeFactor);
			}
		}
		if (maxBufferedDocs != null) {
			indexWriter.setMaxBufferedDocs(maxBufferedDocs);
		}
		return indexWriter;
	}

	public void deleteTmpDirectory() {
		IndexerHelper.reCreateDir(tmpIndexDir);
	}

	private void deleteBisenFromIndex(Bisen bisen, IndexWriter iwriter,
			Statement jdbcStatement) {
		try {
			Term term = new Term(Bisen.idFieldName, bisen.getId().toString());
			iwriter.deleteDocuments(term);
			jdbcStatement.addBatch("update bisen set state = '"
					+ BisenState
							.newState(bisen, BisenOperation.DELETEFROMINDEX)
					+ "' where id=" + bisen.getId());
		} catch (Exception e) {
			try {
				logger.error("error while deleting bisen:" + bisen, e);
				jdbcStatement.addBatch("update bisen " + "set state = '"
						+ BisenState.newState(bisen, BisenOperation.ERROR)
						+ "' where id=" + bisen.getId());
			} catch (SQLException e1) {
				// e1.printStackTrace();
				logger.fatal("DELETING error for bisen:" + bisen, e);
				throw new RuntimeException(
						"Error while deleting from index and updating error status on bisen",
						e1);
			}
			throw new RuntimeException("Error while deleting from index", e);
		}
	}

	private void indexBisen(Bisen bisen, IndexWriter iwriter,
			Statement jdbcStatement) {
		try {
			iwriter.addDocument(bisen.toLucene());
			jdbcStatement.addBatch("update bisen set state = '"
					+ BisenState.newState(bisen, BisenOperation.ADD2INDEX)
					+ "' where id=" + bisen.getId());
		} catch (Exception e) {
			try {
				jdbcStatement.addBatch("update bisen " + "set state = '"
						+ BisenState.newState(bisen, BisenOperation.ERROR)
						+ "' where id=" + bisen.getId());
			} catch (SQLException e1) {
				// e1.printStackTrace();
				logger.fatal("Exception in Catch block for "
						+ "Indexing error for bisen:", e1);
				throw new RuntimeException(
						"Error while indexing and updating error status on bisen",
						e1);

			}
			logger.error("Indexing error for bisen:" + bisen, e);
			throw new RuntimeException(e);
		}
	}

	private List<Bisen> getBisensByState(EntityManager em, List<State> states) {
		List<Bisen> result = new ArrayList<Bisen>();
		for (State state : states) {
			result = em
					.createQuery(
							"from Bisen where state = '" + state.toString()
									+ "' order by id")
					.setMaxResults(dbBatchSize).getResultList();
			if (result != null && result.size() > 0) {
				break;
			}
		}
		return result;
	}

	/**
	 * delete from the index the next batch of size BATCH_SIZE 1) get the next
	 * batch of Bisens from database, continue if not empty 2) for all bisen:
	 * delete from index, add update statement to batch 3) execute database
	 * batch updates
	 * 
	 * @param jdbcConnection
	 * @param indexWriter
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public boolean deleteBatch(Connection jdbcConnection,
			IndexWriter indexWriter) {
		// the batch statement
		Statement jdbcStatement = IndexerHelper
				.getJdbcStatement(jdbcConnection);

		EntityManager em = null;
		boolean result = false;
		List<Bisen> bisens = new ArrayList<Bisen>();
		try {
			// 1) get the next batch from database
			em = entityManagerFactory.createEntityManager();
			bisens = getBisensByState(
					em,
					BisenState
							.waitingForOperation(BisenOperation.DELETEFROMINDEX)); // State.E.toString());
			result = (bisens != null) && (bisens.size() > 0);
			// if (!result) {
			// bisens = getBisensByState(em, State.R.toString());
			// result = (bisens != null) && (bisens.size() > 0);
			// }
			logger.info("DELETE FROM INDEX get the next batch from database done resultList size:"
					+ bisens.size());

			if (result) {
				// 2) for all bisen: delete from index, add update statement to
				// batch
				for (Bisen bisen : bisens) {
					deleteBisenFromIndex(bisen, indexWriter, jdbcStatement);
				}
				// 3) execute database batch updates
				IndexerHelper.executeBatchUpdate(jdbcConnection, jdbcStatement);
			}
		} finally {
			IndexerHelper.closeStatement(jdbcStatement);
			bisens.clear();
			bisens = null;
			if (em != null) {
				em.clear();
				em.close();
			}
		}
		return result;
	}

	/**
	 * index the next batch of size BATCH_SIZE 1) get the next batch of Bisens
	 * from database, continue if not empty 2) clear the temporary index dir,
	 * and initialize indexWriter on the temporary directory 3) call indexBisen
	 * for all Bisens and store corresponding database updates in a batch
	 * jdbcStatement 4) finalyze temporary index and merge it into main index 5)
	 * execute database batch updates
	 * 
	 * @param jdbcConnection
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public boolean indexBatch(Connection jdbcConnection) {
		// the batch statement
		Statement jdbcStatement = IndexerHelper
				.getJdbcStatement(jdbcConnection);

		EntityManager em = null;
		boolean result = false;
		List<Bisen> bisens = new ArrayList<Bisen>();
		try {
			// 1) get the next batch from database
			em = entityManagerFactory.createEntityManager();
			bisens = getBisensByState(em,
					BisenState.waitingForOperation(BisenOperation.ADD2INDEX)); // State.I.toString());
			result = (bisens != null) && (bisens.size() > 0);
			logger.info("get the next batch from database done resultList size:"
					+ bisens.size());

			if (result) {
				// 2) clear the temporary index dir, and initialize indexWriter
				// on the temporary directory
				// +
				// 3) call indexBisen for all Bisens and store corresponding
				// database updates in a batch jdbcStatement
				// 4) finalyze temporary index
				indexBisens(bisens, jdbcStatement);

				// 4) cont... merge temporary index into main index
				mergeTmpIndex();

				// 5) execute database batch updates
				IndexerHelper.executeBatchUpdate(jdbcConnection, jdbcStatement);
			}
		} finally {
			IndexerHelper.closeStatement(jdbcStatement);
			bisens.clear();
			bisens = null;
			if (em != null) {
				em.clear();
				em.close();
			}
		}
		return result;
	}

	private void indexBisens(List<Bisen> bisens, Statement jdbcStatement) {
		IndexWriter indexWriter = initIndexer(true);
		logger.info("-------clear the temporary index dir, and initialize indexWriter on the temporary directory done-----");
		try {
			for (Bisen bisen : bisens) {
				indexBisen(bisen, indexWriter, jdbcStatement);
			}
			logger.info("-------call indexBisen for all Bisens and store corresponding database updates in a batch jdbcStatement done-----");
			logger.info("------indexing batch done in memory.");

		} finally {
			IndexerHelper.closeIndexWriter(indexWriter);
			logger.info("-------temporary index written to disk done-----");
		}

	}

	public void optimizeIndex() {
		IndexWriter indexWriter = initIndexer(false);
		try {
			logger.debug("index optimize start");
			indexWriter.optimize();
			logger.debug("index optimize finished");
		} catch (CorruptIndexException e) {
			logger.fatal("CorruptIndexException while optimizing index", e);
			throw new RuntimeException(e);
		} catch (IOException e) {
			logger.fatal("IOException while optimizing index", e);
			throw new RuntimeException(e);
		}
		IndexerHelper.closeIndexWriter(indexWriter);
	}

	/* true=temp that is index will be created in hunglishIndexTmp, false=main */
	/**
	 * Create a jdbcConnection calls indexBatch until it returns false using the
	 * connection clear up connection
	 */
	synchronized public void indexAll() {
		Connection jdbcConnection = null;
		try {

			jdbcConnection = IndexerHelper.getJdbcConnection(dataSource);
			logger.info("db connection initialized");

			boolean indexchanged = deleteAllFromIndex(jdbcConnection);
			indexchanged |= addAllToIndex(jdbcConnection);
			
			if (indexchanged){
				optimizeIndex();
			}
		} catch (SQLException sex) {

			logger.error("Couldn't initialize database connection for indexing.", sex);

		} finally {
			IndexerHelper.closeConnection(jdbcConnection);
			logger.info("db connection closed");
		}
	}


	public void reBuildSpellIndex(){
		logger.debug("About to rebuild spell indexes, spellIndexDir:"+spellIndexDir);
		logger.debug("spellIndexDirHu:"+spellIndexDirHu);
		SpellIndexBuilder.rebuildSpellIndex(Bisen.enSentenceFieldName, indexDir, spellIndexDir);
		SpellIndexBuilder.rebuildSpellIndex(Bisen.huSentenceFieldName, indexDir, spellIndexDirHu);
		logger.debug("Done rebuild spell indexes");		
	}
	
	private boolean addAllToIndex(Connection jdbcConnection) {
		boolean result = false;
		int batchIndex = 0;
		boolean keepItOn = true;
		while (keepItOn) {
			keepItOn = indexBatch(jdbcConnection);
			result |= keepItOn;
			logger.info("<<<<<< finished indexing batch at " + ++batchIndex
					* dbBatchSize);
		}
		logger.info("-----indexing ::: all batches done--");
		return result;
	}

	public boolean deleteAllFromIndex(Connection jdbcConnection) {
		boolean result = false;
		IndexWriter indexWriter = initIndexer(false);
		boolean keepItOn = true;
		int batchIndex = 0;
		try {
			while (keepItOn) {
				keepItOn = deleteBatch(jdbcConnection, indexWriter);
				result |= keepItOn;
				logger.info("<<<<<< DELETE FROM INDEX batch at " + ++batchIndex
						* dbBatchSize);

			}
			logger.info("-----DELETE FROM INDEX ::: all batches done--");
			IndexerHelper.closeIndexWriter(indexWriter);
			logger.info("-----DELETE FROM INDEX ::: indexwriter closed --");
		} catch (Exception e) {
			logger.fatal("DELETE FROM INDEX Exception while optimizing index",
					e);
			throw new RuntimeException(e);
		}
		return result;
	}

	synchronized private void mergeTmpIndex() {
		boolean readOnly = true;
		IndexWriter indexWriter = null;
		IndexReader indexReader = null;
		try {

			try {
				indexWriter = initIndexer(false);
				indexReader = IndexReader.open(new SimpleFSDirectory(new File(
						tmpIndexDir)), readOnly);
				indexWriter.addIndexes(indexReader);
				logger.info("------indexreader addIndexes done----");

			} catch (Exception e) {
				logger.error("Index merge error", e);
				throw new RuntimeException(e);
			}
		} finally {
			IndexerHelper.closeIndexWriter(indexWriter);
			try {
				if (indexReader != null) {
					indexReader.close();
				}
				logger.info("------indexreader close done----");
			} catch (Exception e) {
				logger.fatal("indexReader close error in Finally block", e);
				throw new RuntimeException(e);
			}
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

	public String getUploadDir() {
		return uploadDir;
	}

	public void setUploadDir(String uploadDir) {
		this.uploadDir = uploadDir;
	}

	public String getUploadJobPath() {
		return uploadJobPath;
	}

	public void setUploadJobPath(String uploadJobPath) {
		this.uploadJobPath = uploadJobPath;
	}

	public static Indexer getInstance() {
		return instance;
	}

	public Integer getMergeFactorTemp() {
		return mergeFactorTemp;
	}

	public void setMergeFactorTemp(Integer mergeFactorTemp) {
		this.mergeFactorTemp = mergeFactorTemp;
	}

	public Integer getDbBatchSize() {
		return dbBatchSize;
	}

	public void setDbBatchSize(Integer dbBatchSize) {
		this.dbBatchSize = dbBatchSize;
	}

	public MockJob getMockJob() {
		return mockJob;
	}

	public void setMockJob(MockJob mockJob) {
		this.mockJob = mockJob;
	}

	public void setSpellIndexDir(String spellIndexDir) {
		this.spellIndexDir = spellIndexDir;
	}

	public String getSpellIndexDir() {
		return spellIndexDir;
	}

	public void setSpellIndexDirHu(String spellIndexDirHu) {
		this.spellIndexDirHu = spellIndexDirHu;
	}

	public String getSpellIndexDirHu() {
		return spellIndexDirHu;
	}
	
}

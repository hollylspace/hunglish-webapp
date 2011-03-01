/**
 * 
 */
package hu.mokk.hunglish.lucene;

import hu.mokk.hunglish.domain.Bisen;
import hu.mokk.hunglish.jmorph.AnalyzerProvider;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
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
	private String uploadDir;
	private String uploadJobPath;

	/**
	 *besides JPA/Hibernate, indexer also uses  pure JDBC, for updating bisen table 
	 */
	private String dbUrl;
	private String dbDriver;
	private String dbUser;
	private String dbPassword;
	private Integer dbBatchSize;

	
	@Autowired
	private EntityManagerFactory entityManagerFactory;

	private static Indexer instance;
	public Indexer(){
		if (instance == null){
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
			indexWriter = new IndexWriter(new SimpleFSDirectory( new File(dir)),
					analyzerProvider.getAnalyzer(), temporary,
					IndexWriter.MaxFieldLength.UNLIMITED);
		} catch (FileNotFoundException e) {
			try {
				logger.warn("Error while opening index. Now trying to create new empty index.", e);
				indexWriter = new IndexWriter(new SimpleFSDirectory( new File(dir)),
						analyzerProvider.getAnalyzer(), true,
						IndexWriter.MaxFieldLength.UNLIMITED);
			} catch (Exception e1) {
				throw new RuntimeException(
						"Previous index not found. Cannot create new empty index: "+ dir, e1);
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
		if (temporary){
			if (mergeFactor != null) {
				indexWriter.setMergeFactor(mergeFactorTemp);
			}
		} else {
			if (mergeFactor != null) {
				indexWriter.setMergeFactor(mergeFactor);
			}
		}
		if (maxBufferedDocs != null){
			indexWriter.setMaxBufferedDocs(maxBufferedDocs);
		}
		return indexWriter;
	}

	private void reCreateDir(String theDir) {
		File dir = null;
		try {
			dir = new File(theDir);
			FileUtils.deleteQuietly(dir);
			FileUtils.forceMkdir(dir);
		} catch (IOException e) {
			throw new RuntimeException("Cannot recreate directory:" + theDir, e);
		}
	}

	public void deleteTmpDirectory() {
		reCreateDir(tmpIndexDir);
	}


	private Connection getJdbcConnection() {
		Connection con = null;
		try {
			Class.forName(dbDriver);
			con = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
			con.setAutoCommit(false);
		} catch (Exception e) {
			logger.error("Cannot create connection", e);
			throw new RuntimeException(e);
		}
		return con;
	}

	private Statement getJdbcStatement(Connection con) {

		Statement st = null;
		try {
			st = con.createStatement();// java.sql.ResultSet.TYPE_FORWARD_ONLY,java.sql.ResultSet.CONCUR_READ_ONLY);
			// st.setFetchSize(Integer.MIN_VALUE);
		} catch (SQLException e) {
			logger.error("Cannot create statement", e);
			throw new RuntimeException(e);
		}
		return st;
	}

	private void deleteBisenFromIndex(Bisen bisen, IndexWriter iwriter,
			Statement jdbcStatement) {
		try {
			Term term = new Term(Bisen.idFieldName, bisen.getId().toString());
			iwriter.deleteDocuments(term);
			String newState = "N";
			if ("R".equals(bisen.getState())){
				newState = "I";
			}
			jdbcStatement.addBatch(
					"update bisen set state = '"+newState+"' where id=" + bisen.getId());
		} catch (Exception e) {
			try {
				jdbcStatement.addBatch("update bisen "
						+ "set state = 'O' where id=" + bisen.getId());
			} catch (SQLException e1) {
				e1.printStackTrace();
				logger.fatal("Exception in Catch block for "
						+ "Indexing error for bisen:", e1);
			}
			logger.error("Indexing error for bisen:" + bisen, e);
			throw new RuntimeException(e);
		}
	}
	
	private void indexBisen(Bisen bisen, IndexWriter iwriter,
			Statement jdbcStatement) {
		try {
			iwriter.addDocument(bisen.toLucene());
			jdbcStatement.addBatch(
					"update bisen set state = 'X' where id=" + bisen.getId());
		} catch (Exception e) {
			try {
				jdbcStatement.addBatch("update bisen "
						+ "set state = 'O' where id=" + bisen.getId());
			} catch (SQLException e1) {
				e1.printStackTrace();
				logger.fatal("Exception in Catch block for "
						+ "Indexing error for bisen:", e1);
			}
			logger.error("Indexing error for bisen:" + bisen, e);
			throw new RuntimeException(e);
		}
	}

	private List<Bisen> getBisensByState(EntityManager em, String state){
		return em.createQuery("from Bisen where state = '"+state+"' order by id") 
			.setMaxResults(dbBatchSize).getResultList();
	}
	
	/**
	 * delete from the index the next batch of size BATCH_SIZE 
	 * 1) get the next batch of Bisens from database, continue if not empty
	 * 2) for all bisen: delete from index, add update statement to batch
	 * 3) execute database batch updates 
	 * @param jdbcConnection
	 * @param indexWriter
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public boolean deleteBatch(Connection jdbcConnection, IndexWriter indexWriter) {
		 //the batch statement
		Statement jdbcStatement = getJdbcStatement(jdbcConnection);

		EntityManager em = null;
		boolean result = false;
		List<Bisen> bisens = new ArrayList<Bisen>();
		try {
			//1) get the next batch from database 
			em = entityManagerFactory.createEntityManager();
			bisens =getBisensByState(em, "E");
			result = (bisens != null) && (bisens.size() > 0);
			if (!result){
				bisens =getBisensByState(em, "R");
				result = (bisens != null) && (bisens.size() > 0);
			}
			logger.info("DELETE FROM INDEX get the next batch from database done resultList size:"+ bisens.size());
			
			if (result) {
				//2) for all bisen: delete from index, add update statement to batch
				for (Bisen bisen : bisens) {
					deleteBisenFromIndex(bisen, indexWriter, jdbcStatement);
				}
				//3) execute database batch updates
				executeBatchUpdate(jdbcConnection, jdbcStatement);
			}
		} finally {
			closeStatement(jdbcStatement);
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
	 * index the next batch of size BATCH_SIZE 
	 * 1) get the next batch of Bisens from database, continue if not empty
	 * 2) clear the temporary index dir, and initialize indexWriter on the temporary directory
	 * 3) call indexBisen for all Bisens and store corresponding database updates in a batch jdbcStatement 
	 * 4) finalyze temporary index and merge it into main index
	 * 5) execute database batch updates
	 * @param jdbcConnection
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public boolean indexBatch(Connection jdbcConnection) {
		 //the batch statement
		Statement jdbcStatement = getJdbcStatement(jdbcConnection);

		EntityManager em = null;
		boolean result = false;
		List<Bisen> bisens = new ArrayList<Bisen>();
		try {
			//1) get the next batch from database 
			em = entityManagerFactory.createEntityManager();
			bisens = getBisensByState(em, "I"); 
			result = (bisens != null) && (bisens.size() > 0);
			logger.info("get the next batch from database done resultList size:"+ bisens.size());
			
			if (result) {
				//2) clear the temporary index dir, and initialize indexWriter on the temporary directory
				//+
				//3) call indexBisen for all Bisens and store corresponding database updates in a batch jdbcStatement
				//4) finalyze temporary index 
				indexBisens(bisens, jdbcStatement);
				
				//4) cont... merge temporary index into main index
				mergeTmpIndex();
				
				//5) execute database batch updates
				executeBatchUpdate(jdbcConnection, jdbcStatement);
			}
		} finally {
			closeStatement(jdbcStatement);
			bisens.clear();
			bisens = null;
			if (em != null) {
				em.clear();
				em.close();
			}
		}
		return result;
	}

	private void closeStatement(Statement jdbcStatement){
		if (jdbcStatement != null){
			try {
				jdbcStatement.close();
			} catch (SQLException e) {
				logger.fatal("Inception: SQLException in finaly block and in "
						+ "SQLException catch block for jdbcStatement.close()",
						e);
				throw new RuntimeException(
						"Finally block Cannot close JDBC statement", e);
			}
		}
	}
	
	private void executeBatchUpdate(Connection jdbcConnection, Statement jdbcStatement){
		try {
			jdbcStatement.executeBatch();
			jdbcConnection.commit(); 
			logger.info("------Indexing batch updates commited ...");
		} catch (Exception e) {
			logger.error("Indexing batch commit error", e);
			throw new RuntimeException(e);
		}		
	}
	
	private void indexBisens(List<Bisen> bisens, Statement jdbcStatement){
		IndexWriter indexWriter = initIndexer(true);
		logger.info("-------clear the temporary index dir, and initialize indexWriter on the temporary directory done-----");
		try {
			for (Bisen bisen : bisens) {
				indexBisen(bisen, indexWriter, jdbcStatement);
			}
			logger.info("-------call indexBisen for all Bisens and store corresponding database updates in a batch jdbcStatement done-----");
			logger.info("------indexing batch done in memory.");
		
		} finally {
			closeIndexWriter(indexWriter);
			logger.info("-------temporary index written to disk done-----");
		}
		
	}

	
	public void optimizeIndex(){
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
		closeIndexWriter(indexWriter);
	}
	
	private void closeIndexWriter(IndexWriter indexWriter){
		try {
			if (indexWriter != null) {
				indexWriter.commit();
				indexWriter.close();
			}
		} catch (OutOfMemoryError e) {
			logger.fatal("indexWriter close OutOfMemoryError. Index writes rolled back.", e);
			throw new RuntimeException(e);
		} catch (Exception e) {
			logger.fatal("indexWriter close error", e);
			throw new RuntimeException(e);
		}		
	}
	
	/* true=temp that is index will be created in hunglishIndexTmp, false=main */
	/**
	 * Create a jdbcConnection
	 * calls indexBatch until it returns false using the connection
	 * clear up connection
	 */
	synchronized public void indexAll() {
		Connection jdbcConnection = null;
		try {
			jdbcConnection = getJdbcConnection();
			logger.info("db connection initialized");
			
			deleteAllFromIndex(jdbcConnection);

			addAllToIndex(jdbcConnection);						
			
			optimizeIndex();
			
		} finally {
			closeConnection(jdbcConnection);
			logger.info("db connection closed");
		}
	}

	private void addAllToIndex(Connection jdbcConnection){
		int batchIndex = 0;
		boolean keepItOn=true;
		while (keepItOn) {
			keepItOn = indexBatch(jdbcConnection);
			logger.info("<<<<<< finished indexing batch at " + ++batchIndex
					* dbBatchSize);
		}
		logger.info("-----indexing ::: all batches done--");
	}
	
	public void deleteAllFromIndex(Connection jdbcConnection){
		IndexWriter indexWriter = initIndexer(false);
		boolean keepItOn = true;
		int batchIndex = 0;
		try {
			while (keepItOn){
				keepItOn = deleteBatch(jdbcConnection, indexWriter);
				logger.info("<<<<<< DELETE FROM INDEX batch at " + ++batchIndex
						* dbBatchSize);
				
			}
			logger.info("-----DELETE FROM INDEX ::: all batches done--");
			closeIndexWriter(indexWriter);
			logger.info("-----DELETE FROM INDEX ::: indexwriter closed --");
		} catch (Exception e) {
			logger.fatal("DELETE FROM INDEX Exception while optimizing index", e);
			throw new RuntimeException(e);
		}		
	}
	
	
	
	private void closeConnection(Connection jdbcConnection){
		try {
			if (jdbcConnection != null) {
				jdbcConnection.close();
			}
		} catch (Exception e) {
			logger.fatal("connection close error", e);
			throw new RuntimeException(e);
		}
		
	}
	
	synchronized private void mergeTmpIndex() {
		boolean readOnly = true;
		IndexWriter indexWriter = null;
		IndexReader indexReader = null;
		try {

			try {
				indexWriter = initIndexer(false);
				indexReader = IndexReader.open(
						new SimpleFSDirectory(new File(tmpIndexDir)), readOnly);
				indexWriter.addIndexes(indexReader);
				logger.info("------indexreader addIndexes done----");

			} catch (Exception e) {
				logger.error("Index merge error", e);
				throw new RuntimeException(e);
			}
		} finally {
			closeIndexWriter(indexWriter);
			try {
				if (indexReader != null) {
					indexReader.close();
				}
				logger.info("------indexreader close done----");
			} catch (Exception e) {
				logger.fatal(
						"indexReader close error in Finally block",
						e);
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

	public String getDbUrl() {
		return dbUrl;
	}

	public void setDbUrl(String dbUrl) {
		this.dbUrl = dbUrl;
	}

	public String getDbDriver() {
		return dbDriver;
	}

	public void setDbDriver(String dbDriver) {
		this.dbDriver = dbDriver;
	}

	public String getDbUser() {
		return dbUser;
	}

	public void setDbUser(String dbUser) {
		this.dbUser = dbUser;
	}

	public String getDbPassword() {
		return dbPassword;
	}

	public void setDbPassword(String dbPassword) {
		this.dbPassword = dbPassword;
	}

	public Integer getDbBatchSize() {
		return dbBatchSize;
	}

	public void setDbBatchSize(Integer dbBatchSize) {
		this.dbBatchSize = dbBatchSize;
	}

}

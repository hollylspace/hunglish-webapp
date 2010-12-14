/**
 * 
 */
package hu.mokk.hunglish.lucene;

import hu.mokk.hunglish.domain.Bisen;
import hu.mokk.hunglish.jmorph.AnalyzerProvider;

import java.io.File;
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

	transient private static Log logger = LogFactory.getLog(Indexer.class);

	@Autowired
	private AnalyzerProvider analyzerProvider;

	private Integer mergeFactor = 100;
	private Integer maxBufferedDocs = 1000;
	private String indexDir;
	private String tmpIndexDir;
	private String uploadDir;
	
	@Autowired
	private EntityManagerFactory entityManagerFactory;

	// private IndexWriter indexWriter;

	// private CreateOrAppend createOrAppend = CreateOrAppend.Create;

	private IndexWriter initIndexer(Boolean temporary) {
		IndexWriter indexWriter;
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
		if (mergeFactor != null) {
			indexWriter.setMergeFactor(mergeFactor);
		}
		indexWriter.setMaxBufferedDocs(maxBufferedDocs);
		return indexWriter;
	}

	private void reCreateDir(File dir) {

		/*
		 * int numOfFiles = 0; if (dir.exists() && dir.isDirectory()) {
		 * numOfFiles = FileUtils.listFiles(dir, null, false).size(); }
		 */

		try {
			// if (numOfFiles >= 0) {
			FileUtils.deleteQuietly(dir);
			FileUtils.forceMkdir(dir);
			// }
		} catch (IOException e) {
			throw new RuntimeException("Cannot recreate directory:" + dir, e);
		}
	}

	/*
	public static File getFile(String path){
		File dir = null;
		try {
			dir = new File(getClass().getClassLoader().getResource(tmpIndexDir).toURI());
		} catch (URISyntaxException e) {
			String message = "Temp index dir cannot be resolved:"+tmpIndexDir;
			logger.error(message, e);
			throw new RuntimeException(message, e);
		}
		return dir;
	} //*/
	
	public void deleteTmpDirectory() {
		File dir = new File(tmpIndexDir);  //getFile(tmpIndexDir);
		logger.info("exists? " + dir.exists());
		logger.info("is it a dir? " + dir.isDirectory());
		logger.info("recreate dir: " + dir.getAbsolutePath());
		reCreateDir(dir);		
	}

	// TODO get this from properties file
	String url = "jdbc:mysql://localhost:3306/";
	String db = "hunglishwebapp";
	String driver = "com.mysql.jdbc.Driver";
	String user = "hunglish";
	String pass = "sw6x2the";
	public static int BATCH_SIZE = 10000;

	private Connection getJdbcConnection() {
		Connection con = null;
		try {
			Class.forName(driver);
			con = DriverManager.getConnection(url + db, user, pass);
			con.setAutoCommit(true);
		} catch (Exception e) {
			logger.error("Cannot create connection", e);
			throw new RuntimeException(e);
		}
		return con;
	}

	private Statement getJdbcStatement(Connection con) {

		Statement st = null;
		try {
			st = con.createStatement();
			st = con.createStatement();// java.sql.ResultSet.TYPE_FORWARD_ONLY,java.sql.ResultSet.CONCUR_READ_ONLY);
			// st.setFetchSize(Integer.MIN_VALUE);
		} catch (SQLException e) {
			logger.error("Cannot create statement", e);
			throw new RuntimeException(e);
		}
		return st;
	}

	private void indexBisen(Bisen bisen, IndexWriter iwriter,
			Statement jdbcStatement) {
		try {
			iwriter.addDocument(bisen.toLucene());
			jdbcStatement
					.addBatch("update bisen set is_indexed = true where id="
							+ bisen.getId());
		} catch (Exception e) {
			try {
				jdbcStatement.addBatch("update bisen "
						+ "set is_indexed = false where id=" + bisen.getId());
			} catch (SQLException e1) {
				e1.printStackTrace();
				logger.fatal("Exception in Catch block for "
						+ "Indexing error for bisen:", e1);
			}
			logger.error("Indexing error for bisen:" + bisen, e);
			throw new RuntimeException(e);
		}
	}

	@SuppressWarnings("unchecked")
	private boolean indexBatch(IndexWriter iwriter, Connection connection) {
		Statement jdbcStatement = null;
		EntityManager em = null;
		boolean result = false;
		List<Bisen> bisens = new ArrayList<Bisen>(1);
		try {
			jdbcStatement = getJdbcStatement(connection);
			em = entityManagerFactory.createEntityManager();
			bisens = em
					.createQuery(
							"from Bisen where isIndexed is null and isDuplicate = false") 
					.setMaxResults(BATCH_SIZE).getResultList();
			result = (bisens != null) && (bisens.size() > 0);
			if (result) {
				logger.info("--getResultList() done resultList size:"
						+ bisens.size());
				for (Bisen bisen : bisens) {
					indexBisen(bisen, iwriter, jdbcStatement);
				}

				logger
						.info("------indexing batch done in memory. mysql commit phase ...");
				try {
					jdbcStatement.executeBatch();
					// connection.commit(); //conn.setAutoCommit
					logger
							.info("------indexing batch done in memory. lucene commit phase ...");
					iwriter.commit();
				} catch (Exception e) {
					logger.error("Indexing batch commit error", e);
					throw new RuntimeException(e);
				}
				logger.info("------indexing batch commited to disk ");

			}
		} finally {
			try {
				jdbcStatement.close();
			} catch (SQLException e) {
				logger.fatal("Inception: SQLException in finaly block and in "
						+ "SQLException catch block for jdbcStatement.close()",
						e);
				throw new RuntimeException(
						"Finally block Cannot close JDBC statement", e);
			}
			bisens.clear();
			if (em != null) {
				em.clear();
				em.close();
			}
		}
		return result;
	}

	/* true=temp that is index will be created in hunglishIndexTmp, false=main */
	synchronized public void indexAll(boolean create) {
		Connection jdbcConnection = null;
		IndexWriter indexWriter = null;
		try {
			jdbcConnection = getJdbcConnection();
			indexWriter = initIndexer(create);
			logger.info("----init indexer done--");
			int batchIndex = 0;
			while (indexBatch(indexWriter, jdbcConnection)) {
				logger.info("<<<<<< finished indexing batch at " + ++batchIndex
						* BATCH_SIZE);
			}
			logger.info("-----index all batches done--");
			try {
				indexWriter.optimize();
			} catch (Exception e) {
				logger.error("indexWriter optimize error", e);
				throw new RuntimeException(e);
			}
			logger.info("-------optimize done-----");

		} finally {
			try {
				if (indexWriter != null) {
					indexWriter.close();
				}
				if (jdbcConnection != null) {
					jdbcConnection.close();
				}
			} catch (Exception e) {
				logger.fatal("indexWriter close error", e);
				throw new RuntimeException(e);
			}
		}
	}

	synchronized public void mergeTmpIndex() {
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
			try {
				if (indexReader != null) {
					indexReader.close();
				}
				logger.info("------indexreader close done----");
				if (indexWriter != null) {
					indexWriter.close();
				}
				logger.info("------indexwriter close done----");
			} catch (Exception e) {
				logger.fatal(
						"indexReader/indexWriter close error in Finally block",
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

}

package hu.mokk.hunglish.util;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import javax.sql.DataSource;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.index.IndexWriter;

public class IndexerHelper {
	
	transient private static Log logger = LogFactory.getLog(IndexerHelper.class);	
	
	public static void reCreateDir(String theDir) {
		File dir = null;
		try {
			dir = new File(theDir);
			FileUtils.deleteQuietly(dir);
			FileUtils.forceMkdir(dir);
		} catch (IOException e) {
			throw new RuntimeException("Cannot recreate directory:" + theDir, e);
		}
	}

	
	public static Connection getJdbcConnection(DataSource dataSource) throws SQLException {
		// Connection con = null;
		// try {
		// Class.forName(dbDriver);
		// con = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
		// con.setAutoCommit(false);
		// } catch (Exception e) {
		// logger.error("Cannot create connection", e);
		// throw new RuntimeException(e);
		// }
		// return con;

		return dataSource.getConnection();

	}

	public static void closeConnection(Connection jdbcConnection) {
		try {
			if (jdbcConnection != null) {
				jdbcConnection.close();
			}
		} catch (Exception e) {
			logger.fatal("connection close error", e);
			throw new RuntimeException(e);
		}
	}
	
	public static void closeIndexWriter(IndexWriter indexWriter) {
		try {
			if (indexWriter != null) {
				indexWriter.commit();
				indexWriter.close();
			}
		} catch (OutOfMemoryError e) {
			logger.fatal("indexWriter close OutOfMemoryError. Index writes rolled back.",
					e);
			throw new RuntimeException(e);
		} catch (Exception e) {
			logger.fatal("indexWriter close error", e);
			throw new RuntimeException(e);
		}
	}

	
	public static Statement getJdbcStatement(Connection con) {

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
	
	
	public static void closeStatement(Statement jdbcStatement) {
		if (jdbcStatement != null) {
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

	public static void executeBatchUpdate(Connection jdbcConnection,
			Statement jdbcStatement) {
		try {
			jdbcStatement.executeBatch();
			jdbcConnection.commit();
			logger.info("------Indexing batch updates commited ...");
		} catch (Exception e) {
			logger.error("Indexing batch commit error", e);
			throw new RuntimeException(e);
		}
	}

	
}

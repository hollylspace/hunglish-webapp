/*
 * Created by Peter Halacsy <peter at halacsy.com>
 * 
 * This work is licensed under the Creative Commons 
 * Attribution License. To view a copy of this license, 
 * visit http://creativecommons.org/licenses/by/2.0/ 
 * or send a letter to Creative Commons, 559 Nathan Abbott Way, 
 * Stanford, California 94305, USA.
 * 
 * Created on Mar 23, 2005
 *
 */

package mokk.nlp.irutil.lucene;

import java.io.IOException;

import mokk.nlp.irutil.Document;
import mokk.nlp.irutil.Indexer;
import mokk.nlp.irutil.io.DocumentHandler;
import mokk.nlp.irutil.io.DocumentSource;
import mokk.nlp.irutil.io.ProcessingException;
import mokk.nlp.irutil.lucene.analysis.AnalyzerFactory;

import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.activity.Initializable;
import org.apache.avalon.framework.component.Component;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.avalon.framework.logger.LogEnabled;
import org.apache.avalon.framework.logger.Logger;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.IndexWriter;

/**
 * @avalon.component
 * @avalon.service type=mokk.nlp.irutil.Indexer
 * @x-avalon.info name=default-indexer
 * @x-avalon.lifestyle type="singleton"
 */
public class DefaultLuceneIndexer implements Indexer, Component, LogEnabled,
		Configurable, Initializable, Serviceable, Disposable, Contextualizable {

	private Logger logger;
	private ServiceManager manager;
	private String indexDir;
	private String analyzerId;
	private String sourceId;
	private String mapperId;

	private IndexWriter indexWriter;
	private Analyzer analyzer;
	private Mapper mapper;

	private int mergeFactor;
	private int minMergeDocs;


	public void index() throws ProcessingException, IOException {

		DocumentSource source;
		try {
			source = (DocumentSource) manager.lookup(DocumentSource.ROLE + "/"
					+ sourceId);
		} catch (ServiceException e1) {

			throw new ProcessingException(e1);
		}
		source.setHandler(new DocumentHandler() {
			public void processDocument(Document d) throws ProcessingException {

				try {
					indexWriter.addDocument(mapper.toLucene(d)); // System.out.println(d.getText());
				} catch (IOException e) {

					new ProcessingException("can't index:" + d.getDocId(), e);
				}
			}
		});
		logger.info("starting to read from the source");

		source.read();
		logger.info("starting optimazitation");
		indexWriter.optimize();
	}

	/**
	 * @avalon.dependency type="mokk.nlp.irutil.lucene.analysis.AnalyzerFactory"
	 * @avalon.dependency type="mokk.nlp.irutil.io.DocumentSource"
	 * @avalon.dependency type="mokk.nlp.irutil.lucene.Mapper"
	 */
	public void service(ServiceManager manager) throws ServiceException {
		this.manager = manager;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.avalon.framework.activity.Initializable#initialize()
	 */
	public void initialize() throws Exception {

		// lucene requeres that the used analyser is
		// subclass of Analyzer abstract class
		// But fortress likes only interfaces here
		AnalyzerFactory a = (AnalyzerFactory) manager
				.lookup(AnalyzerFactory.ROLE + "/" + analyzerId);

		analyzer = a.getAnalyzer();

		mapper = (Mapper) manager.lookup(Mapper.ROLE + "/" + mapperId);
		// RAMDirectory ram = new RAMDirectory();
		indexWriter = new IndexWriter(indexDir, analyzer, true);
		indexWriter.setMergeFactor(mergeFactor);

		// TODO this is commented out due to switch to latest Lucene release.
		// find the equivalent method in new Lucene
		// http://lucene.apache.org/java/1_9_0/api/index.html
		// indexWriter.minMergeDocs = minMergeDocs;
		indexWriter.setMaxBufferedDocs(minMergeDocs);

		// indexWriter = new IndexWriter(ram, analyzer, true);
		logger.info("indexWriter opened in:" + indexDir);


	}


	public void configure(Configuration config) throws ConfigurationException {
		indexDir = config.getChild("index-dir").getValue();
		if (indexDir == null) {
			throw new ConfigurationException("no index-dir specified");
		}

		mapperId = config.getChild("mapper").getValue();
		logger.info("using mapper: " + mapperId);

		analyzerId = config.getChild("analyzer").getValue();
		logger.info("using analyzer:" + analyzerId);

		sourceId = config.getChild("source").getValue();
		logger.info("reading from source:" + sourceId);

		// TODO this is commented out due to switch to latest Lucene release.
		// find the equivalent method in new Lucene
		// http://lucene.apache.org/java/1_9_0/api/index.html
		// minMergeDocs =
		// config.getChild("min-merge-docs").getValueAsInteger(IndexWriter.DEFAULT_MIN_MERGE_DOCS);
		minMergeDocs = config.getChild("min-merge-docs").getValueAsInteger(
				IndexWriter.DEFAULT_MAX_BUFFERED_DOCS);
		logger.info("minMergeDocs: " + minMergeDocs);

		mergeFactor = config.getChild("merge-factor").getValueAsInteger(
				IndexWriter.DEFAULT_MERGE_FACTOR);
		logger.info("mergeFactor:" + mergeFactor);
	}

	/*
	 * @see
	 * org.apache.avalon.framework.logger.LogEnabled#enableLogging(org.apache
	 * .avalon.framework.logger.Logger)
	 */
	public void enableLogging(Logger logger) {
		this.logger = logger;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.apache.avalon.framework.context.Contextualizable#contextualize(org
	 * .apache.avalon.framework.context.Context)
	 */
	public void contextualize(Context context) throws ContextException {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.avalon.framework.activity.Disposable#dispose()
	 */
	public void dispose() {        
		if (indexWriter != null) {
			try {
				indexWriter.close();
			} catch (IOException e) {
				logger.error("can't gracefuly close indexWriter", e);
			}
		}

		if (analyzer != null) {
			manager.release(analyzer);
		}

		if (mapper != null) {
			manager.release(mapper);
		}

	}

}

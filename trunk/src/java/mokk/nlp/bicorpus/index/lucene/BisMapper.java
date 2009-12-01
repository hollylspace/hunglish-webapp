/*
 * Created on Nov 28, 2004
 *
 */
package mokk.nlp.bicorpus.index.lucene;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;

import mokk.nlp.bicorpus.BiSentence;
import mokk.nlp.bicorpus.Source;
import mokk.nlp.bicorpus.SourceDB;
import mokk.nlp.irutil.SearchException;
import mokk.nlp.irutil.lucene.Mapper;

import org.apache.avalon.fortress.util.ContextManagerConstants;
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
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;

/*
 * Az a komponens, ami egy BiSentence objektumot atalakit Lucene indexelhetove.
 * Sajnos a Lucene szarul van megtervezve, mert elore el kell kesziteni egy
 * Document objektumot, amire o meghivja az egyetlen Analyzerunket. Igy mezo
 * nevekbe kell bekodolni, hogy hogyan is tokenizalni a dolgokat
 * 
 * A bimondat ket stringbol all. Ezekbol egy egyszeru tokenized, stored field
 * lesz, hogy kereses utan meg tudjuk mutatni a mondatot. Ket masik mezoben
 * ugyanezen ket mondat stemmelt valtozata kerul. Hogy miert rakjuk kulon mezobe?
 * Mert a lucene nem kepes token es token kozott kulonbseget tenni, es mi szeretnenk
 * neha stemmeletlenul is keresni.
 * 
 * SourceId siman megy untokenized, stored, indexed, a sentenceId untokenized,
 * stored, unindexed, mert nincs ertelme mondatra keresni (egyenlore).
 * 
 * Ez az implementacio egy singleton avalon komponens.
 *  
 * @author hp
 * 
 * Duplikátum kezelés hozzáadva
 * @author bpgergo
 *
 */

/**
 * @avalon.component
 * @avalon.service type=mokk.nlp.irutil.lucene.Mapper
 * @x-avalon.info name=bis-mapper
 * @x-avalon.lifestyle type="singleton"
 */
public class BisMapper implements Mapper, Component, LogEnabled, Configurable,
		Initializable, Serviceable, Disposable, Contextualizable {

	/*
	 * TODO: configuracioba vele
	 */
	private static String leftFieldName = "left";
	private static String leftStemmedFieldName = "left_stemmed";
	private static String rightFieldName = "right";
	private static String rightStemmedFieldName = "right_stemmed";

	ServiceManager manager = null;

	SourceDB sourceDb = null;

	private Logger logger;
	private String indexDir;
	private IndexSearcher searcher = null;
	private IndexReader indexReader = null;
	private File contextDirectory;
	private HashSet<Integer> duplicateFilters;

	/**
	 * @avalon.dependency type="mokk.nlp.bicorpus.SourceDB"
	 */
	public void service(ServiceManager manager) throws ServiceException {
		this.manager = manager;

	}

	public void initialize() throws Exception {
		sourceDb = (SourceDB) manager.lookup(SourceDB.ROLE);
		String path = getContextualizedPath(indexDir);
		logger.warn("Bismapper index dir:" + path);
		// TODO it can be opened readonly in the webapp
		// it should be opened readwrite when indexing
		boolean readOnly = false;
		try {
			indexReader = IndexReader.open(FSDirectory.open(new File(path)),
					readOnly);
			searcher = new IndexSearcher(indexReader);
			logger.info("indexReader opened in:" + indexDir);
		} catch (Exception e) {
			logger.warn("no indexer provided for the sentence2doc mapper");
		}
		logger.warn("create duplicateFilters");
		duplicateFilters = new HashSet<Integer>();

	}

	public void configure(Configuration config) throws ConfigurationException {
		indexDir = config.getChild("index-dir").getValue();
		if (indexDir == null) { // this will not happen; avalon framework will
			// throw exception on the line before
			throw new ConfigurationException("no index-dir specified");
		}
	}

	// TODO this is duplicate code, add it to some base class
	private String getContextualizedPath(String file) {
		if (file.startsWith("/")) {
			return file;
		}
		return contextDirectory.getAbsolutePath() + "/" + file;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.apache.avalon.framework.context.Contextualizable#contextualize(org
	 * .apache.avalon.framework.context.Context)
	 */
	public void contextualize(Context context) throws ContextException {
		contextDirectory = (File) context
				.get(ContextManagerConstants.CONTEXT_DIRECTORY);
		logger.info("context directory:" + contextDirectory);
	}

	public void dispose() {
		if (indexReader != null) {
			try {
				indexReader.close();
				indexReader = null;
			} catch (IOException e) {
				logger.error("can't gracefuly close indexReader", e);
			}
		}
		if (sourceDb != null) {
			manager.release(sourceDb);
		}
	}

	private static String stripPunctuation(String s) {
		StringBuffer sb = new StringBuffer();
		if (s.length() > 0) {
			for (int i = 0; i < s.length(); i++) {
				char c = s.charAt(i);
				if (c == 0x20 || Character.isLetterOrDigit(c)) {
					sb = sb.append(Character.toLowerCase(s.charAt(i)));
				}
			}
		}
		//reduce spaces
		return sb.toString().replaceAll(" +", " ").trim();
	}

	private enum Side {
		LEFT, RIGHT
	}

	public static String duplicateFilterName = "duplicateFilter";
	public static String isDuplicateName = "isDuplicate";
	public static String RIGHT = "R";
	public static String LEFT = "L";
	public static String YES = "Y";
	public static String BOTH = "B";
	public static String NO = "N";

	
	
	
	private boolean isDuplicate(HashSet<Integer> dupCache, String dupFieldName, Integer duplicateFilter) throws SearchException{
		boolean result = false;

		boolean inCache = checkCache(dupCache, duplicateFilter);
		if (inCache){
			//logger.debug("<<<<< found in cache! "+dupFieldName+" duplicateFilter:"+duplicateFilter);
			result = true;
		} else {
			boolean inIndex = alreadyInIndex(dupFieldName, duplicateFilter);
			if (inIndex){
				//logger.debug("<<<<< found in INDEX! "+dupFieldName+" duplicateFilter:"+duplicateFilter);
				result = true;
			}
		}
		
		return result;
	}
	
	/**
	 * Duplicate will be searched in the currently processed file (using the cache)
	 * and also in the index (actually in a copy)
	 * @param d
	 * @param bis
	 * @param side
	 * @throws SearchException
	 */
	private void addDuplicateFilterField(Document d, BiSentence bis)
			throws SearchException {//System.out.printlnrintln("++++++addDuplicateFilterField Bis:"+bis);

		
		// Add a field for both sentence to support duplicate filtering.
		// First clean punctuation,
		// then get a hash of the punctuation free sentence.
		
		// Now a search will be done to check whether this sen has already been
		// added to the index.
		// http://wiki.apache.org/lucene-java/LuceneFAQ#Searching
		// Does Lucene allow searching and indexing simultaneously?
		// Yes. However, an IndexReader only searches the index as of the
		// "point in time" that it was opened. Any updates to the index, either
		// added or deleted documents, will not be visible until the IndexReader
		// is re-opened. So your application must periodically re-open its
		// IndexReaders to see the latest updates. The IndexReader.isCurrent()
		// method allows you to test whether any updates have occurred to the
		// index since your IndexReader was opened.

		
		String noPunct = stripPunctuation(bis.getLeftSentence()+" "+bis.getRightSentence());
		Integer dupFilter = new Integer(noPunct.hashCode());
		//System.out.println.println("-----NoPunct:"+noPunct+";filter:"+dupFilter);
		
		String isDuplicateValue = NO;
		if (isDuplicate(duplicateFilters, duplicateFilterName, dupFilter)){
			isDuplicateValue = YES;
		}
		
		// add is_duplicate filed to the document
		d.add(new Field(isDuplicateName, isDuplicateValue, Field.Store.YES,
				Field.Index.NOT_ANALYZED, Field.TermVector.NO));

		// Finally add duplicate filter field to the document
		d.add(new Field(duplicateFilterName, dupFilter.toString(), Field.Store.YES,
				Field.Index.NOT_ANALYZED, Field.TermVector.NO));
		

	}

	/*
	private static void addCache(HashSet<String> cache, String word) {
		synchronized (cache) {
			cache.add(word);
		}
	}*/

	private static boolean checkCache(HashSet<Integer> cache, Integer word) {
		boolean result = false;
		synchronized (cache) {
			result = cache.contains(word);
			if (!result){
				//System.out.printlnut.println(">>>>>> add to cahce!:"+word);
				cache.add(word);
			}
		}
		return result;
	}

	private boolean alreadyInIndex(String fieldName, Integer duplicateFilter)
			throws SearchException {
		boolean res = false;
		if (searcher != null) {
			Term term = new Term(fieldName, duplicateFilter.toString());
			Query query = new TermQuery(term);
			TopDocs topDocs = null;
			try {
				topDocs = searcher.search(query, 1);
			} catch (IOException ioe) {
				throw new SearchException(ioe);
			}
			if (topDocs.totalHits > 0) {
				res = true;
			}//System.out.println.out.println("????? index check !!!!! fieldName:"+fieldName+"; duplicateFilter:"+duplicateFilter.toString()+"; res:"+Boolean.toString(res));
		}
		return res;
	}

	public org.apache.lucene.document.Document toLucene(
			mokk.nlp.irutil.Document doc) throws SearchException {

		BiSentence bis = (BiSentence) doc;
		Document d = new Document();

		/**
		 * String name, String string, boolean store, boolean index, boolean
		 * token
		 */
		d.add(new Field(leftFieldName, bis.getLeftSentence(), 
		// Field.Index.TOKENIZED, Field.TermVector.WITH_OFFSETS));
				Field.Store.YES, Field.Index.ANALYZED, 
				Field.TermVector.WITH_POSITIONS_OFFSETS));
		d.add(new Field(leftStemmedFieldName, bis.getLeftSentence(),
				// Field.Store.NO, Field.Index.TOKENIZED,
				Field.Store.NO, Field.Index.ANALYZED,
				Field.TermVector.WITH_POSITIONS_OFFSETS));
		d.add(new Field(rightFieldName, bis.getRightSentence(),
				// Field.Store.YES, Field.Index.TOKENIZED,
				Field.Store.YES, Field.Index.ANALYZED,
				Field.TermVector.WITH_POSITIONS_OFFSETS));
		d.add(new Field(rightStemmedFieldName, bis.getRightSentence(),
				// Field.Store.NO, Field.Index.TOKENIZED,
				Field.Store.NO, Field.Index.ANALYZED,
				Field.TermVector.WITH_POSITIONS_OFFSETS));

		Source source;

		for (source = bis.getSource(); source != null; source = source
				.getParent()) {

			d.add(new Field("source", source.getId(), Field.Store.YES,
			// Field.Index.UN_TOKENIZED, Field.TermVector.NO));
					Field.Index.NOT_ANALYZED, Field.TermVector.NO));
		}
		d.add(new Field("sen", bis.getSenId(), Field.Store.YES, Field.Index.NO,
				Field.TermVector.NO));

		addDuplicateFilterField(d, bis);

		return d;
	}

	public mokk.nlp.irutil.Document toResource(
			org.apache.lucene.document.Document luceneDocument) {
		String left = luceneDocument.getField(leftFieldName).stringValue();
		String right = luceneDocument.getField(rightFieldName).stringValue();
		String sourceId = luceneDocument.getField("source").stringValue();

		// a leghosszabb sourceId az aktualis, es mivel ugyanaz a prefixuk,
		// maximumot kell keresni
		Source source = null;
		String[] sources = luceneDocument.getValues("source");
		int deepeth = -1;
		if (sources.length == 0) {
			source = Source.UNKNOWN_SOURCE;
		} else {

			for (int i = 0; i < sources.length; i++){
			//System.out.printlnem.out.println(sources[i]);
				Source s = sourceDb.get(sources[i]);
				if (s != null){
					//System.out.printlnstem.out.println(s.getId() + " " + s.getLevel());
					if (s.getLevel() > deepeth) {
						source = s;
						deepeth = s.getLevel();
					}
				}
			}

			if (source == null) {
				source = Source.UNKNOWN_SOURCE;
			}
		}

		String senId = luceneDocument.getField("sen").stringValue();
		return new BiSentence(source, senId, left, right);
	}

	public void close() throws IOException {
		if (searcher != null){
			searcher.close();
		}
	}

	// @Override
	public void enableLogging(Logger logger) {
		this.logger = logger;
	}

}

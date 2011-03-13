/**
 * 
 */
package hu.mokk.hunglish.lucene;

import hu.mokk.hunglish.domain.Bisen;
import hu.mokk.hunglish.jmorph.AnalyzerProvider;
import hu.mokk.hunglish.lucene.analysis.CompoundStemmerTokenFilter;
import hu.mokk.hunglish.util.Pair;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.search.highlight.Highlighter;
import org.apache.lucene.search.highlight.InvalidTokenOffsetsException;
import org.apache.lucene.search.highlight.QueryScorer;
import org.apache.lucene.search.highlight.SimpleFragmenter;
import org.apache.lucene.search.highlight.SimpleHTMLFormatter;
import org.apache.lucene.search.highlight.TokenSources;
import org.apache.lucene.store.SimpleFSDirectory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

/**
 * @author bpgergo
 * 
 */
@Configurable
public class Searcher {
	
	transient private static Log logger = LogFactory.getLog(Searcher.class);
	private String indexDir;
	/**
	 * the maximum number of search results. 
	 * 1000 like in google
	 */
	private Integer maxDocuments;
	
	private IndexSearcher searcher;
	private IndexReader indexReader;
	
	private String useHunglishSyntax = "true";
	//private Boolean setHunglishSyntax = false;
	
	@Autowired
	private LuceneQueryBuilder luceneQueryBuilder;

	@Autowired
	private AnalyzerProvider analyzerProvider;
	
	
	private static Searcher instance;
	public Searcher(){
		if (instance == null){
			instance = this;
		} else {
			throw new IllegalStateException("Indexer is singleton");
		}
	}
	
	
	public void reInitSearcher(){
		if (indexReader != null){
			try {
				indexReader.close();
			} catch (IOException e) {
				throw new RuntimeException("Error while closing indexreader.", e);
			}
		}
		indexReader = null;
		if (luceneQueryBuilder != null){
			luceneQueryBuilder.deleteSourceFilterCache();
		}
		initSearcher();
	}
	
	public void initSearcher() {
		boolean readOnly = true;
		if (indexReader == null) {
			try {
				indexReader = IndexReader.open(new SimpleFSDirectory(new File(
						indexDir)), readOnly);
				searcher = new IndexSearcher(indexReader);
				luceneQueryBuilder = new LuceneQueryBuilder();
			} catch (CorruptIndexException e) {
				throw new RuntimeException("Cannot open index directory:"+indexDir, e);
			} catch (IOException e) {
				indexReader =null;
				//throw new RuntimeException("Cannot open index directory:"+indexDir, e);
				//TODO what to do when you have no index? 
			}
		}
	}

	private void checkState(){
		if (indexReader == null){
			throw new IllegalStateException("Cannot initialize searcher. indexReader is null");
		}
		if (searcher == null){
			throw new IllegalStateException("Cannot initialize searcher. searcher is null");
		}
		if (luceneQueryBuilder == null){
			throw new IllegalStateException("Cannot initialize searcher. luceneQueryBuilder is null");
		}		
	}
		
	
	private static String highlightField(TokenStream tokenStream, Query query,
			String fieldName, String text) throws IOException,
			InvalidTokenOffsetsException {
		SimpleHTMLFormatter formatter = new SimpleHTMLFormatter();
		QueryScorer scorer = new QueryScorer(query, fieldName);
		Highlighter highlighter = new Highlighter(formatter, scorer);
		highlighter.setTextFragmenter(new SimpleFragmenter(Integer.MAX_VALUE));
		String rv = highlighter.getBestFragments(tokenStream, text, 10, "...");
		return rv.length() == 0 ? text : rv;
	}

	private boolean requestIsEmpty(SearchRequest request) {
		return (request == null) || ( 
				(request.getEnQuery() == null || request.getEnQuery().isEmpty()) 
				&& (request.getHuQuery() == null || request.getHuQuery().isEmpty())
				);
	}
	
	public SearchResult search(SearchRequest request){
		checkState();
		SearchResult result = new SearchResult();
		if (requestIsEmpty(request)){
			return result;
		}
		int end = request.getStartOffset() + request.getMaxResults();
		if (end > maxDocuments){
			return result;
		}
		
		Query query = null;
		try {
			query = luceneQueryBuilder.parseRequest(request);
		} catch (ParseException e1) {
			logger.error("cannot parse search request:"+request, e1);
			throw new RuntimeException("Request couldn't be parsed", e1);
		}
		
		// Collect enough docs to show 1 pages
		TopScoreDocCollector collector = TopScoreDocCollector.create(
				maxDocuments, false);

		try {
			searcher.search(query, collector);
		} catch (IOException ioe) {
			throw new RuntimeException(ioe);
		}
		ScoreDoc[] hits = collector.topDocs().scoreDocs;
		int totalHits = collector.getTotalHits();
		result.setTotalCount(totalHits);

		result.setStartOffset(request.getStartOffset());
		
		if (end > totalHits) {
			end = totalHits;
		}
		result.setEndOffset(end);

		// kiolvassuk az aktualis lapon levo dokumentumokat, csinalunk beloluk
		// bimondatot, plusz adunk hozza highlightingot
		
		List<Pair<Document, Integer>> docs = new ArrayList<Pair<Document,Integer>>();
		for (int i = request.getStartOffset(); i < end; i++) {
			Document d;
			int docId = hits[i].doc;
			try {
				d = searcher.doc(docId);
			} catch (IOException ioe) {
				throw new RuntimeException(ioe);
			}
			docs.add(new Pair<Document, Integer>(d, docId));
		}
		
		List<Pair<Document,Bisen>> resultBisens = Bisen.toBisens(docs);
		
		if (request.getHighlightHu() || request.getHighlightEn()){
			highlightBisens(request, resultBisens, query);
		}
		
		result.addToHits(resultBisens);
		return result;
		
	}
	
	
	
	static String highLightStart = "<B>";
	static String highLightEnd = "</B>";
	static String highPatternString = "("+highLightStart+")"+"([^<>]+)"+"("+highLightEnd+")";
	static Pattern highPattern = Pattern.compile(highPatternString);
	
	public static String mergeHighLight(String line1, String line2){
		Matcher matcher = highPattern.matcher(line1);
		boolean found = matcher.find();
		while(found){
			String match = matcher.group(2);
			String replaceMent = matcher.group(); //highLightStart+match+highLightEnd;
			System.out.println("match:"+match+";replacement:"+replaceMent);
			line2 = line2.replaceAll(match, replaceMent);
			found = matcher.find();
		}
		return line2;
	}
	
	private void highlightBisens(SearchRequest request, List<Pair<Document, Bisen>> bisens, Query query){
		for (Pair<Document, Bisen> pair : bisens){
			Bisen bisen = pair.getSecond();
			//Document document = pair.getFirst();
			logger.debug("--------------- HIGHLIGHT -----------------");
			if (pair.getSecond() != null && request.nonEmptyHuQuery() && request.getHighlightHu() && indexReader != null) {
				try {
					TokenStream huTokens = TokenSources.getTokenStream(
							indexReader, bisen.getLuceneDocId(), Bisen.huSentenceStemmedFieldName);
					huTokens = new CompoundStemmerTokenFilter(huTokens,
							analyzerProvider.getLemmatizerMap().get(Bisen.huSentenceStemmedFieldName)); 
					logger.debug("try to high hu stemmed:"+query.toString());
					String high = highlightField(huTokens, query,
							Bisen.huSentenceFieldName, bisen.getHuSentence());
					logger.debug(high);
					
					//if (high.toLowerCase().indexOf("<b>")< 0){
					logger.debug("try to high hu on not-stemmed field 2:"+query.toString());
					huTokens = TokenSources.getTokenStream(
							indexReader, bisen.getLuceneDocId(), Bisen.huSentenceFieldName);
					huTokens = new CompoundStemmerTokenFilter(huTokens,
							analyzerProvider.getLemmatizerMap().get(Bisen.huSentenceStemmedFieldName)); 
					String high2 = highlightField(huTokens, query,
							Bisen.huSentenceStemmedFieldName, bisen.getHuSentence());
					logger.debug(high2);
					//}					
					
					high = mergeHighLight(high, high2);
					bisen.setHuSentenceView(high);
				} catch (Exception e) {
					e.printStackTrace(); //TODO FIXME
					//throw new RuntimeException("error while highlighting", e);
				}
			}
	
			if (bisen != null && request.nonEmptyEnQuery() && request.getHighlightEn() && indexReader != null) {
				try {
					TokenStream enTokens = TokenSources.getTokenStream(
							indexReader, bisen.getLuceneDocId(), Bisen.enSentenceStemmedFieldName);
					enTokens = new CompoundStemmerTokenFilter(enTokens,
							analyzerProvider.getLemmatizerMap().get(Bisen.enSentenceStemmedFieldName)); 
					
					logger.debug("try to high en stemmed:"+query.toString());
					String high = highlightField(enTokens, query,
							Bisen.enSentenceFieldName, bisen.getEnSentence());
					logger.debug(high);
					enTokens = TokenSources.getTokenStream(
							indexReader, bisen.getLuceneDocId(), Bisen.enSentenceFieldName);
					enTokens = new CompoundStemmerTokenFilter(enTokens,
							analyzerProvider.getLemmatizerMap().get(Bisen.enSentenceStemmedFieldName)); 
					logger.debug("try to high en on not-stemmed field:"+query.toString());
					String high2 = highlightField(enTokens, query,
							Bisen.enSentenceStemmedFieldName, bisen.getEnSentence());
					logger.debug(high);
					
					high = mergeHighLight(high, high2);
					pair.getSecond().setEnSentenceView(high);
				} catch (Exception e) {
					e.printStackTrace(); //TODO FIXME
					//throw new RuntimeException("error while highlighting", e);
				}
			}	
			
		}
	}
	
	public void setIndexDir(String indexDir) {
		this.indexDir = indexDir;
	}

	
	public void setSearcher(IndexSearcher searcher) {
		this.searcher = searcher;
	}


	public void setLuceneQueryBuilder(LuceneQueryBuilder luceneQueryBuilder) {
		this.luceneQueryBuilder = luceneQueryBuilder;
	}

	public String getIndexDir() {
		return indexDir;
	}

	public IndexSearcher getSearcher() {
		return searcher;
	}

	public String getUseHunglishSyntax() {
		return useHunglishSyntax;
	}

	public void setUseHunglishSyntax(String useHunglishSyntax) {
		this.useHunglishSyntax = useHunglishSyntax;
		//if (Boolean.parseBoolean(useHunglishSyntax)){
		//	setHunglishSyntax = true;
		//}
	}


	public static Searcher getInstance() {
		return instance;
	}


	public Integer getMaxDocuments() {
		return maxDocuments;
	}


	public void setMaxDocuments(Integer maxDocuments) {
		this.maxDocuments = maxDocuments;
	}


	public AnalyzerProvider getAnalyzerProvider() {
		return analyzerProvider;
	}


	public void setAnalyzerProvider(AnalyzerProvider analyzerProvider) {
		this.analyzerProvider = analyzerProvider;
	}



}

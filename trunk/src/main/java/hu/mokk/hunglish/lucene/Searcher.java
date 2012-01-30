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
import java.util.Arrays;
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
import org.apache.lucene.index.Term;
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
import org.apache.lucene.search.spell.LuceneDictionary;
import org.apache.lucene.search.spell.SpellChecker;
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
	private String spellIndexDir;
	private String spellIndexDirHu;
	/**
	 * the maximum number of search results. 1000 like in google
	 */
	private Integer maxDocuments;

	private Integer maxResultSetSize;

	private SimpleFSDirectory indexFSDir;
	private SimpleFSDirectory spellFSDir;
	private SimpleFSDirectory spellFSDirHu;

	private IndexSearcher searcher;
	private IndexReader indexReader;
	private SpellChecker enSpellChecker;
	private SpellChecker huSpellChecker;

	private int minimumHits;
	private float minimumScore;

	private String useHunglishSyntax = "true";
	// private Boolean setHunglishSyntax = false;

	@Autowired
	private LuceneQueryBuilder luceneQueryBuilder;

	@Autowired
	private AnalyzerProvider analyzerProvider;

	private static Searcher instance;

	public Searcher() {
		if (instance == null) {
			instance = this;
		} else {
			throw new IllegalStateException("Indexer is singleton");
		}
	}

	public synchronized void reInitSearcher() {
		closeSearcher();
		internalInitSearcher();
	}

	private void closeSearcher() {
		if (searcher != null) {
			try {
				searcher.close();
			} catch (IOException e) {
				throw new RuntimeException("Error while closing searcher.",
						e);
			}
			searcher = null;
		}
		if (indexReader != null) {
			try {
				indexReader.close();
				indexReader = null;
			} catch (IOException e) {
				throw new RuntimeException("Error while closing indexreader.",
						e);
			}
		}
		if (indexFSDir != null) {
			indexFSDir.close();
			indexFSDir = null;
		}
		if (luceneQueryBuilder != null) {
			luceneQueryBuilder.deleteSourceFilterCache();
		}
	}

	private void initSpellChecker() {
		try{
			spellFSDir = new SimpleFSDirectory(new File(spellIndexDir));
			logger.debug("spell index dir initialized:"+spellIndexDir);
		} catch (Exception e) {
			throw new RuntimeException("Cannot open spell index directory:"
					+ spellIndexDir, e);
		}
		try{
			spellFSDirHu = new SimpleFSDirectory(new File(spellIndexDirHu));
			logger.debug("spell index dir initialized:"+spellIndexDirHu);
		} catch (Exception e) {
			throw new RuntimeException("Cannot open spell index directory:"
					+ spellIndexDirHu, e);
		}
		try{
			enSpellChecker = new SpellChecker(spellFSDir);
			logger.debug("spell checker initialized:"+spellIndexDir);
		} catch (Exception e) {
			throw new RuntimeException("Cannot cannot initialize spell checker dir:"
					+ spellIndexDir, e);
		}
		try{
			huSpellChecker = new SpellChecker(spellFSDirHu);
			logger.debug("spell checker initialized:"+spellIndexDirHu);
		} catch (Exception e) {
			throw new RuntimeException("Cannot cannot initialize spell checker from dir:"
					+ spellIndexDirHu, e);
		}
		
	}

	public void initSearcher() {
		internalInitSearcher();
		initSpellChecker();
	}
	
	private void internalInitSearcher() {
		boolean readOnly = true;

		try {
			if (indexFSDir == null) {
				indexFSDir = new SimpleFSDirectory(new File(indexDir));
				logger.debug("index dir initialized:"+indexDir);
			}
			if (indexReader == null) {
				indexReader = IndexReader.open(indexFSDir, readOnly);
				logger.debug("index reader initialized:"+indexDir);
			}
			if (searcher == null) {
				searcher = new IndexSearcher(indexReader);
				logger.debug("index searcher initialized:"+indexDir);
			}
			if (luceneQueryBuilder == null) {
				luceneQueryBuilder = new LuceneQueryBuilder();
				logger.debug("query builder initialized:");
			}
		} catch (CorruptIndexException e) {
			throw new RuntimeException("Cannot open index directory:"
					+ indexDir, e);
		} catch (IOException e) {
			//indexReader = null;
			throw new RuntimeException("Cannot open index directory:"
					+ indexDir, e);
			// TODO what to do when you have no index?
		}

	}

	private void checkState() {
		if (indexReader == null) {
			throw new IllegalStateException(
					"Cannot initialize searcher. indexReader is null");
		}
		if (searcher == null) {
			throw new IllegalStateException(
					"Cannot initialize searcher. searcher is null");
		}
		if (luceneQueryBuilder == null) {
			throw new IllegalStateException(
					"Cannot initialize searcher. luceneQueryBuilder is null");
		}
	}

	private boolean checkSpellCheckerState() {
		if ((enSpellChecker == null) || (huSpellChecker == null)){
			logger.warn("spell checker is not initialized");
			return false;			
		} else {			
			return true;
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
		return (request == null)
				|| ((request.getEnQuery() == null || request.getEnQuery()
						.isEmpty()) && (request.getHuQuery() == null || request
						.getHuQuery().isEmpty()));
	}

	public SearchResult search(SearchRequest request) {
		checkState();
		SearchResult result = new SearchResult();
		if (requestIsEmpty(request)) {
			return result;
		}
		int end = request.getStartOffset() + request.getMaxResults();
		if (end > maxDocuments) {
			return result;
		}

		Query query = null;
		try {
			query = luceneQueryBuilder.parseRequest(request);
		} catch (ParseException e1) {
			logger.error("cannot parse search request:" + request, e1);
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

		List<Pair<Document, Integer>> docs = new ArrayList<Pair<Document, Integer>>();
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

		List<Pair<Document, Bisen>> resultBisens = Bisen.toBisens(docs);
		for (Pair<Document, Bisen> pair : resultBisens) {
			Bisen bisen = pair.getSecond();
			bisen.setEnSentenceView(bisen.getEnSentence());
			bisen.setHuSentenceView(bisen.getHuSentence());
		}
		if (request.getHighlightHu() || request.getHighlightEn()) {
			highlightBisens(request, resultBisens, query);
		}

		result.addToHits(resultBisens);
		if (hits.length > 0)
			logger.debug("SScore:" + hits[0].score);
		if (hits.length < minimumHits
				|| (hits.length > 0 && hits[0].score < minimumScore)) {
			didYouMean(result, query, request);
		}
		return result;

	}

	private void didYouMean(SearchResult result, Query query,
			SearchRequest request) {
		if (checkSpellCheckerState()) {
			String enSuggestionString = request.getEnQuery();
			String huSuggestionString = request.getHuQuery();
			boolean OK = false;
			Set<Term> terms = new HashSet<Term>();
			query.extractTerms(terms);
			if (terms.size() > 0) {
				for (Term term : terms) {
					logger.debug("Term:" + term);
					if (Bisen.enSentenceStemmedFieldName.equals(term.field())) {
						String suggestion = suggest(enSpellChecker, term);
						if (suggestion != null) {
							OK = true;
							logger.debug("suggestion for Term:" + suggestion);
							logger.debug("enSuggestionString:"
									+ enSuggestionString);
							enSuggestionString = enSuggestionString.replaceAll(
									term.text(), suggestion);
							logger.debug("enSuggestionString replaced:"
									+ enSuggestionString);
						}
					} else if (Bisen.huSentenceStemmedFieldName.equals(term
							.field())) {
						String suggestion = suggest(huSpellChecker, term);
						if (suggestion != null) {
							OK = true;
							logger.debug("suggestion for Term:" + suggestion);
							logger.debug("huSuggestionString:"
									+ huSuggestionString);
							huSuggestionString = huSuggestionString.replaceAll(
									term.text(), suggestion);
							logger.debug("huSuggestionString replaced:"
									+ huSuggestionString);
						}
					}
				}
			}
			if (OK) {
				result.setEnSuggestionString(enSuggestionString);
				result.setHuSuggestionString(huSuggestionString);
			}
		}
	}

	private String suggest(SpellChecker spellChecker, Term term) {
		String queryString = term.text();
		try {
			if (spellChecker.exist(queryString)) {
				return null;
			}
			String[] similarWords = spellChecker.suggestSimilar(queryString, 1);
			if (similarWords.length == 0) {
				return null;
			}
			return similarWords[0];
		} catch (IOException e) {
			// TODO Auto-generated catch block
			throw new RuntimeException("error while getting similar word for "
					+ queryString, e);
		}
	}

	static String highLightStart = "<B>";
	static String highLightEnd = "</B>";
	static String highPatternString = "(" + highLightStart + ")" + "([^<>]+)"
			+ "(" + highLightEnd + ")";
	static Pattern highPattern = Pattern.compile(highPatternString);

	public static String mergeHighLight(String line1, String line2) {
		if (line1 != null && line2 != null) {
			Matcher matcher = highPattern.matcher(line1);
			boolean found = matcher.find();
			while (found) {
				String match = matcher.group(2);
				String replaceMent = matcher.group();
				line2 = line2.replaceAll(match, replaceMent);
				found = matcher.find();
			}
			return line2;
		} else if (line1 != null) {
			return line1;
		} else if (line2 != null) {
			return line2;
		} else {
			throw new IllegalArgumentException("both line is null");
		}
	}

	private enum BisenSide {
		EN, HU
	}

	private static boolean notEmptyQuery(Query query, BisenSide side) {
		Set<Term> terms = new HashSet<Term>();
		query.extractTerms(terms);
		for (Term term : terms) {
			if (term.field().toUpperCase()
					.startsWith(side.toString().toUpperCase())) {
				return true;
			}
		}
		return false;
	}

	private void highlightBisen(Bisen bisen, Query query, BisenSide side) {
		if (logger.isDebugEnabled()) {
			logger.debug("highlight bisen");
			logger.debug(bisen);
			logger.debug(query);
			logger.debug(side);
		}
		if (notEmptyQuery(query, side)) {
			String fieldName = null;
			String stemmedFieldName = null;
			String sentence = null;
			switch (side) {
			case EN:
				fieldName = Bisen.enSentenceFieldName;
				stemmedFieldName = Bisen.enSentenceStemmedFieldName;
				sentence = bisen.getEnSentence();
				break;
			case HU:
				fieldName = Bisen.huSentenceFieldName;
				stemmedFieldName = Bisen.huSentenceStemmedFieldName;
				sentence = bisen.getHuSentence();
				break;
			}
			logger.debug(fieldName + "--" + stemmedFieldName);
			String high = null;
			String high2 = null;
			TokenStream tokens;
			try {
				tokens = TokenSources.getTokenStream(indexReader,
						bisen.getLuceneDocId(), stemmedFieldName);
				tokens = new CompoundStemmerTokenFilter(tokens,
						analyzerProvider.getLemmatizerMap().get(
								stemmedFieldName));
				high = highlightField(tokens, query, stemmedFieldName, sentence);
				logger.debug(high);
			} catch (Exception e) {
				logger.error(side + ": Error while highlighting stemmed field",
						e);
				// TODO? throw new RuntimeException("error while highlighting",
				// e);
			}

			try {
				tokens = TokenSources.getTokenStream(indexReader,
						bisen.getLuceneDocId(), fieldName);
				// TODO is this next line really neaded?
				tokens = new CompoundStemmerTokenFilter(tokens,
						analyzerProvider.getLemmatizerMap().get(
								stemmedFieldName));
				high2 = highlightField(tokens, query, fieldName, sentence);
				logger.debug(high2);
			} catch (Exception e) {
				logger.error(side
						+ ": Error while highlighting NOT stemmed field", e);
				// TODO? throw new RuntimeException("error while highlighting",
				// e);
			}

			if (high != null || high2 != null) {
				high = mergeHighLight(high, high2);

				switch (side) {
				case EN:
					bisen.setEnSentenceView(high);
					break;
				case HU:
					bisen.setHuSentenceView(high);
					break;
				}
			}

		}
	}

	private void highlightBisens(SearchRequest request,
			List<Pair<Document, Bisen>> bisens, Query query) {
		for (Pair<Document, Bisen> pair : bisens) {
			Bisen bisen = pair.getSecond();
			// Document document = pair.getFirst();
			logger.debug("--------------- HIGHLIGHT -----------------");
			logger.debug(bisen);
			if (pair.getSecond() != null && request.nonEmptyHuQuery()
					&& request.getHighlightHu() && indexReader != null) {
				logger.debug("-- HIGHLIGHTing HU --");
				highlightBisen(bisen, query, BisenSide.HU);
			}

			if (bisen != null && request.nonEmptyEnQuery()
					&& request.getHighlightEn() && indexReader != null) {
				logger.debug("-- HIGHLIGHTing EN --");
				highlightBisen(bisen, query, BisenSide.EN);
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
		// if (Boolean.parseBoolean(useHunglishSyntax)){
		// setHunglishSyntax = true;
		// }
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

	public Integer getMaxResultSetSize() {
		return maxResultSetSize;
	}

	public void setMaxResultSetSize(Integer maxResultSetSize) {
		this.maxResultSetSize = maxResultSetSize;
	}

	public String getSpellIndexDir() {
		return spellIndexDir;
	}

	public void setSpellIndexDir(String spellIndexDir) {
		this.spellIndexDir = spellIndexDir;
	}

	public int getMinimumHits() {
		return minimumHits;
	}

	public void setMinimumHits(int minimumHits) {
		this.minimumHits = minimumHits;
	}

	public float getMinimumScore() {
		return minimumScore;
	}

	public void setMinimumScore(float minimumScore) {
		this.minimumScore = minimumScore;
	}

	public String getSpellIndexDirHu() {
		return spellIndexDirHu;
	}

	public void setSpellIndexDirHu(String spellIndexDirHu) {
		this.spellIndexDirHu = spellIndexDirHu;
	}

}

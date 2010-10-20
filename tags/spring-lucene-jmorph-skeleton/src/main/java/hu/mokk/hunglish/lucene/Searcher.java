/**
 * 
 */
package hu.mokk.hunglish.lucene;

import hu.mokk.hunglish.domain.Bisen;

import java.io.File;
import java.io.IOException;

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

	private String indexDir;
	private Integer maxDocumments = 1000;
	
	private IndexSearcher searcher;
	private IndexReader indexReader;
	
	private String useHunglishSyntax = "true";
	//private Boolean setHunglishSyntax = false;
	
	@Autowired
	private LuceneQueryBuilder luceneQueryBuilder;
	
	public void initSearcher() {
		boolean readOnly = true;
		if (indexReader == null) {
			try {
				indexReader = IndexReader.open(new SimpleFSDirectory(new File(
						indexDir)), readOnly);
				searcher = new IndexSearcher(indexReader);
				luceneQueryBuilder = new LuceneQueryBuilder();
			} catch (CorruptIndexException e) {
				throw new RuntimeException("Cannot open index directory.", e);
			} catch (IOException e) {
				//throw new RuntimeException("Cannot open index directory.", e);
				indexReader =null;
			}
		}
	}

	private void checkState(){
		if (indexReader == null || searcher == null || luceneQueryBuilder == null){
			throw new IllegalStateException("can not search. Initialize searcher first.");
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
		Query query = null;
		try {
			query = luceneQueryBuilder.parseRequest(request);
		} catch (ParseException e1) {
			// TODO Auto-generated catch block
			//e1.printStackTrace();
			throw new RuntimeException("Request couldn't be parsed", e1);
		}
		
		// Collect enough docs to show 1 pages
		TopScoreDocCollector collector = TopScoreDocCollector.create(
				maxDocumments, false);

		try {
			searcher.search(query, collector);
		} catch (IOException ioe) {
			throw new RuntimeException(ioe);
		}
		ScoreDoc[] hits = collector.topDocs().scoreDocs;
		int totalHits = collector.getTotalHits();
		result.setTotalCount(totalHits);

		result.setStartOffset(request.getStartOffset());
		int end = request.getStartOffset() + request.getMaxResults();
		if (end > totalHits) {
			end = totalHits;
		}
		result.setEndOffset(end);

		// kiemeleshez
		// String queryTerms[] = SimpleQueryTermExtractor.getTerms(query);
		// Highlighter highlighter = new Highlighter("b", null);
		// */

		// kiolvassuk az aktualis lapon levo dokumentumokat, csinalunk beloluk
		// bimondatot, plusz adunk hozza highlightingot
		for (int i = request.getStartOffset(); i < end; i++) {
			Document d;
			int docId = hits[i].doc;
			try {
				d = searcher.doc(docId);
			} catch (IOException ioe) {
				throw new RuntimeException(ioe);
			}

			//TODO FIXME request Bisens from the db in one batch, that is: where id in (<idlist>)
			//public static List<Bisen> findBisenEntries(List<Long> ids)
			Bisen bisen = Bisen.toBisen(d);

			if (request.nonEmptyHuQuery()) { // && request.getHighlightHu()
				try {
					TokenStream huTokens = TokenSources.getTokenStream(
							indexReader, docId, Bisen.huSentenceFieldName);
					bisen.setHuSentenceView((highlightField(huTokens, query,
							Bisen.huSentenceFieldName, bisen.getHuSentence())));
				} catch (Exception e) {
					e.printStackTrace(); //TODO FIXME
					//throw new RuntimeException("error while highlighting", e);
				}
			}

			if (request.nonEmptyEnQuery()) { // && request.getHighlightEn()
				try {
					TokenStream enTokens = TokenSources.getTokenStream(
							indexReader, docId, Bisen.enSentenceFieldName);
					bisen.setEnSentenceView((highlightField(enTokens, query,
							Bisen.enSentenceFieldName, bisen.getEnSentence())));
				} catch (Exception e) {
					e.printStackTrace(); //TODO FIXME
					//throw new RuntimeException("error while highlighting", e);
				}
			}
			result.addToHits(bisen);
		}
		return result;
		
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



}

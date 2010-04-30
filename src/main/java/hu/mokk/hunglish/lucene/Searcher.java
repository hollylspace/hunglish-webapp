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

	@Autowired
	private String indexDir;
	@Autowired
	private Integer maxDocumments = 1000;
	
	private IndexSearcher searcher;
	private IndexReader indexReader;
	
	@Autowired
	private LuceneQueryBuilder queryBuilder;
	
	synchronized public void initSearcher() {
		boolean readOnly = true;
		if (indexReader == null) {
			try {
				indexReader = IndexReader.open(new SimpleFSDirectory(new File(
						indexDir)), readOnly);
			} catch (CorruptIndexException e) {
				throw new RuntimeException("Cannot open index directory.", e);
			} catch (IOException e) {
				throw new RuntimeException("Cannot open index directory.", e);
			}
			searcher = new IndexSearcher(indexReader);
		}
		if (searcher == null){
			searcher = new IndexSearcher(indexReader);
		}
		if (queryBuilder == null){
			queryBuilder = new LuceneQueryBuilder();
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

	public SearchResult search(SearchRequest request){
		//TODO FIXME
		initSearcher();
		Query query = null;
		try {
			query = queryBuilder.parseRequest(request);
		} catch (ParseException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		SearchResult result = new SearchResult();

		// Collect enough docs to show 1 pages
		TopScoreDocCollector collector = TopScoreDocCollector.create(
				maxDocumments, false);

		try {
			searcher.search(query, collector);
		} catch (IOException ioe) {
			throw new RuntimeException(ioe);
		}
		ScoreDoc[] hits = collector.topDocs().scoreDocs;
		int l = collector.getTotalHits();

		result.setTotalCount(l);
		result.setStartOffset(request.getStartOffset());
		int end = request.getStartOffset() + request.getMaxResults();
		if (end > l) {
			end = l;
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

			Bisen bisen = Bisen.toBisen(d);

			if (request.isLeftQuery()) {
				try {
					TokenStream leftTokens = TokenSources.getTokenStream(
							indexReader, docId, Bisen.huSentenceFieldName);
					bisen.setHuSentenceView((highlightField(leftTokens, query,
							Bisen.huSentenceFieldName, bisen.getHuSentence())));
				} catch (Exception e) {
					throw new RuntimeException("error while highlighting", e);
				}
			}

			if (request.isRightQuery()) {
				try {
					TokenStream leftTokens = TokenSources.getTokenStream(
							indexReader, docId, Bisen.enSentenceFieldName);
					bisen.setEnSentenceView((highlightField(leftTokens, query,
							Bisen.enSentenceFieldName, bisen.getEnSentence())));
				} catch (Exception e) {
					throw new RuntimeException("error while highlighting", e);
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

	public void setIndexReader(IndexReader indexReader) {
		this.indexReader = indexReader;
	}

}

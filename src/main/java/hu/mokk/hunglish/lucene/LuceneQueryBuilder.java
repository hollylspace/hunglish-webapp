package hu.mokk.hunglish.lucene;

import hu.mokk.hunglish.domain.Bisen;
import hu.mokk.hunglish.lucene.query.HunglishQueryParser;

import java.util.HashMap;

import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.FilteredQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.util.Version;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

/**
 * SearchRequest objektumbol csinal lucene szamara ertheto Queryt
 */
@Configurable
public class LuceneQueryBuilder {

	private HashMap<String, SourceFilter> sourceFilterCache = new HashMap<String, SourceFilter>();

	@Autowired
	private HunglishQueryParser queryParser;
	
	private Query simpleParseSearchRequest(SearchRequest request) throws ParseException {
		BooleanQuery result = new BooleanQuery();
		String huRequest;
		huRequest = request.getHuQuery();
		if (huRequest != null && huRequest.length() > 0) {
			
			Query huQuery = new QueryParser(Version.LUCENE_30,
					Bisen.huSentenceStemmedFieldName, queryParser.getAnalyzerProvider()
							.getAnalyzer()).parse(huRequest);
			result.add(huQuery, Occur.SHOULD);
		}

		if (request.getEnQuery() != null
				&& request.getEnQuery().length() > 0) {
			Query enQuery = new QueryParser(Version.LUCENE_30,
					Bisen.enSentenceStemmedFieldName, queryParser.getAnalyzerProvider()
							.getAnalyzer()).parse(request.getEnQuery());
			result.add(enQuery, Occur.SHOULD);
		}
		return result;
	}
	
	public Query parseRequest(SearchRequest request) throws ParseException {
		Query query; 
		try {
			if (request.getHunglishSyntax()){
				query = new BooleanQuery();
				query = queryParser.parse(request.getHuQuery(), request.getEnQuery());
			} else {
				query = simpleParseSearchRequest(request);
			}
		} catch (Exception e) {
			//e.printStackTrace(); 
			throw new RuntimeException("query couldn't be parsed", e);
		}
		
		if (request.getSourceId() != null){
			query = addSourceFilter(query, Bisen.genreFieldName, request.getSourceId());
		}
		
		return query;
	}

	private Query addSourceFilter(Query q, String fieldName, String sourceId) {
		if (sourceId == null || "".equals(sourceId)) {
			return q;
		}
		SourceFilter filter = null;
		// TODO: ha valami tobb indexben is keresni kell, akkor itt azzal kell
		// a kulcsot kepezni, mert ott mas, es mas lehet a source filter
		// bitsetsje;

		synchronized (sourceFilterCache) { // check cache
			filter = (SourceFilter) sourceFilterCache.get(sourceId);
			if (filter == null) {
				filter = new CachingSourceFilter(fieldName, sourceId);
				sourceFilterCache.put(sourceId, filter);
			}
		}

		FilteredQuery qf = new FilteredQuery(q, filter);
		return qf;
	}

	public static void printBytes(byte[] array, String name) {
		for (int k = 0; k < array.length; k++) {
			System.out.println(name + "[" + k + "] = " + "0x"
					+ byteToHex(array[k]));
		}
	}

	static public String byteToHex(byte b) {
		// Returns hex String representation of byte b
		char hexDigit[] = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
				'a', 'b', 'c', 'd', 'e', 'f' };
		char[] array = { hexDigit[(b >> 4) & 0x0f], hexDigit[b & 0x0f] };
		return new String(array);
	}

	public void setQueryParser(HunglishQueryParser queryParser) {
		this.queryParser = queryParser;
	}

	
}

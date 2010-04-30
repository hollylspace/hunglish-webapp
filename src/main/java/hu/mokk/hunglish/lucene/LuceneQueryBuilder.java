package hu.mokk.hunglish.lucene;

import hu.mokk.hunglish.domain.Bisen;
import hu.mokk.hunglish.jmorph.AnalyzerProvider;

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

	private HashMap<String, SourceFilter> sourceFilterCache  = new HashMap<String, SourceFilter>();
	
	@Autowired
	private AnalyzerProvider analyzerProvider;


	public static void printBytes(byte[] array, String name) {
		for (int k = 0; k < array.length; k++) {
			System.out.println(name + "[" + k + "] = " + "0x" + byteToHex(array[k]));
		}
}

static public String byteToHex(byte b) {
	// Returns hex String representation of byte b
	char hexDigit[] = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
			'a', 'b', 'c', 'd', 'e', 'f' };
	char[] array = { hexDigit[(b >> 4) & 0x0f], hexDigit[b & 0x0f] };
	return new String(array);
}

	private Query parseSearchRequest(SearchRequest request){
		BooleanQuery result = new BooleanQuery();
		try {
			String leftRequest; 
			leftRequest = request.getLeftQuery();
			if (leftRequest != null
					&& leftRequest.length() > 0) {
				Query leftQuery = new QueryParser(Version.LUCENE_30,
						Bisen.huSentenceFieldName, analyzerProvider.getAnalyzer())
						.parse(leftRequest);
				result.add(leftQuery, Occur.SHOULD);
			}
			
		} catch (ParseException e) {
			throw new RuntimeException("left query couldn't be parsed", e);
		}
		try {
			if (request.getRightQuery() != null
					&& request.getRightQuery().length() > 0) {
				Query rightQuery = new QueryParser(Version.LUCENE_30,
						Bisen.enSentenceFieldName, analyzerProvider.getAnalyzer())
						.parse(request.getRightQuery());
				result.add(rightQuery, Occur.SHOULD);
			}
		} catch (ParseException e) {
			throw new RuntimeException("right query couldn't be parsed", e);
		}
		return result;
	}

	public Query parseRequest(SearchRequest request) throws ParseException {
		Query query = parseSearchRequest(request);
		query = addSourceFilter(query, "source", request.getSourceId());
		return query;
	}

	private Query addSourceFilter(Query q, String fieldName, String sourceId) {
		if (sourceId == null || sourceId == ""
				|| (sourceId.equals("all") && fieldName.equals("source"))) {
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

	public void setAnalyzerProvider(AnalyzerProvider analyzerProvider) {
		this.analyzerProvider = analyzerProvider;
	}

}

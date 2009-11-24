/*
 * Created by Peter Halacsy <peter at halacsy.com>
 * 
 * This work is licensed under the Creative Commons 
 * Attribution License. To view a copy of this license, 
 * visit http://creativecommons.org/licenses/by/2.0/ 
 * or send a letter to Creative Commons, 559 Nathan Abbott Way, 
 * Stanford, California 94305, USA.
 * 
 * Created on May 19, 2005
 *
 */

package mokk.nlp.bicorpus.index.lucene;

import java.util.HashMap;

import mokk.nlp.bicorpus.index.SearchRequest;
import mokk.nlp.bicorpus.index.query.ParseException;

import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.FilteredQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;

/**
 * SearchRequest objektumbol csinal lucene szamara ertheto Queryt 
 */
public class LuceneQueryBuilder {

    private HashMap sourceFilterCache;
    
    
    private QueryParser qp = null;
    
    /**
     * 
     */
    public LuceneQueryBuilder(QueryParser qp) {
        this.qp = qp;
        sourceFilterCache = new HashMap();
        
        
    }

    public Query parseRequest(SearchRequest request) throws  ParseException {
        
    		//String leftField = "left";
    		//String rightField = "right";
    		
    		
    //		if(request.getCommonQuery() != null ) {
    	//	    QueryParser qp = new QueryParser("nodefault", )
    	//	}
    		
    		
    	Query query = qp.parse(request.getLeftQuery(), request.getRightQuery());
    		
    		//duplicate filter
    		if (request.isExcludeDuplicates()){
    			Term term = new Term(BisMapper.isDuplicateName, BisMapper.NO);
    			TermQuery termQuery = new TermQuery(term);
    			BooleanClause bc = new BooleanClause(termQuery, BooleanClause.Occur.MUST);
    			((BooleanQuery)query).add(bc);
    		} 
    		
    		
    		if(query == null) {
    		    throw new ParseException ("no query");
    		}
    		
    		query = addSourceFilter(query, request.getSourceId());
    		return query;
    	
    }
    
    private Query addSourceFilter(Query q, String sourceId) {
        if(sourceId == null || sourceId == "" || sourceId.equals("all")) {
            return q;
        }
        SourceFilter filter = null;
        // TODO: ha valami tobb indexben is keresni kell, akkor itt azzal kell
        // a kulcsot kepezni, mert ott mas, es mas lehet a source filter bitsetsje;
        
        synchronized (sourceFilterCache) {  // check cache
            filter = (SourceFilter) sourceFilterCache.get(sourceId);
            if (filter == null) {
              filter = new CachingSourceFilter(sourceId);
              sourceFilterCache.put(sourceId, filter);
            }
          }

          FilteredQuery qf = new FilteredQuery(q, filter);
          return qf;
    }
   
}

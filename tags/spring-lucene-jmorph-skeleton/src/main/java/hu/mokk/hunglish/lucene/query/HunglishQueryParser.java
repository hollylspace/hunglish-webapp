/**
 * 
 */
package hu.mokk.hunglish.lucene.query;

import hu.mokk.hunglish.domain.Bisen;
import hu.mokk.hunglish.jmorph.AnalyzerProvider;

import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.PhraseQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.util.Version;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author Peter Halacsy <peter at halacsy.com>
 *
 */
public class HunglishQueryParser {

	@Autowired
	private AnalyzerProvider analyzerProvider;
 
    public HunglishQueryParser() {
    }
    
 


    public Query parse(String hu, String en) throws Exception {
    	HunglishQuerySyntaxParser qp = new HunglishQuerySyntaxParser();
        QueryStructure qs = qp.parse(hu, en);
        
        QueryPhrase[] phrases = qs.getPhrases();
        
        if(phrases.length == 0) {
            return null;
        }
        BooleanQuery theQuery = new BooleanQuery();
        
        
        for (int i = 0; i < phrases.length; i++) {
            Query q = phraseToQuery(phrases[i]);
            BooleanClause bc = null;

            if (phrases[i].getQualifier() == QueryPhrase.Qualifier.MUSTNOT) {
                bc = new BooleanClause(q, BooleanClause.Occur.MUST_NOT);
            } else if (phrases[i].getQualifier() == QueryPhrase.Qualifier.MUST) {
                bc = new BooleanClause(q, BooleanClause.Occur.MUST);
            } else {
                bc = new BooleanClause(q, BooleanClause.Occur.SHOULD);
            }

            theQuery.add(bc);

        }

        return theQuery;
    }

    private Query phraseToQuery(QueryPhrase phrase) {
    	String luceneField = null;
    	if (phrase.stemmed){
    		luceneField = Bisen.huSentenceStemmedFieldName;
	        if (phrase.field == QueryPhrase.Field.EN) {
	            luceneField = Bisen.enSentenceStemmedFieldName;
	        }
    	} else {
    		luceneField = Bisen.huSentenceFieldName;
	        if (phrase.field == QueryPhrase.Field.EN) {
	            luceneField = Bisen.enSentenceFieldName;
	        }
    	}

		
    	/*--------old HUnglish version----------*/
        String [] terms = phrase.getTerms();
    	if (terms.length == 0) {
            return null;
        } else if (terms.length == 1) {
            return new TermQuery(new Term(luceneField, terms[0]));
        }

    	PhraseQuery result = new PhraseQuery(); 
        for (int i = 0; i < terms.length; i++) {
            result.add(new Term(luceneField, terms[i]));
        }
    	return result;

        /*----------new version-----------*/
        /*
		Query result = null;
    	QueryParser luceneQueryParser = new QueryParser(Version.LUCENE_CURRENT,
				luceneField, analyzerProvider.getAnalyzer());
    	try {
			//TODO FIXME create separate query object from each term and
			//combine them with boolean query
			String wtf = phrase.getTermsSpaceSeparated(); 
			//System.out.println("wft"+wtf);
			result = luceneQueryParser.parse(wtf);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new RuntimeException("Parse error while parsing:"+phrase.getTermsSpaceSeparated(), e);
		}//*/	        
    	
    }

    
	public AnalyzerProvider getAnalyzerProvider() {
		return analyzerProvider;
	}




	public void setAnalyzerProvider(AnalyzerProvider analyzerProvider) {
		this.analyzerProvider = analyzerProvider;
	}
}
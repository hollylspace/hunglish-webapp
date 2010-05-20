/**
 * 
 */
package hu.mokk.hunglish.lucene.query;

import hu.mokk.hunglish.domain.Bisen;
import hu.mokk.hunglish.jmorph.AnalyzerProvider;
import hu.mokk.hunglish.jmorph.LemmatizerWrapper;

import java.util.Iterator;
import java.util.List;

import net.sf.jhunlang.jmorph.lemma.Lemma;

import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.MultiPhraseQuery;
import org.apache.lucene.search.PhraseQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author Peter Halacsy <peter at halacsy.com>
 *
 */
public class QueryParser {

    //protected  Lemmatizer leftLemmatizer = null;
    //protected  Lemmatizer rightLemmatizer = null;
	@Autowired
	private AnalyzerProvider analyzerProvider;
 
    public QueryParser() {
    }
    
 


    public Query parse(String left, String right) throws Exception {
    	HunglishQueryParser qp = new HunglishQueryParser();
        QueryStructure qs = qp.parse(left, right);
        
        QueryPhrase[] phrases = qs.getPhrases();
        
        if(phrases.length == 0) {
            return null;
        }
        BooleanQuery theQuery = new BooleanQuery();
        
        
        for (int i = 0; i < phrases.length; i++) {
            Query q = termsToQuery(phrases[i].getField(), phrases[i].getTerms());
            BooleanClause bc = null;
            Query stemmedQuery = null;

            if (phrases[i].isStemmed() == true) {
                stemmedQuery = getStemmedQuery(q);
            }

            if (phrases[i].getQualifier() == QueryPhrase.Qualifier.MUSTNOT) {
                bc = new BooleanClause(q, BooleanClause.Occur.MUST_NOT);
            } else if (phrases[i].getQualifier() == QueryPhrase.Qualifier.MUST) {
                if (stemmedQuery != null) {
                    // ha vannak tovek, akkor az eredeti alak opcionalis
                    bc = new BooleanClause(q, BooleanClause.Occur.SHOULD);
                } else {
                    bc = new BooleanClause(q, BooleanClause.Occur.MUST);
                }
            } else {
                bc = new BooleanClause(q, BooleanClause.Occur.SHOULD);
            }

            theQuery.add(bc);

            if (stemmedQuery != null) {

                if (phrases[i].getQualifier() == QueryPhrase.Qualifier.MUSTNOT) {
                    bc = new BooleanClause(stemmedQuery,
                            BooleanClause.Occur.MUST_NOT);
                } else if (phrases[i].getQualifier() == QueryPhrase.Qualifier.MUST) {
                    bc = new BooleanClause(stemmedQuery,
                            BooleanClause.Occur.MUST);
                } else {
                    bc = new BooleanClause(stemmedQuery,
                            BooleanClause.Occur.SHOULD);
                }
                theQuery.add(bc);
            }
        }

        return theQuery;
    }

    /**
     * @param q
     * @return
     */
    private Query getStemmedQuery(Query q) {
        if (q instanceof PhraseQuery) {
            return getStemmedQuery((PhraseQuery) q);
        } else if (q instanceof TermQuery) {
            return getStemmedQuery((TermQuery) q);
        }

        return null;
    }

    private Query getStemmedQuery(PhraseQuery pq) {
        MultiPhraseQuery mpq = new MultiPhraseQuery();

        Term[] terms = pq.getTerms();
        if (terms.length == 0) {
            return null;
        }
        int position = 0;
        for (int i = 0; i < terms.length; i++) {
            Term[] stems = stemTerm(terms[i]);
            if (stems.length != 0) {
                mpq.add(stems, position);

            }
            // ha nincs szoto egy szo helyett, akkor kihagyjuk, de a poziciojat
            // megtartjuk
            // ez lehet, h nem jo, mert az indexelesnel lehet, h nem hagytak ki
            // helyet
            // a not stemmed szavaknal
            position++;
        }

        return mpq;
    }

    /**
     * Egy termbol tovezesebol elloallit egy query-t. Ha nincs to, akkor null-t
     * ad vissza. Ha egy to van, akkor sima TermQuery-t. Ha tobb, akkor OR-ral
     * osszekapcsolja oket
     * 
     * @param t
     * @return
     */
    private Query getStemmedQuery(TermQuery tq) {
        Term t = tq.getTerm();
        Term[] stemTerms = stemTerm(t);
        if (stemTerms == null || stemTerms.length == 0) {
            return null;
        }

        if (stemTerms.length == 1) {
            return new TermQuery(stemTerms[0]);
        }

        BooleanQuery q = new BooleanQuery();
        for (int i = 0; i < stemTerms.length; i++) {

            TermQuery currentQuery = new TermQuery(stemTerms[i]);
            q.add(currentQuery, BooleanClause.Occur.SHOULD);
        }
        return q;

    }

    private Term[] stemTerm(Term t) {
        //String luceneField = t.field() + "_stemmed";
        
    	
    	LemmatizerWrapper lemmatizer = analyzerProvider.getLemmatizerMap().get(t.field());
        
        Term[] terms;
        List lemmas = lemmatizer.lemmatize(t.text());
        
   
        
        terms = new Term[lemmas.size()];
        Iterator it = lemmas.iterator();
        
        for(int i = 0; it.hasNext(); i++) {
            Lemma lemma = (Lemma) it.next();
            String term;
            term = lemma.getWord();
            terms[i] = new Term(t.field(), term);
        }
        return terms;

    }

    private String[] stem(String s) {
        String[] stems = new String[3];
        stems[0] = s + "1";
        stems[1] = s + "2";
        stems[2] = s + "3";

        return stems;
    }

    private Query termsToQuery(QueryPhrase.Field field, String[] terms) {
        String luceneField = Bisen.huSentenceFieldName;

        if (field == QueryPhrase.Field.RIGHT) {
            luceneField = Bisen.enSentenceFieldName;
        }

        if (terms.length == 0) {
            return null;
        } else if (terms.length == 1) {
            return new TermQuery(new Term(luceneField, terms[0]));
        }

        PhraseQuery pq = new PhraseQuery();
        for (int i = 0; i < terms.length; i++) {
            pq.add(new Term(luceneField, terms[i]));
        }

        return pq;
    }




	public AnalyzerProvider getAnalyzerProvider() {
		return analyzerProvider;
	}




	public void setAnalyzerProvider(AnalyzerProvider analyzerProvider) {
		this.analyzerProvider = analyzerProvider;
	}
}
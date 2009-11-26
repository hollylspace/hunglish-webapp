/*
 * Created by Peter Halacsy <peter at halacsy.com>
 * 
 * This work is licensed under the Creative Commons 
 * Attribution License. To view a copy of this license, 
 * visit http://creativecommons.org/licenses/by/2.0/ 
 * or send a letter to Creative Commons, 559 Nathan Abbott Way, 
 * Stanford, California 94305, USA.
 * 
 * Created on Jul 13, 2005
 *
 */

package mokk.nlp.bicorpus.index.lucene;

import java.util.Iterator;
import java.util.List;

import net.sf.jhunlang.jmorph.lemma.Lemma;
import mokk.nlp.jmorph.Lemmatizer;

import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.*;

import mokk.nlp.bicorpus.index.query.HunglishQueryParser;
import mokk.nlp.bicorpus.index.query.ParseException;
import mokk.nlp.bicorpus.index.query.QueryPhrase;
import mokk.nlp.bicorpus.index.query.QueryStructure;

/**
 * @author hp
 * 
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
public class QueryParser {

  

    protected  Lemmatizer leftLemmatizer = null;
    protected  Lemmatizer rightLemmatizer = null;

    
    protected boolean returnPOS = false;
    

    
    public QueryParser(Lemmatizer leftLemmatizer, Lemmatizer rightLemmatizer) {
        this.leftLemmatizer = leftLemmatizer;
        this.rightLemmatizer = rightLemmatizer;
     
    }
    
 


    public Query parse(String left, String right) throws ParseException {
        HunglishQueryParser qp = new HunglishQueryParser();
        QueryStructure qs = qp.parse(left, right);
        
        QueryPhrase[] phrases = qs.getPhrases();
        
        if(phrases.length == 0) {
            return null;
        }
        BooleanQuery theQuery = new BooleanQuery();
        //PhraseQuery phraseQuery = new PhraseQuery();
        
        
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
            //phraseQuery.add(term)

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
        String luceneField = t.field() + "_stemmed";
        Lemmatizer lemmatizer = t.field().equals("left") ? leftLemmatizer : rightLemmatizer;
        
        Term[] terms;
        List lemmas = lemmatizer.lemmatize(t.text());
        
   
        
        terms = new Term[lemmas.size()];
        Iterator it = lemmas.iterator();
        
        for(int i = 0; it.hasNext(); i++) {
            Lemma lemma = (Lemma) it.next();
            String term;
            if(returnPOS) {
                term = lemma.getWord() + "/" + lemma.getPOS();
            } else {
                term = lemma.getWord();
            }
            terms[i] = new Term(luceneField, term);
        }
        return terms;

    }

    /*private String[] stem(String s) {
        String[] stems = new String[3];
        stems[0] = s + "1";
        stems[1] = s + "2";
        stems[2] = s + "3";
        return stems;
    }*/

    private Query termsToQuery(QueryPhrase.Field field, String[] terms) {
        String luceneField = "left";

        if (field == QueryPhrase.Field.RIGHT) {
            luceneField = "right";
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
}
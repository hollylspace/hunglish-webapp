/*
 * Created on Jan 1, 2005
 *
 *
 */
package mokk.nlp.irutil.lucene.analysis;



import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import net.sf.jhunlang.jmorph.lemma.Lemma;
import net.sf.jhunlang.jmorph.lemma.Lemmatizer;

import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.TokenStream;


/**
 * @author hp
 *

 */
public class StemmerTokenFilter extends CloningTokenFilter {

    protected Lemmatizer lemmatizer = null;

    protected boolean returnOOVOrig;
    
    protected boolean returnPOS;
    
    protected boolean returnOrig;
    
	public StemmerTokenFilter (Lemmatizer lemmatizer, boolean returnOOVOrig, boolean returnPOS , TokenStream input)  {

		this(lemmatizer, false, returnOOVOrig, returnPOS,input);
	}
	
	public StemmerTokenFilter (Lemmatizer lemmatizer, boolean returnOrig, boolean returnOOVOrig, boolean returnPOS , TokenStream input)  {
		super(input);
		this.returnOrig = returnOrig;
		this.lemmatizer = lemmatizer;
		this.returnOOVOrig = returnOOVOrig;
		this.returnPOS = returnPOS;
	}

	/* (non-Javadoc)
	 * @see org.apache.lucene.analysis.TokenStream#next()
	 */
	public Token next() throws IOException {
		// Ha van a veremben meg token, akkor azt adjuk vissza
		Token t = _next();
		// if(t != null) {
		//	System.out.println(t.getPositionIncrement() + t.termText());
		// }
		return t;
		
	}
	public Token _next() throws IOException {
		Token t = pop();
		if(t != null) {
			return t;
		}
		
		// egyebkent vesszuk a kovetkezo tokent es toveljuk
		t = input.next();
		if(t == null) {
			return null;
		}
		List lemmas = lemmatizer.lemmatize(t.termText());
		
		// az eredeti tokent csak akkor adjuk vissza, ha a szo ismeretlen
		// es kertek
		if(lemmas.size() == 0) {
		    if (returnOOVOrig || returnOrig) {
		        return t;
		    } else {
		        return next();
		    }
		}
		
		
		Iterator it = lemmas.iterator();
		boolean isFirst = true;
		while(it.hasNext()) {
		    Lemma lemma = (Lemma) it.next();
		    
		   
			// Token(String text,  int start,  int end,  String typ) 
			
		
		    	// egyebkent a tovek jonnek, de ebbol tobb is lehet. az elsonel
		    // setPositionIncrement = 1 es azt adjuk vissza, a tobbinel nulla
		
		
		    
			Token stemToken = null;
			if(returnPOS) {
			    stemToken = new Token(lemma.getWord() + "/" + lemma.getPOS(), 
						   t.startOffset(), 
						   t.endOffset(), 
						   t.type());
			} else {
			    stemToken = new Token(lemma.getWord(), 
						   t.startOffset(), 
						   t.endOffset(), 
						   t.type());
			}
			// put the token representing the stem to the same position as
			// the original word if the orig word won't be returned
			if(returnOrig || !isFirst) {
			    stemToken.setPositionIncrement(0);
			}
			push(stemToken);
			isFirst = false;
			
		}
		// ha returnOrig, akkor visszaadjuk, a to(vek) majd a kovetkezo
		// hivasokkal a verembol
		if(returnOrig) {
		    return t;
		}
		// ez itt biztonsagi jatek. elvileg nem lehet, hogy nem raktunk tovet
		// a bufferbe
		Token lemmatizedToken = getFirst();
		if(lemmatizedToken == null) {
		    return t;
		}
		
	
		return lemmatizedToken;
	}
}

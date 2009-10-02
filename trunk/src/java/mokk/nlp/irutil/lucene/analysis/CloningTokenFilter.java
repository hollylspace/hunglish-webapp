/*
 * Created on Jan 2, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package mokk.nlp.irutil.lucene.analysis;

import java.io.IOException;
import java.util.LinkedList;

import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;

/**
 * Egy sima TokenFilter a TokenStream-tol egyenkent kapott Tokeneket
 * szuri vagy alakitja at. Az olyan filtereknek, amik egy tokent megsokszoroznak
 * (pl. StemmerTokenFilter, ami egy Token minden lehetseges tovet visszaadja)
 * ket hivas kozt meg kell tartani a tokeneket. Ebben segit ez az osztaly.
 */
public abstract class CloningTokenFilter extends TokenFilter {

	private LinkedList buffer;
	
	public CloningTokenFilter(TokenStream input) {
		super(input);
		buffer = new LinkedList();
	}
	
	protected void push(Token t) {
		buffer.addLast(t);
	}
	
	protected Token pop() {
		if(buffer.size() == 0) {
			return null;
		}
		
		Token t = (Token) buffer.getLast();
		if(t != null) {
			buffer.removeLast();
		}
		return t;
	}
	
	protected Token getFirst() {
		if(buffer.size() == 0) {
			return null;
		}
		
		Token t = (Token) buffer.getFirst();
		if(t != null) {
			buffer.removeFirst();
		}
		return t;
	}
	/* (non-Javadoc)
	 * @see org.apache.lucene.analysis.TokenStream#next()
	 */
	public abstract Token next() throws IOException ;

}

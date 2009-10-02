/*
 * Created by Peter Halacsy <peter at halacsy.com>
 * 
 * This work is licensed under the Creative Commons 
 * Attribution License. To view a copy of this license, 
 * visit http://creativecommons.org/licenses/by/2.0/ 
 * or send a letter to Creative Commons, 559 Nathan Abbott Way, 
 * Stanford, California 94305, USA.
 * 
 * Created on Jun 15, 2005
 *
 */

package mokk.nlp.irutil.lucene.highlight;

import java.io.IOException;
import java.io.StringReader;
import java.util.Iterator;
import java.util.LinkedList;

import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.highlight.QueryTermExtractor;
import org.apache.lucene.search.highlight.WeightedTerm;

/**
 * @author hp
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class Highlighter {
    String hlTag, hlClass;
    
    	public Highlighter (String hlTag, String hlClass) {
    	    this.hlTag = hlTag;
    	    this.hlClass = hlClass;
    	}
    	
    	public String highlight(String text, TokenStream tokenStream, String[] queryTerms) {
    	
    	        
    	        //logger.debug(field + text);
    	        
    	        StringBuffer res = new StringBuffer(text.length());
    	        int prev = 0;
    	        
    	  
    	        
    	        // a tokenstream tobb furcsasagot is tartalmazhat. Peldaul
    	        // ket token kozott kimaradhat hely, es egymas utani
    	        // tokenek hivatkozhatnak az eredeti szoveg ugyanazon helyere (stemmer)
    	        // utobbi esetben ugye nem akarunk ket taget betenni
    	        // ha az kapjuk, hogy az 5-10 poziciot ki kell emelni, akkor
    	        // elobb 0-5 bemasolas, start tag, 5-10, end tag
    	        // de ha a kovetkezo token 6-11 is azt is ki kell emelni, akkor 5-11 a kimeles
    	        // de mi kihasznaljuk, hogy a tokenek vagy ugyanarra mutatnak, vagy nem lapolodnak at
    	        LinkedList matchedToken = new LinkedList();
    	        // nem tul effektiven keresunk, dehat keves terms van
    	        Token token = null;
    	        try {
    	            while((token = tokenStream.next()) != null) {
    	             //  System.out.println("Token: " + token.termText());
    	                for(int i = 0; i<queryTerms.length; i++) {
    	               
    	                    if(queryTerms[i].compareTo(token.termText()) == 0) {
    	                     matchedToken.add(token);  
    	                     break;
    	                    }   
    	                }
    	            } 
    	        } catch (IOException e) {
    	           System.err.println("can't parse string:" + text);
    	            return text;
    	        }
    	        
    	  //   System.out.println("tn = " + matchedToken.size());
    	        // vegig a tokenekek is kuszurjuk az egymasba skatulyazottakat. a kezdet
    	        // szerint sorrendben vannak, es egyik sem kezdodik elobb, mint a masik
    	        LinkedList filteredMatchedToken = new LinkedList();
    	        Iterator it = matchedToken.iterator();
    	        while(it.hasNext()) {
    	            token = (Token) it.next();
    	            if(filteredMatchedToken.size() == 0) {
    	           
    	                filteredMatchedToken.addLast(token);
    	            } else {
    	                Token lastToken = (Token) filteredMatchedToken.getLast();
    	                if (lastToken.startOffset() == token.startOffset()) {
    	                    Token v = new Token(lastToken.termText(), lastToken.startOffset(), Math.max(lastToken.endOffset(), token.endOffset()));
    	                    filteredMatchedToken.removeLast();
    	                    filteredMatchedToken.addLast(v);
    	                } else {
    	                    filteredMatchedToken.addLast(token);
    	                }
    	            }
    	        }
    	        
    	  //    System.out.println("ftn = " + filteredMatchedToken.size());
    	        // es a kiemeles
    	        int lastAppended = 0;
    	         it = filteredMatchedToken.iterator();
    	    
    	        while(it.hasNext()) {
    	            token = (Token) it.next();
    	            
    	            res.append(text.substring(lastAppended, token.startOffset()));
    	            tagIt(res, text.substring(token.startOffset(), token.endOffset()), hlTag, hlClass);
    	            lastAppended = token.endOffset();
    	        //    logger.debug("tok = " + token.termText());
    	         //   logger.debug(res.toString());
    	            
    	        }
    	        if(lastAppended <  text.length()-1) {
    	            res.append(text.substring(lastAppended, text.length()));
    	        // es a vege
    	        }
    	        return res.toString();
    	    }

    	 
    	    private static void tagIt(StringBuffer res, String text, String tag, String cl) {
    	        res.append("<");
    	        res.append(tag);
    	        if(cl != null) {
    	            
    	            res.append(" class =\"");
    	            res.append(cl);
    	            res.append("\"");
    	        }
    	        res.append(">");
    	        
    	        res.append(text);
    	     
    	        
    	        res.append("</");
    	        res.append(tag);
    	        res.append(">");
    	    }
    	}


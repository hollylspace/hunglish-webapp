/*
 * Created by Peter Halacsy <peter at halacsy.com>
 * 
 * This work is licensed under the Creative Commons 
 * Attribution License. To view a copy of this license, 
 * visit http://creativecommons.org/licenses/by/2.0/ 
 * or send a letter to Creative Commons, 559 Nathan Abbott Way, 
 * Stanford, California 94305, USA.
 * 
 * Created on May 6, 2005
 *
 */

package mokk.nlp.irutil.lucene;

import java.io.IOException;
import java.io.StringReader;
import java.util.Iterator;
import java.util.LinkedList;

import mokk.nlp.irutil.lucene.analysis.AnalyzerFactory;

import org.apache.avalon.framework.activity.Initializable;
import org.apache.avalon.framework.component.Component;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.logger.LogEnabled;
import org.apache.avalon.framework.logger.Logger;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.highlight.QueryTermExtractor;
import org.apache.lucene.search.highlight.WeightedTerm;
import org.apache.lucene.search.highlight.TokenSources;

/**
 * @avalon.component
 * @avalon.service type=mokk.nlp.irutil.lucene.Highlighter
 * @x-avalon.info name=simple-highlighter
 * @x-avalon.lifestyle type="singleton"
 */
public class SimpleHighlighter implements Highlighter, Component, 
 
Configurable,
LogEnabled,  
Initializable, 
Serviceable
{

    String hlClass;
    String hlTag;
    
    private Logger logger;
    private ServiceManager manager;
   
    private String analyzerId;
    private Analyzer analyzer;
    

 
    public void enableLogging(Logger logger) {
        this.logger = logger;
   
    }
  
    public void configure(Configuration config) throws ConfigurationException {
        hlTag = config.getChild("tag").getValue("span");
        logger.info("using hl-tag: " + hlTag);
		
        if( config.getChild("class", false) != null) {
            hlClass = config.getChild("class").getValue();
		}
        
		logger.info("using hl-class: " + hlClass);
		
		analyzerId = config.getChild("analyzer").getValue();
		logger.info("using analyzer: " + analyzerId);
		
    }
    
    /**
     * @avalon.dependency type="mokk.nlp.irutil.lucene.analysis.AnalyzerFactory"
     */
    public void service(ServiceManager manager) throws ServiceException {
        this.manager = manager;

    }

    public void initialize() throws Exception {
        logger.debug("getting analyzer");
        // lucene requeres that the used analyser is
        // subclass of Analyzer abstract class
        // But fortress likes only interfaces here
        AnalyzerFactory a = (AnalyzerFactory) manager.lookup(AnalyzerFactory.ROLE + "/" + analyzerId);

        analyzer = a.getAnalyzer();
        manager.release(a);
     
    
    }
    
   

    public String highlight(String field, String text, Query q) {
        
        //logger.debug(field + text);
        
        StringBuffer res = new StringBuffer(text.length());
        int prev = 0;
        
        WeightedTerm[] terms = QueryTermExtractor.getTerms(q, false);
        
        TokenStream t = analyzer.tokenStream(field, new StringReader(text));
        
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
            while((token = t.next()) != null) {
           //    logger.debug("Token: " + token.termText());
                for(int i = 0; i<terms.length; i++) {
                  
                    if(terms[i].getTerm().compareTo(token.termText()) == 0) {
                     matchedToken.add(token);  
                     break;
                    }   
                }
            } 
        } catch (IOException e) {
            logger.error("can't parse string:" + text);
            return text;
        }
        
      //logger.debug("tn = " + matchedToken.size());
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
        
      //logger.debug("ftn = " + filteredMatchedToken.size());
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

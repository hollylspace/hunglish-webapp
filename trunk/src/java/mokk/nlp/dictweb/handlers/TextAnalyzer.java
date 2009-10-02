/*
 * Created by Peter Halacsy <peter at halacsy.com>
 * 
 * This work is licensed under the Creative Commons 
 * Attribution License. To view a copy of this license, 
 * visit http://creativecommons.org/licenses/by/2.0/ 
 * or send a letter to Creative Commons, 559 Nathan Abbott Way, 
 * Stanford, California 94305, USA.
 * 
 * Created on May 9, 2005
 *
 */

package mokk.nlp.dictweb.handlers;


import java.io.StringReader;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import mokk.nlp.dictweb.RequestHandler;
import mokk.nlp.irutil.lucene.analysis.AnalyzerFactory;

import org.apache.avalon.framework.activity.Disposable;
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
import org.apache.velocity.context.Context;

/**
 * @author hp
 *
 * Handle request for analyze text as at indexing time.
 * 
 * @avalon.component
 * @avalon.service type=mokk.nlp.dictweb.RequestHandler
 * @x-avalon.info name=text-analyzer
 * @x-avalon.lifestyle type=singleton
 * 
 */
public class TextAnalyzer implements RequestHandler,
Component, 
LogEnabled, 
Configurable, 
Initializable, 
Serviceable, 
Disposable {


    private ServiceManager m_manager;
    
 
    private Logger m_logger;
    
  
    /*
     * Az analyser komponensek, amibol lehet valasztani
     */
    private List m_analysers;
    
    
	
	public void enableLogging(Logger logger) {
		this.m_logger = logger;
	}
	
	/*
	 * Reads the analyzer child with id, name subchilds
	 *  (non-Javadoc)
	 * @see org.apache.avalon.framework.configuration.Configurable#configure(org.apache.avalon.framework.configuration.Configuration)
	 */
    public void configure(Configuration config) throws ConfigurationException {
        m_analysers = new LinkedList();
        
        Configuration[] m_analysersConfig = config.getChildren("analyzer");
		for(int i = 0 ; i < m_analysersConfig.length ; i ++ )
		{
		    Configuration c = m_analysersConfig[i];
		    
		    String id = c.getChild("id").getValue();
		    String name = c.getChild("title").getValue();
		    String description = c.getChild("description").getValue("");
		    
		    AnalyzerEntry e = new AnalyzerEntry(id, name);
		    m_analysers.add(e);
		    
		    m_logger.info ("using analyzer id, name" + id + " " + name);
		}
		
		if ( m_analysers.size() == 0 )
		{
		    m_logger.error("no analyzer configurated");
		    throw new ConfigurationException("no analyzer configurated");
		}
        
    }
    
    /**
	 * @avalon.dependency type="mokk.nlp.irutil.lucene.analysis.MorphAnalyser"
	 */
	public void service(ServiceManager manager) throws ServiceException {
		this.m_manager = manager;
	}
	
    public void initialize() throws Exception {
        
        Iterator analysers = m_analysers.iterator();
        
        while(analysers.hasNext())
        {
            AnalyzerEntry e = (AnalyzerEntry) analysers.next();
            String id = e.getId();
            
            m_logger.info("looking up analyzer id = " + id);
            e.setAnalyzerFactory((AnalyzerFactory) m_manager.lookup(AnalyzerFactory.ROLE + "/" + id));
        
        }
        
  
    }
    

    public void dispose() {
        Iterator analysers = m_analysers.iterator();
        
        while(analysers.hasNext())
        {
            AnalyzerEntry e = (AnalyzerEntry) analysers.next();
            m_manager.release(e.getAnalyzerFactory());
        }
    }
    
    /* (non-Javadoc)
     * @see mokk.nlp.bicorpus.servlet.RequestHandler#handleRequest(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, org.apache.velocity.context.Context)
     */
    public String handleRequest(HttpServletRequest request,
            HttpServletResponse response, Context context) throws Exception {

       
        // the text to analyze
        String text = request.getParameter("text");
        if(text == null ) 
        {
            // no query specified
            return "index.vm";
        }
        
        String field = request.getParameter("field");
        if(field == null) {
            field = "left";
        }
        
        String analyserId = request.getParameter("id");
        if(analyserId == null) {
            analyserId = "default";
        }
        
        // seek the analyzer with id == analyserId
        AnalyzerEntry analyzer = null;
        
        Iterator analysers = m_analysers.iterator();
        
        while(analysers.hasNext())
        {
            AnalyzerEntry e = (AnalyzerEntry) analysers.next();
            if(e.getId().compareTo( analyserId ) == 0 ) {
                analyzer = e;
                break;
            }
        }
        
        // if no analyzer found, use the first one
        if(analyzer == null) 
        {
            m_logger.debug("no analyzer found to the id = " + analyserId);
            analyzer = (AnalyzerEntry) m_analysers.get(0);
            analyserId = analyzer.getId();
        }
       
        // a munka maga. fogni kell egy analyzert, es atkuldeni a szoveget
        // rajta. A tokeneket meg egy listaba rakni.
        
        Analyzer luceneAnalyzer = analyzer.getAnalyzerFactory().getAnalyzer();
        
        TokenStream tokens = luceneAnalyzer.tokenStream(field, new StringReader(text));
        
        List tokenResult = new LinkedList();
        
        
        
        Token t = null;
        while((t = tokens.next()) != null)
        {
            
                    tokenResult.add(t);
                   
        }
       
        
        context.put("text", text);
        context.put("id", analyserId);
        context.put("result", tokenResult);
        context.put("field", field);
        context.put("avalaibleAnalyzers", m_analysers);
    

        return "textanalyzer.vm";
        
    }


   /*
    * The information we know about an analyzer. The name and id is read
    * from the configuration.
    * @author hp
    *
     */
    public class AnalyzerEntry {
        /* The name of the analyser showed to the user */
        private String m_title;
        
        /* The id of the analyser could be used in the request */
        private String m_id;
        
        /* The component looked up by this class  */
        private AnalyzerFactory m_analyzerFactory;
        
        public AnalyzerEntry (String id, String title) 
        {
            this.m_id = id;
            this.m_title = title;
        }
        
        /**
         * @return Returns the analyser.
         */
        public AnalyzerFactory getAnalyzerFactory() {
            return m_analyzerFactory;
        }
        /**
         * @param analyser The analyser to set.
         */
        public void setAnalyzerFactory(AnalyzerFactory analyzer) {
            this.m_analyzerFactory = analyzer;
        }
        /**
         * @return Returns the id.
         */
        public String getId() {
            return m_id;
        }
        /**
         * @param id The id to set.
         */
        public void setId(String id) {
            this.m_id = id;
        }
        /**
         * @return Returns the name.
         */
        public String getTitle() {
            return m_title;
        }
        /**
         * @param name The name to set.
         */
        public void setTitle(String title) {
            this.m_title = title;
        }
    }
 

}

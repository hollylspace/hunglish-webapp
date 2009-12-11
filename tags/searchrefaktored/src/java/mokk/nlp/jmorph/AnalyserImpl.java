/*
 * Created on Jan 16, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package mokk.nlp.jmorph;

import java.io.File;
import java.util.List;

import net.sf.jhunlang.jmorph.Dictionaries;
import net.sf.jhunlang.jmorph.Rules;
import net.sf.jhunlang.jmorph.analysis.AnalyserContext;
import net.sf.jhunlang.jmorph.analysis.IStem;
import net.sf.jhunlang.jmorph.analysis.consumer.AnalysisConsumer;
import net.sf.jhunlang.jmorph.factory.Definition;
import net.sf.jhunlang.jmorph.factory.JMorphFactory;
import net.sf.jhunlang.jmorph.sample.AnalyserConfig;

import org.apache.avalon.fortress.util.ContextManagerConstants;
import org.apache.avalon.framework.activity.Initializable;
import org.apache.avalon.framework.component.Component;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.avalon.framework.logger.LogEnabled;
import org.apache.avalon.framework.logger.Logger;

// do not reformat the next lines (two ** at the begining)
/**
 * @author hp
 * @avalon.component
 * @avalon.service type=Analyser
 * @x-avalon.info name=jmorph
 * @x-avalon.lifestyle type="singleton"
 */

/*
 * Simple avalon-aware wrapper component around jmorph Analyser that
 * reads the configuration from like any other component. It delegates
 * every method to an Analyser object created by jmorph's factory.
 */
public class AnalyserImpl 	implements 

	Analyser,
	Component, 
	LogEnabled, 
	Configurable, 
	Initializable ,
	Contextualizable {
	
    public File contextDirectory = null;
    
   
	private Logger logger;
	
	/**
	 * Build at configuration.
	 */
    private Definition affixDef;
   
    /**
	 * Build at configuration.
	 */
    private Definition dicDef;

	
	
	/**
	 * The jmorph.stemmer that the work is delegated
	 */
	private net.sf.jhunlang.jmorph.analysis.Analyser  m_worker;
	
	///////////////////
	// Avalon methods
	///////////////////
	public void enableLogging(Logger logger) {
		this.logger = logger;

	}
	
	 public void contextualize(Context context) throws ContextException {
	     contextDirectory = (File) context.get(ContextManagerConstants.CONTEXT_DIRECTORY);
	     logger.info("context directory:" + contextDirectory );
	  }
	 

	public void configure(Configuration config) throws ConfigurationException {
	    String affFile, affEncoding, affReader, dicFile, dicEncoding, dicReader;
	
		
	    AnalyserConfig ac = new AnalyserConfig();
		
	    affFile = config.getChild("aff").getChild("file").getValue();
		affEncoding = config.getChild("aff").getChild("encoding").getValue("ISO-8859-2");
		affReader = config.getChild("aff").getChild("reader").getValue("net.sf.jhunlang.jmorph.sword.parser.SwordAffixReader");
		
		
		  try {
	            affixDef = ac.createDefinition(
	                     getRealPath(affFile), affEncoding, Class.forName(affReader));
	            logger.info("aff definition: " + affixDef.toString());
	        }catch (ClassNotFoundException e) {
	            logger.error("aff/reader class not found: " + affReader);
	            throw new ConfigurationException("aff/reader class not found" + affReader, e);
	        } catch (Exception e) {
	            throw new ConfigurationException("can't create definition for aff",e);
	        } 
	        
	        int recursionDepth = config.getChild("recursion-depth").getValueAsInteger(4);
	        logger.info("recursion depth:" + 	recursionDepth);
	        
	        ac.setRecursionDepth(affixDef, recursionDepth);
	        
	        String derivatives = config.getChild("derivatives").getValue(null);
	        if(derivatives != null) {
	            	ac.setDerivativeFile(affixDef, getRealPath(derivatives));
	            	logger.info("derivates files:" + getRealPath(derivatives));
	        } else {
	             logger.info("no derivates");
	        }
	        
	        
		dicFile = config.getChild("dic").getChild("file").getValue();
		dicEncoding = config.getChild("dic").getChild("encoding").getValue("ISO-8859-2");
		dicReader = config.getChild("dic").getChild("reader").getValue("net.sf.jhunlang.jmorph.sword.parser.SwordReader");

    	
    	    // ac.setCompoundFile(affixDef, "compound.lst");
        
        try {
            dicDef = ac.createDefinition(
                     getRealPath(dicFile), dicEncoding, Class.forName(dicReader));
            logger.info("dic definition: " + dicDef.toString());
        }catch (ClassNotFoundException e) {
            logger.error("dic/reader class not found: " + affReader);
            throw new ConfigurationException("dic/reader class not found" + affReader, e);
        } catch (Exception e) {
            throw new ConfigurationException("can't create definition for dic",e);
        } 
  	    
	}
	
	public String getRealPath(String path) {
	    if(path.startsWith("/")) {
	        return path;
	    }
	    
	    return contextDirectory.getAbsolutePath() + "/" + path;
	    
	}

	public void initialize() throws Exception {
	    
		logger.debug("start to load jmorph");
	
		 JMorphFactory jf = new JMorphFactory();
		 m_worker = jf.build(new Definition[] {affixDef, dicDef});
		 logger.debug("jmorph loaded");
		
	}


	///////////////////////////////////
	/// Delegated methods
	/////////////////////

	  public List analyse(String word) {
	      return m_worker.analyse(word);
	    }


	    public List analyse(String word, AnalyserContext context) {
	        return m_worker.analyse(word, context);
	    }

 
    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    public int hashCode() {
        return m_worker.hashCode();
    }
    /**
     * @param arg0
     * @param arg1
     * @param arg2
     * @return
     */
    public IStem istem(String arg0, int arg1, AnalyserContext arg2) {
        return m_worker.istem(arg0, arg1, arg2);
    }
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return m_worker.toString();
    }
    /**
     * @param arg0
     * @param arg1
     * @return
     */
    public IStem istem(String arg0, int arg1) {
        return m_worker.istem(arg0, arg1);
    }
    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object arg0) {
        return m_worker.equals(arg0);
    }
    /**
     * @param arg0
     * @param arg1
     * @param arg2
     * @param arg3
     * @return
     */
    public boolean subanalyse(String arg0, String arg1, AnalyserContext arg2,
            AnalysisConsumer arg3) {
        return m_worker.subanalyse(arg0, arg1, arg2, arg3);
    }
    /**
     * @return
     */
    public Rules getRules() {
        return m_worker.getRules();
    }
  
    /**
     * @return
     */
    public Dictionaries getDictionaries() {
        return m_worker.getDictionaries();
    }
/*
	public Definition getAffixDef() {
		return affixDef;
	}

	public void setAffixDef(Definition affixDef) {
		this.affixDef = affixDef;
	}

	public Definition getDicDef() {
		return dicDef;
	}

	public void setDicDef(Definition dicDef) {
		this.dicDef = dicDef;
	}
*/
}

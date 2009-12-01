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
import net.sf.jhunlang.jmorph.analysis.AnalyserControl;
import net.sf.jhunlang.jmorph.analysis.IStem;
import net.sf.jhunlang.jmorph.analysis.consumer.AnalysisConsumer;
import net.sf.jhunlang.jmorph.factory.Definition;
import net.sf.jhunlang.jmorph.factory.JMorphFactory;
import net.sf.jhunlang.jmorph.lemma.Lemma;
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
 * @avalon.service type=Lemmatizer
 * @x-avalon.info name=jmorph-lemmatizer
 * @x-avalon.lifestyle type="singleton"
 */

/*
 * Simple avalon-aware wrapper component around jmorph Lemmatizer that
 * reads the configuration from like any other component. It delegates
 * every method to an LemmatizerImpl object created by jmorph's factory.
 */
public class LemmatizerImpl 	implements 

	Lemmatizer,
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
	 * Whether the tokenfilter should return OOV words 
	 */
	private boolean returnOOVs;
	
	/**
	 * Must the lemmatizer strip derivates
	 */
	private boolean stripDerivates;
	
	/**
	 * The depth the analyser uses
	 */
	private int depth;
	
	/**
	 * The jmorph.stemmer that the work is delegated
	 */
	private net.sf.jhunlang.jmorph.lemma.Lemmatizer  m_worker;
	
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
        
        stripDerivates = config.getChild("strip-derivates").getValueAsBoolean(false);
        returnOOVs = config.getChild("index-oovs-as-stem").getValueAsBoolean(false);

    	
     
        String sdepth = config.getChild("depth").getValue("");
        depth = 0;
	    
	    for(int i = 0; i< AnalyserControl.DEPTHS.length; i ++)
	    {
	        if(AnalyserControl.DEPTHS[i].compareTo(sdepth) == 0)
	        {
	            depth = i;
	        }
	    }
	    
	    logger.info("lemmatizer config:  stripderivates, oov, depth " + 
	            stripDerivates + " " + returnOOVs + " " + AnalyserControl.DEPTHS[depth]);
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
		 net.sf.jhunlang.jmorph.analysis.Analyser  jmorph  = jf.build(new Definition[] {affixDef, dicDef});
		 
		 AnalyserControl ac = new AnalyserControl(depth);
		 AnalyserContext analyserContext  = new AnalyserContext(ac);
		    
		 m_worker = new net.sf.jhunlang.jmorph.lemma.LemmatizerImpl(jmorph, stripDerivates, analyserContext);
		 
		 logger.debug("jmorph loaded");
		
	}


	///////////////////////////////////
	/// Delegated methods
	/////////////////////

	
    /**
     * @param arg0
     * @return
     */
    public List lemmatize(String arg0) {
        List lemmas = m_worker.lemmatize(arg0);
        
        if(lemmas.size() == 0 && returnOOVs) {
            lemmas.add(new Lemma(arg0, "unknown"));
        } 
        
        return lemmas;
    }

    /* (non-Javadoc)
     * @see net.sf.jhunlang.jmorph.lemma.Lemmatizer#setStripDerivates(boolean)
     */
    public void setStripDerivates(boolean arg0) {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see net.sf.jhunlang.jmorph.lemma.Lemmatizer#setAnalyserContext(net.sf.jhunlang.jmorph.analysis.AnalyserContext)
     */
    public void setAnalyserContext(AnalyserContext arg0) {
        // TODO Auto-generated method stub
        
    }
}

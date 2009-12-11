/*
 * Created on Jan 2, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package mokk.nlp.bicorpus.index.lucene;



import java.util.HashMap;
import java.util.Map;

import mokk.nlp.irutil.lucene.analysis.AnalyzerFactory;
import mokk.nlp.irutil.lucene.analysis.LemmatizerWrapper;
import mokk.nlp.irutil.lucene.analysis.StemmerAnalyzer;
import mokk.nlp.jmorph.Analyser;
import net.sf.jhunlang.jmorph.analysis.AnalyserContext;
import net.sf.jhunlang.jmorph.analysis.AnalyserControl;
import net.sf.jhunlang.jmorph.lemma.Lemmatizer;

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
import org.apache.lucene.util.Version;

/**
 * @avalon.component
 * @avalon.service type=mokk.nlp.irutil.lucene.analysis.AnalyzerFactory
 * @x-avalon.info name=bis-analyzer
 * @x-avalon.lifestyle type="singleton"
 */
public class BiCorpusAnalyzerFactory 
	implements AnalyzerFactory, Component, Disposable, LogEnabled, Configurable, Initializable, Serviceable {
	
	private ServiceManager manager;
	
	
	private Logger logger;
	
	/**
	 * The left lemmatizer
	 */
	private Lemmatizer leftLemmatizer;
	/**
	 * The jmorph analyser that used be the left lemmatizer
	 */
	private String leftAnalyserId;
	
	
	/**
	 * Whether the tokenfilter should return original word 
	 */
	private boolean returnOrig = false;
	
	/**
	 * Whether the tokenfilter should return OOV words 
	 */
	private boolean leftReturnOOVs;
	/**
	 * Must the tokenfilter append the POS to the term.
	 */
	private boolean leftReturnPOS;
	/**
	 * Must the lemmatizer strip derivates
	 */
	private boolean leftStripDerivates;
	
	/**
	 * The depth the analyser uses
	 */
	private int leftDepth;
	
	/**
	 * The analyser component used be the left lemmatization
	 */
	private Analyser leftAnalyser;
	
	/**
	 * The jmorph analyser that used be the left lemmatizer
	 */
	private String rightAnalyserId;
	/**
	 * Whether the tokenfilter should return OOV words 
	 */
	private boolean rightReturnOOVs;
	/**
	 * Must the tokenfilter append the POS to the term.
	 */
	private boolean rightReturnPOS;
	/**
	 * Must the lemmatizer strip derivates
	 */
	private boolean rightStripDerivates;
	
	/**
	 * The depth the analyser uses
	 */
	private int rightDepth;
	
	/**
	 * The analyser component used be the left lemmatization
	 */
	private Analyser rightAnalyser;
	/**
	 * The right lemmatizer
	 */
	private Lemmatizer rightLemmatizer;
	
	private Map<String, LemmatizerWrapper> lemmatizerMap;

	private boolean leftReturnOrig, rightReturnOrig;
	
	public void enableLogging(Logger logger) {
		this.logger=logger;
		
	}
	
	public void configure(Configuration config) throws ConfigurationException {
	    
	    Configuration leftLemmaConfig = config.getChild("left").getChild("lemmatization", false);
    	returnOrig = config.getChild("return-orig").getValueAsBoolean(true);
	    //logger.debug(">>> BiCorpusAnalyzerFactory returnOrig="+Boolean.toString(returnOrig));
	    if (leftLemmaConfig != null) {
	        leftAnalyserId = leftLemmaConfig.getChild("morph-analyzer").getValue();
	        leftStripDerivates = leftLemmaConfig.getChild("strip-derivates").getValueAsBoolean(false);
	        leftReturnOOVs = leftLemmaConfig.getChild("index-oovs-as-stem").getValueAsBoolean(false);
	        leftReturnPOS = leftLemmaConfig.getChild("append-pos").getValueAsBoolean(false);
	     
	        String depth = leftLemmaConfig.getChild("depth").getValue("");
	        leftDepth = 0;
		    
		    for(int i = 0; i< AnalyserControl.DEPTHS.length; i ++)
		    {
		        if(AnalyserControl.DEPTHS[i].compareTo(depth) == 0)
		        {
		            leftDepth = i;
		        }
		    }
		    
		    logger.info("left lemmatization set: id, stripderivates, oov, pos, depth " + 
		            leftAnalyserId + " " + leftStripDerivates + " " + leftReturnOOVs + " " + leftReturnPOS + " " + AnalyserControl.DEPTHS[leftDepth]);
	    }
	    
	    Configuration rightLemmaConfig = config.getChild("right").getChild("lemmatization", false);
	    if (rightLemmaConfig != null) {
	        rightAnalyserId = rightLemmaConfig.getChild("morph-analyzer").getValue();
	        rightStripDerivates = rightLemmaConfig.getChild("strip-derivates").getValueAsBoolean(false);
	        rightReturnOOVs = rightLemmaConfig.getChild("index-oovs-as-stem").getValueAsBoolean(false);
	        rightReturnPOS = rightLemmaConfig.getChild("append-pos").getValueAsBoolean(false);
	    	
	     
	        String depth = rightLemmaConfig.getChild("depth").getValue();
	        rightDepth = 0;
		    
		    for(int i = 0; i< AnalyserControl.DEPTHS.length; i ++)
		    {
		        if(AnalyserControl.DEPTHS[i].compareTo(depth) == 0)
		        {
		            rightDepth = i;
		        }
		    }
		    logger.info("right lemmatization set: id, stripderivates, oov, pos, depth " + 
		            rightAnalyserId + " " + rightStripDerivates + " " + rightReturnOOVs + " " + rightReturnPOS + " " + AnalyserControl.DEPTHS[rightDepth]);
	   
	    }
	  
		
	}

	
	/* (non-Javadoc)
	 * @see org.apache.avalon.framework.activity.Initializable#initialize()
	 */
	public void initialize() throws Exception {
		
		if(leftAnalyserId != null) {
		    leftAnalyser = (Analyser) manager.lookup(Analyser.ROLE + "/" + leftAnalyserId);
		    AnalyserControl ac = new AnalyserControl(leftDepth);
		    AnalyserContext analyserContext  = new AnalyserContext(ac);
		    
		    leftLemmatizer = new net.sf.jhunlang.jmorph.lemma.LemmatizerImpl(leftAnalyser, leftStripDerivates, analyserContext);
		 
		} else {
		    leftLemmatizer = null;
			logger.info("using no left lemmatizer");
		}
		
		if(rightAnalyserId != null) {
		    rightAnalyser = (Analyser) manager.lookup(Analyser.ROLE + "/" + rightAnalyserId);
		    AnalyserControl ac = new AnalyserControl(rightDepth);
		    AnalyserContext analyserContext  = new AnalyserContext(ac);
		    
		    rightLemmatizer = new net.sf.jhunlang.jmorph.lemma.LemmatizerImpl(leftAnalyser, rightStripDerivates, analyserContext);
		 
		} else {
		    rightLemmatizer = null;
			logger.info("using no right lemmatizer");
		}
		
		lemmatizerMap = new HashMap<String, LemmatizerWrapper>();
		LemmatizerWrapper leftLemmatizerWrapper = new LemmatizerWrapper();
		leftLemmatizerWrapper.setLemmatizer(leftLemmatizer);
		leftLemmatizerWrapper.setReturnOOVOrig(leftReturnOOVs);
		leftLemmatizerWrapper.setReturnOrig(leftReturnOrig);
		leftLemmatizerWrapper.setReturnPOS(leftReturnPOS);
		lemmatizerMap.put(BisMapper.leftStemmedFieldName, leftLemmatizerWrapper);

		LemmatizerWrapper rightLemmatizerWrapper = new LemmatizerWrapper();
		rightLemmatizerWrapper.setLemmatizer(rightLemmatizer);
		rightLemmatizerWrapper.setReturnOOVOrig(rightReturnOOVs);
		rightLemmatizerWrapper.setReturnOrig(rightReturnOrig);
		rightLemmatizerWrapper.setReturnPOS(rightReturnPOS);
		lemmatizerMap.put(BisMapper.rightStemmedFieldName, rightLemmatizerWrapper);
		
	}
	
	/**
	 * @avalon.dependency type="mokk.nlp.jmorph.Lemmatizer"
	 */
	public void service(ServiceManager manager) throws ServiceException {
		this.manager = manager;
	}
	
	
    public void dispose() {
      if (leftAnalyser != null)
      { 
          manager.release(leftAnalyser);
      }
      if (rightAnalyser != null)
      { 
          manager.release(rightAnalyser);
      }
    }
	 
	

	/**
	 * TODO is it necessary to always create a new instance of this?
	 */
	public Analyzer getAnalyzer() {
		Analyzer result = new StemmerAnalyzer(Version.LUCENE_CURRENT, lemmatizerMap);
		return result;
	}

	/*
	 * getters and setters
	 */
	
	public Lemmatizer getLeftLemmatizer() {
		return leftLemmatizer;
	}

	public void setLeftLemmatizer(Lemmatizer leftLemmatizer) {
		this.leftLemmatizer = leftLemmatizer;
	}

	public String getLeftAnalyserId() {
		return leftAnalyserId;
	}

	public void setLeftAnalyserId(String leftAnalyserId) {
		this.leftAnalyserId = leftAnalyserId;
	}

	public boolean isReturnOrig() {
		return returnOrig;
	}

	public void setReturnOrig(boolean returnOrig) {
		this.returnOrig = returnOrig;
	}

	public boolean isLeftReturnOOVs() {
		return leftReturnOOVs;
	}

	public void setLeftReturnOOVs(boolean leftReturnOOVs) {
		this.leftReturnOOVs = leftReturnOOVs;
	}

	public boolean isLeftReturnPOS() {
		return leftReturnPOS;
	}

	public void setLeftReturnPOS(boolean leftReturnPOS) {
		this.leftReturnPOS = leftReturnPOS;
	}

	public boolean isLeftStripDerivates() {
		return leftStripDerivates;
	}

	public void setLeftStripDerivates(boolean leftStripDerivates) {
		this.leftStripDerivates = leftStripDerivates;
	}

	public int getLeftDepth() {
		return leftDepth;
	}

	public void setLeftDepth(int leftDepth) {
		this.leftDepth = leftDepth;
	}

	public Analyser getLeftAnalyser() {
		return leftAnalyser;
	}

	public void setLeftAnalyser(Analyser leftAnalyser) {
		this.leftAnalyser = leftAnalyser;
	}

	public String getRightAnalyserId() {
		return rightAnalyserId;
	}

	public void setRightAnalyserId(String rightAnalyserId) {
		this.rightAnalyserId = rightAnalyserId;
	}

	public boolean isRightReturnOOVs() {
		return rightReturnOOVs;
	}

	public void setRightReturnOOVs(boolean rightReturnOOVs) {
		this.rightReturnOOVs = rightReturnOOVs;
	}

	public boolean isRightReturnPOS() {
		return rightReturnPOS;
	}

	public void setRightReturnPOS(boolean rightReturnPOS) {
		this.rightReturnPOS = rightReturnPOS;
	}

	public boolean isRightStripDerivates() {
		return rightStripDerivates;
	}

	public void setRightStripDerivates(boolean rightStripDerivates) {
		this.rightStripDerivates = rightStripDerivates;
	}

	public int getRightDepth() {
		return rightDepth;
	}

	public void setRightDepth(int rightDepth) {
		this.rightDepth = rightDepth;
	}

	public Analyser getRightAnalyser() {
		return rightAnalyser;
	}

	public void setRightAnalyser(Analyser rightAnalyser) {
		this.rightAnalyser = rightAnalyser;
	}

	public Lemmatizer getRightLemmatizer() {
		return rightLemmatizer;
	}

	public void setRightLemmatizer(Lemmatizer rightLemmatizer) {
		this.rightLemmatizer = rightLemmatizer;
	}

	public boolean isLeftReturnOrig() {
		return leftReturnOrig;
	}

	public void setLeftReturnOrig(boolean leftReturnOrig) {
		this.leftReturnOrig = leftReturnOrig;
	}

	public boolean isRightReturnOrig() {
		return rightReturnOrig;
	}

	public void setRightReturnOrig(boolean rightReturnOrig) {
		this.rightReturnOrig = rightReturnOrig;
	}
   


	
}

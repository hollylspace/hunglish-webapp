package mokk.nlp.ocalemmatizer;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import mokk.nlp.ocamorph.Compounds;
import mokk.nlp.ocamorph.Guess;
import mokk.nlp.ocamorph.OcamorphStemmer;
import mokk.nlp.ocamorph.OcamorphWrapper;

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

//do not reformat the next lines (two ** at the begining)
/**
 * @author bpgergo
 * @avalon.component
 * @avalon.service type=Lemmatizer
 * @x-avalon.info name=ocamorph-lemmatizer
 * @x-avalon.lifestyle type="singleton"
 */

/*
 * Simple avalon-aware wrapper component around Ocamorph Lemmatizer that reads
 * the configuration like any other component. It delegates every method to an
 * OcamorphCachedStemmer.
 */
public class OcamorphLemmatizerImpl implements Component, LogEnabled,
		Configurable, Initializable, Contextualizable {

	public File contextDirectory = null;
	private Logger logger;
	private String ocamorphResource;
	private boolean blocking;
	private boolean stopAtFirst;
	OcamorphStemmer worker;
	String ocamorphCache;

	public List lemmatize(String word) {
		return new LinkedList<String>(worker.getStems(word));
	}

	// Avalon stuff
	// @Override
	public void enableLogging(Logger logger) {
		this.logger = logger;
	}

	// @Override
	public void configure(Configuration config) throws ConfigurationException {
		ocamorphResource = config.getChild("resource").getChild("file")
				.getValue();
		blocking = config.getChild("blocking").getValueAsBoolean(true);
		stopAtFirst = config.getChild("stop-at-first").getValueAsBoolean(false);
		ocamorphCache = config.getChild("cache").getChild("file").getValue();

	}

	// @Override
	public void initialize() throws Exception {
		OcamorphWrapper ocamorphWrapper = new OcamorphWrapper(ocamorphResource,
				blocking, stopAtFirst, Compounds.No, Guess.NoGuess);

		worker = new OcamorphStemmer(ocamorphWrapper);

		if (ocamorphCache != null) {
			//worker = new OcamorphCachedStemmer(ocamorphCache, ocamorphWrapper
			//		.getEncoding(), worker);
		}
	}

	// @Override
	public void contextualize(Context context) throws ContextException {
		contextDirectory = (File) context
				.get(ContextManagerConstants.CONTEXT_DIRECTORY);
		logger.info("context directory:" + contextDirectory);
	}

}

/*
 * Created by Peter Halacsy <peter at halacsy.com>
 * 
 * This work is licensed under the Creative Commons 
 * Attribution License. To view a copy of this license, 
 * visit http://creativecommons.org/licenses/by/2.0/ 
 * or send a letter to Creative Commons, 559 Nathan Abbott Way, 
 * Stanford, California 94305, USA.
 * 
 * Created on Mar 28, 2005
 *
 */

package mokk.nlp.bicorpus.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.util.zip.GZIPInputStream;

import mokk.nlp.bicorpus.BiSentence;
import mokk.nlp.bicorpus.Source;
import mokk.nlp.irutil.io.DocumentHandler;
import mokk.nlp.irutil.io.FileDocumentSource;
import mokk.nlp.irutil.io.ProcessingException;

import org.apache.avalon.framework.component.Component;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.logger.LogEnabled;
import org.apache.avalon.framework.logger.Logger;

/**
 * @avalon.component
 * @avalon.service type=mokk.nlp.irutil.io.FileDocumentSource
 * @x-avalon.info name=alignfile-parser
 * @x-avalon.lifestyle type="singleton"
 */
public class AlignFileParser implements FileDocumentSource, Component,
		LogEnabled, Configurable {

	private Logger logger;
	private DocumentHandler m_handler = null;

	private String encoding;

	private static Source source = new Source("na", "na", null);
	/*
	 * Current file to parse.
	 */
	private File m_inputFile;

	public void enableLogging(Logger logger) {
		this.logger = logger;
	}

	public void configure(Configuration config) throws ConfigurationException {
		encoding = (String) config.getChild("encoding").getValue("ISO-8859-2");

	}

	public void setHandler(DocumentHandler handler) {
		m_handler = handler;

	}

	public DocumentHandler getHandler() {
		// TODO Auto-generated method stub
		return m_handler;
	}

	public void read() throws ProcessingException, IOException {
		if (m_inputFile == null) {
			throw new ProcessingException("no input file set");
		}

		read(m_inputFile);
	}

	public void setInputFile(File f) {
		m_inputFile = f;
	}

	public void read(File inputFile) throws ProcessingException, IOException {
		InputStream inputStream = new FileInputStream(inputFile);

		if (inputFile.getName().endsWith(".gz")) {
			inputStream = new GZIPInputStream(inputStream);
		}

		LineNumberReader reader = new LineNumberReader(new InputStreamReader(
				inputStream, encoding));

		String line = null;

		while ((line = reader.readLine()) != null) {

			int lineNumber = reader.getLineNumber();
			/*
			 * int i = line.indexOf("\t"); if(i < 0) { throw new
			 * ProcessingException("no tab found in the line:" + lineNumber); }
			 * 
			 * 
			 * String left = line.substring(0, i); String right =
			 * line.substring(i+1);
			 * 
			 * BiSentence bis = new BiSentence(source, "" + lineNumber , left,
			 * right);
			 */
			BiSentence bis = getBisentence(line, lineNumber);
			if (bis != null){
				m_handler.processDocument(bis);
			}
		}
		reader.close();
	}

	private BiSentence getBisentence(String line, int lineNumber) throws ProcessingException {
		BiSentence bis = null;
		try {
			String[] fields = line.split("\\t");
			String left = fields[0];
			String right = fields[1];
			bis = new BiSentence(source, "" + lineNumber, left,
					right);
		} catch (Exception e) {
			bis = null;
			//throw new ProcessingException(e.getMessage() + ";line number:"
			//		+ lineNumber);
			logger.warn("cant parse line, source:"+source+"; line:\n"+line);
			e.printStackTrace();
		}
		return bis;
	}

}

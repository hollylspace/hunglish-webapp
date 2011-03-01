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
import java.io.InputStreamReader;
import java.io.LineNumberReader;

import mokk.nlp.bicorpus.BiSentence;
import mokk.nlp.bicorpus.Source;
import mokk.nlp.irutil.io.DocumentHandler;
import mokk.nlp.irutil.io.FileDocumentSource;
import mokk.nlp.irutil.io.ProcessingException;

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

/**
 * @avalon.component
 * @avalon.service type=mokk.nlp.irutil.io.FileDocumentSource
 * @avalon.service type=mokk.nlp.irutil.io.DocumentSource
 * @x-avalon.info name=dictfile-parser
 * @x-avalon.lifestyle type="singleton"
 */
public class HunglishDictFileParser implements 
FileDocumentSource, Component, LogEnabled, Configurable, Initializable, Contextualizable {
  
    
    private Logger m_logger;
    private DocumentHandler m_handler = null;
  
    private String m_encoding;
    private static Source source = new Source("na", "na", null);
    /*
     * Current file to parse.
     */
    private File m_inputFile;
    
    /*
     * The name of the file to parse. It set at configuration
     */
    private String m_inputFileName;
     
    /*
     * The root directory of the context
     */
    private File m_contextDirectory;
    
    /*
     * The sourceId of the dictionary. It's set at configuration  
     */
    private String m_sourceId;
    
    
     public void enableLogging(Logger logger) {
         this.m_logger = logger;
     }
     
  
     public void configure(Configuration config) throws ConfigurationException {
         m_inputFileName = (String) config.getChild("path").getValue();
         m_logger.info("reading file: " + m_inputFileName);
         
         m_encoding = (String) config.getChild("encoding").getValue("ISO-8859-2");
         m_logger.info("using encoding: " + m_encoding);
         
        
         
         m_sourceId = (String) config.getChild("source-id").getValue("nosource");
         m_logger.info("setting sourceId: " + m_sourceId);
         
     }	
    
 	public void contextualize(Context context) throws ContextException {
	     m_contextDirectory = (File) context.get(ContextManagerConstants.CONTEXT_DIRECTORY);
	     m_logger.info("context directory:" + m_contextDirectory );

	}
 	
	private String getContextualizedPath(String file) {
	    if (file.startsWith("/")) {
	        return file;
	    } 
	    
	    return m_contextDirectory.getAbsolutePath() + "/" + file;
	}
	
	public void initialize() throws Exception {
		
		m_inputFile = new File(getContextualizedPath(m_inputFileName));
	}
	
	
     public void setHandler(DocumentHandler handler) {
         m_handler = handler;
       
     } 
     
     public DocumentHandler getHandler() {
         // TODO Auto-generated method stub
         return m_handler;
     }

     public void read() throws ProcessingException, IOException  {
         if(m_inputFile == null) {
             throw new ProcessingException("no input file set");
         }
         
         read(m_inputFile);
     }
     
     public void setInputFile(File f) {
         m_inputFile = f;
     }
     
     public void read(File inputFile) throws ProcessingException, IOException {
	
         LineNumberReader reader;
     
            reader = new LineNumberReader(
                					new InputStreamReader (
                					        new FileInputStream(inputFile), m_encoding));
        
        String line = null;
		
		while((line = reader.readLine()) != null ){
		//m_logger.debug(line);
		    int i = line.indexOf("@");
		    if(i < 0) {
		        throw new ProcessingException("no @ found in the line:" + reader.getLineNumber());
		    }
		
		    String left = line.substring(0, i);
		    String right = line.substring(i+1);
		    // a mostani vonyo forditva van!!!!
		    BiSentence bis = new BiSentence(source, "" + reader.getLineNumber()  , right, left);
		   
		    m_handler.processDocument(bis);
		    
		    if(reader.getLineNumber() % 2000 == 0) {
		        m_logger.debug("indexed lines: " + reader.getLineNumber());
		      //  break;
		    }
		}
		reader.close();
     }

 
}



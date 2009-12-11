/*
 * Created by Peter Halacsy <peter at halacsy.com>
 * 
 * This work is licensed under the Creative Commons 
 * Attribution License. To view a copy of this license, 
 * visit http://creativecommons.org/licenses/by/2.0/ 
 * or send a letter to Creative Commons, 559 Nathan Abbott Way, 
 * Stanford, California 94305, USA.
 * 
 * Created on Jun 11, 2005
 *
 */

package mokk.nlp.bicorpus;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.util.Collection;
import java.util.HashMap;

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
 * @author hp
 *
 * 
 * @avalon.component
 * @avalon.service type=SourceDB
 * @x-avalon.info name=sourcedb
 * @x-avalon.lifestyle type=singleton
 */
public class FileBasedSourceDB implements SourceDB, Component, 
			LogEnabled, 
			Configurable, 
			Initializable,Contextualizable {

    private Logger logger;
    
    private File contextDirectory;
    
    private String fileName;
    
    private String encoding;
    
 
    private HashMap sourceMap = null;
 
    /* (non-Javadoc)
     * @see mokk.nlp.bicorpus.SourceDB#get(java.lang.String)
     */
    public Source get(String id) {
        if(id == null) {
            return null;
        }
        return (Source) sourceMap.get(id);
    }
  
    public Collection getKnownSources() {
        return sourceMap.values();
    }
    
    public void load() throws IOException {
        if(sourceMap == null) {
            sourceMap = new HashMap();
        }
        LineNumberReader in = new LineNumberReader(new InputStreamReader (new FileInputStream(fileName), encoding));
        String line = null;
        int depth = 0;
        
        while((line = in.readLine()) != null) {
            if(line.startsWith("#")) {
                continue;
            }
            String[] fields = line.split("\t");
            if (fields.length < 2) {
                logger.error("syntax error in: " + fileName + " " + in.getLineNumber());
                continue;
            }
            
            Source parent = null;
            
            String absoluteId = fields[0];
            String title = fields[1];
            int ix = absoluteId.lastIndexOf("/");
            if (ix > 0) {
                String parentId = absoluteId.substring(0, ix);
                parent = (Source) sourceMap.get(parentId);
                if(parent == null) {
                    logger.error("found source with unknown parent (is the source file sorted?) " + absoluteId );
                }
            }
            Source source = new Source(absoluteId, true, title, parent);
            sourceMap.put(absoluteId, source);
        }

    }


    public void enableLogging(Logger logger) {
        this.logger = logger;

    }

    public void configure(Configuration config) throws ConfigurationException {
        fileName = config.getChild("file").getValue();
        encoding = config.getChild("encoding").getValue("ISO-8859-2");

    }
    
    public void initialize() throws Exception {
        fileName = getContextualizedPath(fileName);
      load();
      logger.info("the sourcedb loaded with " + sourceMap.size() + " sources");
    }
    
    public void contextualize(Context context) throws ContextException {
        contextDirectory = (File) context.get(ContextManagerConstants.CONTEXT_DIRECTORY);

    }
    
	private String getContextualizedPath(String file) {
	    if (file.startsWith("/")) {
	        return file;
	    } 
	    
	    return contextDirectory.getAbsolutePath() + "/" + file;
	}
}

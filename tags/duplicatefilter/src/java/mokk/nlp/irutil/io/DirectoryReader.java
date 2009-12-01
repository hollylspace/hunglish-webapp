/*
 * Created by Peter Halacsy <peter at halacsy.com>
 * 
 * This work is licensed under the Creative Commons 
 * Attribution License. To view a copy of this license, 
 * visit http://creativecommons.org/licenses/by/2.0/ 
 * or send a letter to Creative Commons, 559 Nathan Abbott Way, 
 * Stanford, California 94305, USA.
 * 
 * Created on Mar 23, 2005
 *
 */

package mokk.nlp.irutil.io;


import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;

import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.activity.Initializable;
import org.apache.avalon.framework.component.Component;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.logger.LogEnabled;
import org.apache.avalon.framework.logger.Logger;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;


/**
 * @avalon.component
 * @avalon.service type=mokk.nlp.irutil.io.DocumentSource
 * @x-avalon.info name=dir-source
 * @x-avalon.lifestyle type="singleton"
 */
public class DirectoryReader
implements DocumentSource, Component, 
LogEnabled, Configurable, 
Initializable, Disposable, Serviceable {

    /* Files to process */
    private File[] files;
    
    protected ServiceManager manager;
    
    private Logger logger;
    private String baseDir;
    private String fileSourceId;
    private String pattern;
    
    private FileDocumentSource fileSource;
    
    public void enableLogging(Logger logger) {
        this.logger = logger;
    }
    

    


     public DocumentHandler getHandler() {
         return    fileSource.getHandler();
     }
     
     public void setHandler(DocumentHandler handler) {
         
         fileSource.setHandler(handler);

     }
    /*
     * A directorybol kiolvassa a fajlneveket. 
     */
    public void initialize() throws Exception {
        File directory = new File(baseDir);
        files = directory.listFiles(
                new FilenameFilter() {
                    public boolean accept(File dir,  String name) {
                        return name.matches(pattern);
                    }
                }
        		 );
        if(files == null) {
            throw new Exception("no files found at " + directory.getAbsolutePath());
        }
        
        logger.info("number of files read " + files.length);
        
        fileSource = (FileDocumentSource)  manager.lookup(FileDocumentSource.ROLE + "/" + fileSourceId);
    }
    
    public void configure(Configuration config) throws ConfigurationException {
        baseDir = config.getChild("directory").getValue();
        if(baseDir == null) {
            throw new ConfigurationException("no dir specified");
        }
        logger.info("seeking files in: " + baseDir);
        
        fileSourceId = config.getChild("file-reader").getValue();
        logger.info("using file parser: " + fileSourceId);
        
        pattern = config.getChild("pattern").getValue(".*");
        logger.info("using pattern: " + pattern);
        
    }
    
    /**
     * @avalon.dependency type="mokk.nlp.irutil.io.FileDocumentSource"
     */
    public void service(ServiceManager manager) throws ServiceException {
        this.manager = manager;

    }
  
   
    public void contextualize(Context context) throws ContextException {
        logger.warn("no contextualization implemented");

    }
    
    
  
    public void read() throws ProcessingException, IOException{
      for(int i = 0; i< files.length; i++) {
          logger.info("processing the +" + i + ". file:" + files[i]);
          fileSource.read(files[i]);
      }

    }
 

    
    public void dispose() {
       if(manager != null && fileSource != null) {
           manager.release(fileSource);
       }
        
    }
}

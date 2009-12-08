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

package mokk.nlp.bicorpus.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import mokk.nlp.bicorpus.*;
import mokk.nlp.irutil.Document;
import mokk.nlp.irutil.io.*;

import org.apache.avalon.fortress.util.ContextManagerConstants;
import org.apache.avalon.framework.activity.Disposable;
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
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;

/**
 * @avalon.component
 * @avalon.service type=mokk.nlp.irutil.io.DocumentSource
 * @x-avalon.info name=catalog-source
 * @x-avalon.lifestyle type="singleton"
 */
public class CatalogReader implements DocumentSource, Component, LogEnabled,
        Configurable, Initializable, Disposable, Serviceable, Contextualizable {

    protected ServiceManager manager;

    private Logger logger;

    private String fileSourceId;

    protected DocumentHandler handler;

    private File contextDirectory;

    private List collections;
    
    public void enableLogging(Logger logger) {
        this.logger = logger;
    }


    
    public DocumentHandler getHandler() {
        return handler;
    }

    public void setHandler(DocumentHandler handler) {

        this.handler = handler;

    }

    /*
     * A directorybol kiolvassa a fajlneveket.
     */
    public void initialize() throws Exception {

    }



    public void configure(Configuration config) throws ConfigurationException {

        fileSourceId = config.getChild("file-reader").getValue();
        logger.info("using file parser: " + fileSourceId);

        Configuration[] colConfigs = config.getChildren("collection");
        
        collections = new LinkedList();
        for(int i = 0; i < colConfigs.length; i++) {
            CollectionConfig cc = new CollectionConfig();
            cc.id = colConfigs[i].getChild("id").getValue();
            cc.title = colConfigs[i].getChild("title").getValue();
            cc.catalogFile = colConfigs[i].getChild("catalog-file").getValue();
            cc.corpusHome = colConfigs[i].getChild("data-home").getValue(".");
            cc.fileSuffix =  colConfigs[i].getChild("file-suffix").getValue("");
            collections.add(cc);
        }
        if(collections.size() == 0) {
            throw new ConfigurationException("no collection found");
        }
        
        logger.info("processing " + collections.size() + " collection");
    }

    /**
     * @avalon.dependency type="mokk.nlp.irutil.io.FileDocumentSource"
     */
    public void service(ServiceManager manager) throws ServiceException {
        this.manager = manager;

    }

    public void contextualize(Context context) throws ContextException {
        contextDirectory = (File) context
                .get(ContextManagerConstants.CONTEXT_DIRECTORY);
        logger.info("context directory:" + contextDirectory);

    }

    private String getContextualizedPath(String file) {
        if (file.startsWith("/")) {
            return file;
        }

        return contextDirectory.getAbsolutePath() + "/" + file;
    }

    protected void readCatalog(CollectionConfig cc)
            throws ProcessingException, IOException, ServiceException {
        
        
        FileDocumentSource fileParser = (FileDocumentSource) manager
                .lookup(FileDocumentSource.ROLE + "/" + fileSourceId);

        Source corpusSource = new Source(cc.id, cc.title, null);

        LineNumberReader catalog = openCatalog(cc.catalogFile);

        logger.info("catalog opened " + cc.catalogFile);
        String line = null;
        // int i = 1;
        try {
            while ((line = catalog.readLine()) != null) {
                // if(--i < 0) break;
                String[] fields = line.split("\t");
                if (fields.length < 2) {
                    throw new ProcessingException("can't parse " + cc.catalogFile
                            + " " + catalog.getLineNumber());
                }

                String fileName = fields[0];
                String fileTitle = fields[1];

                Source biSource = null;
                
                // Arthur_C_Clarke_1
                int ix = fileName.lastIndexOf("_");
                if (ix < 0) {
                    biSource =  new Source(fileName, fileTitle, corpusSource);
                } else {
                
       
                    String authorId = fileName.substring(0, ix);
                    String fileId = fileName.substring(ix + 1);

                    Source authorSource = new Source(authorId, authorId, corpusSource);
                    biSource = new Source(fileId, fileTitle, authorSource);
                }

                fileParser.setHandler(new SourceSetterDocumentHandler(
                        biSource, handler));

                fileName = getContextualizedPath(cc.corpusHome) + "/" + fileName
                        + cc.fileSuffix;
                logger.info("parsing file: " + fileName + " source "
                        + biSource.getId() + " "
                        + biSource.getParent().getId());
           try {
                fileParser.read(new File(fileName));
           } catch (Exception e) {
               logger.error("can't parse: " + fileName, e);
           }
           }

        } finally {
            if (fileParser != null) {
                manager.release(fileParser);
            }

            catalog.close();
        }
    }

    protected LineNumberReader openCatalog(String catalogFile)
            throws IOException {
        return new LineNumberReader(new InputStreamReader(new FileInputStream(
                catalogFile), "ISO-8859-2"));
    }

    public void read() throws ProcessingException, IOException {

            Iterator it = collections.iterator();
            while(it.hasNext()) {
                CollectionConfig cc = (CollectionConfig) it.next();
                try {
                readCatalog(cc);
                } catch (Exception e) {
                    logger.error("can't read the collection " + cc.id, e);
                   // throw new ProcessingException("component error", e);
                }
            }
      
    }

    public void dispose() {

    }

    private class SourceSetterDocumentHandler implements DocumentHandler {
        Source source;

        DocumentHandler handler;

        public SourceSetterDocumentHandler(Source source,
                DocumentHandler handler) {
            this.source = source;
            this.handler = handler;
        }

        /*
         * (non-Javadoc)
         * 
         * @see mokk.nlp.irutil.io.DocumentHandler#processDocument(mokk.nlp.irutil.Document)
         */
        public void processDocument(Document d) throws ProcessingException {
            BiSentence bis = (BiSentence) d;
            bis.setSource(source);
            //    //System.out.printlnln(source.getAbsoluteId());
            handler.processDocument(bis);

        }
    }

    private class CollectionConfig {
        String id;
        String title;
        String catalogFile;
        String corpusHome;
        String fileSuffix;
    }
    
}

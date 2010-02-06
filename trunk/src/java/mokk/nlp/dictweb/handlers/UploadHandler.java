/*
 * Created by bpgergo at gmail
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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import mokk.nlp.bicorpus.SourceDB;
import mokk.nlp.dictweb.RequestHandler;

import org.apache.avalon.fortress.util.ContextManagerConstants;
import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.activity.Initializable;
import org.apache.avalon.framework.component.Component;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.logger.LogEnabled;
import org.apache.avalon.framework.logger.Logger;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.commons.fileupload.DiskFileUpload;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.velocity.context.Context;

/**
 * @author bpgergo
 * 
 *         Handle request for user upload
 * 
 * @avalon.component
 * @avalon.service type=mokk.nlp.dictweb.RequestHandler
 * @x-avalon.info name=fileuploadhandler
 * @x-avalon.lifestyle type=singleton
 * 
 */
public class UploadHandler implements RequestHandler, Component,
		LogEnabled, Configurable, Initializable, Serviceable, Disposable {

	
	public static String encoding = "iso-8859-2";
	private ServiceManager manager;

    private String uploadDir;

    //private File contextDirectory;
    
	private Logger logger;

	private SourceDB sourceDb;

	private List availableSources = null;

	public void enableLogging(Logger logger) {
		this.logger = logger;
	}

	// TODO this is duplicate code, add it to some base class
	/*private String getContextualizedPath(String file) {
		if (file.startsWith("/")) {
			return file;
		}
		return contextDirectory.getAbsolutePath() + "/" + file;
	}*/
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.apache.avalon.framework.context.Contextualizable#contextualize(org
	 * .apache.avalon.framework.context.Context)
	 */
	/*public void contextualize(Context context) throws ContextException {
		contextDirectory = (File) context
				.get(ContextManagerConstants.CONTEXT_DIRECTORY);
		logger.info("context directory:" + contextDirectory);
	}*/
	
	public void configure(Configuration config) throws ConfigurationException {
		uploadDir = config.getChild("upload-dir").getValue();
	}

	/**
	 * @avalon.dependency type="mokk.nlp.bicorpus.SourceDB"
	 */
	public void service(ServiceManager manager) throws ServiceException {
		this.manager = manager;
	}

	public void initialize() throws Exception {
		sourceDb = (SourceDB) manager.lookup(SourceDB.ROLE);
		availableSources = new ArrayList(sourceDb.getKnownSources());
		Collections.sort(availableSources);

	}

	public void dispose() {
		if (sourceDb != null) {
			manager.release(sourceDb);
		}
	}

	public static final String DATE_FORMAT_NOW = "yyyyMMdd-HH_mm_ss";

	  public static String now() {
	    Calendar cal = Calendar.getInstance();
	    SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_NOW);
	    return sdf.format(cal.getTime());

	  }	
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * mokk.nlp.bicorpus.servlet.RequestHandler#handleRequest(javax.servlet.
	 * http.HttpServletRequest, javax.servlet.http.HttpServletResponse,
	 * org.apache.velocity.context.Context)
	 */
	public String handleRequest(HttpServletRequest request,
			HttpServletResponse response, Context context) throws Exception {
		String result =  "fileupload.vm";

logger.debug("+++++3uploader handleRequest+++++++++++++++++++++++++++++++++++++++++");

		request.setCharacterEncoding(encoding);
		response.setContentType("text/html; charset="+encoding);

		//UploadRequest uploadRequest = parseParameters(request);
		UploadRequest uploadRequest = handleFiles(request);
		logger.debug(uploadRequest.toString());
		String templateResult = "";
		if (uploadRequest.shouldWriteSummary()){
			Writer output = null;
			File file = new File(uploadDir, now());
			output = new BufferedWriter(new FileWriter(file));
			output.write(uploadRequest.toString());
			output.close();
			templateResult = "Sikeres feltöltés <br/> \n"+uploadRequest.toString();
		} else {
			templateResult = "Sikertelen feltöltés <br/> \n"+uploadRequest.toString();
		}
	    context.put("encoding", encoding);
	    context.put("result", templateResult);
		context.put("sources", availableSources);
		context.put("source", sourceDb.get(uploadRequest.getSource()));
		
		
		
		return result;

	}



	private UploadRequest handleFiles(HttpServletRequest request) {
		UploadRequest result = new UploadRequest();
        
		DiskFileUpload fu = new DiskFileUpload();
        // If file size exceeds, a FileUploadException will be thrown
        fu.setSizeMax(10000000);

        List fileItems;
		try {
			fileItems = fu.parseRequest(request);
		} catch (FileUploadException e) {
			logger.debug("handleFiles FileUploadException\n"+e);
			return result;
		}
        
        
//logger.debug("handleFiles fileItems.size():"+fileItems.size());        

        Iterator itr = fileItems.iterator();

        while(itr.hasNext()) {
          FileItem fi = (FileItem)itr.next();
          String fieldName = fi.getFieldName();
          
//logger.debug("fieldName="+fieldName);
//logger.debug("Field string ="+fi.getString());

          //Check if not form field so as to only handle the file inputs
          //else condition handles the submit button input
          if(!fi.isFormField() && result.shouldWriteFile()) {
//System.out.println("\nfile NAME: "+fi.getName());
//System.out.println("SIZE: "+fi.getSize());
            //System.out.println(fi.getOutputStream().toString());
//String path = uploadDir; //getContextualizedPath(uploadDir);
//logger.debug("--------->path:"+path);            
            File fNew= new File(uploadDir, fi.getName());

//System.out.println("--------->Absolute path!!!!!!!!!!!!!!!!!:"+fNew.getAbsolutePath());

            try {
				fi.write(fNew);
				if (fieldName.equals("huFile")){
					result.setHuFilePath(fNew.getAbsolutePath());
				} else if (fieldName.equals("enFile")){
					result.setEnFilePath(fNew.getAbsolutePath());
				}
			} catch (Exception e) {
				logger.error("handleFiles file write exception", e);				
			}
          }
          else { //if formfield
            //System.out.println("Field ="+fi.getFieldName());
			
			if (fieldName.equals("author")){
				result.setAuthor(fi.getString());
			} else if (fieldName.equals("title")){
				result.setTitle(fi.getString());
			} else if (fieldName.equals("title")){
				result.setTitle(fi.getString());
			} else if (fieldName.equals("source")){
				result.setSource(fi.getString());
			}
		
        }
      }  
      return result;
	}
	
	public UploadRequest parseParameters(HttpServletRequest request) {

		UploadRequest uploadRequest = new UploadRequest();
		
		//TODO remove this sourceId: "all"
		uploadRequest.setSource(request.getParameter("source"));
		uploadRequest.setAuthor(request.getParameter("author"));
		uploadRequest.setAuthor(request.getParameter("title"));


		String params = "::: Request Parameters :::\n";
		Enumeration<String> names = request.getParameterNames();
		while (names.hasMoreElements()){
			String name = names.nextElement();
			params += name +":::"+request.getParameterValues(name)+"\n";
		}
logger.debug(params);
		
		String ats = "::: Request attribs :::\n";
		Enumeration<String> attribs = request.getAttributeNames();
		while (attribs.hasMoreElements()){
			String attrib = attribs.nextElement();
			ats += attrib +":::"+request.getParameterValues(attrib)+"\n";
		}
logger.debug(ats);		

		
		return uploadRequest;
	}

}

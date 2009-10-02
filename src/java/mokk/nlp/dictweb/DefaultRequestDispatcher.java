/*
 * Created by Peter Halacsy <peter at halacsy.com>
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

package mokk.nlp.dictweb;

import javax.servlet.http.HttpServletRequest;

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

/**
 * @author hp
 *
 * Selects RequestHandler based on url
 * 
 * @avalon.component
 * @avalon.service type=mokk.nlp.dictweb.RequestDispatcher
 * @x-avalon.info name=path-dispatcher
 * @x-avalon.lifestyle type=singleton
 * 
 */
public class DefaultRequestDispatcher implements RequestDispatcher, Component, 
LogEnabled, 
Configurable, 
Initializable, 
Serviceable, 
Disposable {
    
    
    private Logger m_logger;
    private ServiceManager m_manager;

	private RequestHandler handler1;
	private RequestHandler jmorphHandler;
	private RequestHandler textAnalyzerHandler;
	
	private String[] m_requestHandlerPaths;
	private String[] m_requestHandlerIds;
	private RequestHandler[] m_requestHandlers;
	
	
	public void enableLogging(Logger logger) {
		this.m_logger = logger;
	}
	
	
    public void configure(Configuration config) throws ConfigurationException {
     
        
        Configuration[] configs = config.getChildren("handler");
        m_requestHandlerIds = new String[configs.length];
        m_requestHandlerPaths = new String[configs.length];
        
		for(int i = 0 ; i < configs.length ; i ++ )
		{
		    Configuration c = configs[i];
		    
		    String id = c.getChild("id").getValue();
		    String path = c.getChild("path").getValue();
		    
		    m_requestHandlerIds[i] = id;
		    m_requestHandlerPaths[i] = path;
		  
		    
		    m_logger.info ("using handler id, path" + id + " " + path);
		}
		
		if ( m_requestHandlerIds.length == 0 )
		{
		    m_logger.error("no handler configurated");
		    throw new ConfigurationException("no handler configurated");
		}
        
    }
    
	/**
	 * @avalon.dependency type="mokk.nlp.dictweb.RequestHandler"
	 */
	public void service(ServiceManager manager) throws ServiceException {
		this.m_manager = manager;
	}
	
    public void initialize() throws Exception {
        m_requestHandlers = new RequestHandler[m_requestHandlerIds.length];
        for( int i = 0 ; i < m_requestHandlerIds.length; i++)
        {
            String id = m_requestHandlerIds[i];
            RequestHandler handler = (RequestHandler) m_manager.lookup(RequestHandler.ROLE+"/" +id);
            m_requestHandlers[i] = handler;
        }
     
    }
    

    public void dispose() {
        for( int i = 0 ; i < m_requestHandlerIds.length; i++)
        {
           m_manager.release( m_requestHandlers[i] );
        }
    }
/////// END AVALON
    
    
    public RequestHandler dispatch(HttpServletRequest request) {
        m_logger.debug("path info=" + request.getPathInfo());
        
        String p = request.getPathInfo();
        
        RequestHandler selectedRequestHandler = null;
        
        for( int i = 0 ; i < m_requestHandlerIds.length; i++)
        {
           if(p.startsWith(m_requestHandlerPaths[i]))
           {
               selectedRequestHandler = m_requestHandlers[i];
           }
        }
        
        // if no one found use the first one
        if (selectedRequestHandler == null ) 
        {
            selectedRequestHandler = m_requestHandlers[0];
        }
        
        
        return selectedRequestHandler;
    }
    

  

  
}

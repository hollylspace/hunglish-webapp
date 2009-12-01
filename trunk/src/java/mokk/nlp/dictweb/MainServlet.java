/*
 * Created by Peter Halacsy <peter at halacsy.com>
 * 
 * This work is licensed under the Creative Commons 
 * Attribution License. To view a copy of this license, 
 * visit http://creativecommons.org/licenses/by/2.0/ 
 * or send a letter to Creative Commons, 559 Nathan Abbott Way, 
 * Stanford, California 94305, USA.
 * 
 * Created on Feb 7, 2005
 *
 */

package mokk.nlp.dictweb;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.avalon.fortress.ContainerManager;
import org.apache.avalon.fortress.impl.DefaultContainer;
import org.apache.avalon.fortress.impl.DefaultContainerManager;
import org.apache.avalon.fortress.util.FortressConfig;
import org.apache.avalon.framework.container.ContainerUtil;
import org.apache.velocity.Template;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.context.Context;
import org.apache.velocity.servlet.VelocityServlet;




/*
 * @author hp
 * Servlet, ami a GET parameterek alapjan a BiCorpusSearcher-rel keres
 * majd az eredmenyt XML-ben visszaadja
 **/
public class MainServlet extends VelocityServlet {

    private ContainerManager m_containerManager;
    private DefaultContainer m_container;
    private RequestDispatcher m_dispatcher = null;
   
    
    
    protected Template handleRequest(HttpServletRequest request,
            HttpServletResponse response, Context context) throws Exception {
        
        request.setCharacterEncoding("ISO-8859-2");
        response.setContentType("text/html; charset=iso-8859-2");
        
        RequestHandler handler = m_dispatcher.dispatch(request);
        
        String template = handler.handleRequest(request, response, context);
       
        Template t = getTemplate(template);
        return t;
        
    }

    public void destroy() {
        if(m_dispatcher != null ) {
            m_container.getServiceManager().release(m_dispatcher);
        }
        ContainerUtil.dispose( m_containerManager );
        super.destroy();
    }
   

    public void init(ServletConfig servletConfig) throws ServletException {
        // to init the velocity system
        super.init(servletConfig);
        
        // now init the component system
        String configFile = servletConfig.getInitParameter("component-config");
        if(configFile == null ) {
            throw new ServletException("no component-config parameter. set the web.xml");
            
        }
    
       
        String loggerConfig = servletConfig.getInitParameter("logger-config");
        if(loggerConfig == null ) {
            throw new ServletException("no logger-config parameter. set the web.xml");
            
        }
        FortressConfig config = new FortressConfig();
        config.setContainerClass( DefaultContainer.class );
        config.setContainerConfiguration(servletConfig.getServletContext().getRealPath(configFile));
        config.setLoggerManagerConfiguration( servletConfig.getServletContext().getRealPath(loggerConfig));
        config.setContextDirectory( servletConfig.getServletContext().getRealPath("/") );
        
        config.setWorkDirectory( (File) servletConfig.getServletContext().getAttribute( "javax.servlet.context.tempdir" ) );
        try {
         m_containerManager = new DefaultContainerManager( config.getContext() );
      
		
            ContainerUtil.initialize( m_containerManager );
        
		
            m_container = (DefaultContainer) m_containerManager.getContainer();
            
            m_dispatcher = (RequestDispatcher)  m_container.getServiceManager().lookup(RequestDispatcher.ROLE);
		} catch (Exception e) {
		    this.log("hey, component problem", e);
            throw new ServletException("can't init component system", e);
        }   
    }
    
    /**
     *   Called by the VelocityServlet
     *   init().  We want to set a set of properties
     *   so that templates will be found in the webapp
     *   root.  This makes this easier to work with as 
     *   an example, so a new user doesn't have to worry
     *   about config issues when first figuring things
     *   out
     */

    protected Properties loadConfiguration(ServletConfig config )
    throws IOException, FileNotFoundException
{
    Properties p = new Properties();

    /*
     *  first, we set the template path for the
     *  FileResourceLoader to the root of the 
     *  webapp.  This probably won't work under
     *  in a WAR under WebLogic, but should 
     *  under tomcat :)
     */

    String path = config.getServletContext().getRealPath("/");

    if (path == null)
    {
        //System.out.printlnln(" SampleServlet.loadConfiguration() : unable to " 
                           //+ "get the current webapp root.  Using '/'. Please fix.");

        path = "/";
    }

    p.setProperty( Velocity.FILE_RESOURCE_LOADER_PATH,  path );

    /**
     *  and the same for the log file
     */

    p.setProperty( "runtime.log", path + "velocity.log" );

    return p;
}
}

   
/*
 * Created on Jan 2, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package mokk.nlp.irutil;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

import org.apache.avalon.fortress.ContainerManager;
import org.apache.avalon.fortress.impl.DefaultContainer;
import org.apache.avalon.fortress.impl.DefaultContainerManager;
import org.apache.avalon.fortress.util.FortressConfig;
import org.apache.avalon.framework.container.ContainerUtil;
import org.apache.avalon.framework.service.ServiceManager;




/**
 * @author hp
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class SearcherConsole {
	
	
	public static void main(String[] args) throws Exception {
		if (args.length < 2) {
			System.err.println("usage: SearcherConsole conffile logconf ");
			System.exit(-1);
		}
    		
        FortressConfig config = new FortressConfig();
        config.setContainerClass( DefaultContainer.class );
        config.setContainerConfiguration( args[0]);
        config.setLoggerManagerConfiguration( args[1]);
        config.setContextDirectory( "./" );
        config.setWorkDirectory( "./" );
        

        ContainerManager m_containerManager = new DefaultContainerManager( config.getContext() );
        try {
			// ContainerUtil.configure(m_containerManager, new DefaultConfiguration("vacak"));
			ContainerUtil.initialize( m_containerManager );
		
			DefaultContainer container = (DefaultContainer) m_containerManager.getContainer();
        
			ServiceManager manager = container.getServiceManager();
       
			Searcher searcher = (Searcher) container.getServiceManager().lookup(Searcher.ROLE);
         
			
			BufferedReader r = new BufferedReader(
					new InputStreamReader(System.in, "UTF-8"));
	    	
			PrintWriter writer = new PrintWriter(
					new OutputStreamWriter(System.out, "UTF-8"));
			
			String line ;
			for(;;) {
				writer.print("magyar:" );
				writer.flush();
				String magyar = r.readLine();
				writer.print("english:" );
				writer.flush();
				String english = r.readLine();
				try {
				    searcher.search(magyar, english, 0, 10);
				} catch (SearchException se) {
				    se.printStackTrace();
				}
			}
        } catch (Exception e) {
			
			e.printStackTrace();
		} finally {
			ContainerUtil.dispose( m_containerManager );
		}
		
	}
}

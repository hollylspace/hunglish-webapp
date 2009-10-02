/*
 * Created on Jan 2, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package mokk.nlp.bicorpus.index;

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


import mokk.nlp.bicorpus.BiSentence;
import mokk.nlp.irutil.SearchResult;
import mokk.nlp.irutil.SearchException;

import java.util.Iterator;


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
       
			BiCorpusSearcher searcher = (BiCorpusSearcher) container.getServiceManager().lookup(BiCorpusSearcher.ROLE);
         
			
			BufferedReader r = new BufferedReader(
					new InputStreamReader(System.in, "UTF-8"));
	    	
			PrintWriter writer = new PrintWriter(
					new OutputStreamWriter(System.out, "UTF-8"));
			
			String line ;
			for(;;) {
				writer.println();
				writer.print("Hungarian query: " );
				writer.flush();
				String magyar = r.readLine();
				writer.print("English query: " );
				writer.flush();
				String english = r.readLine();
				SearchRequest searchRequest = new SearchRequest();
				searchRequest.setSourceId(null);
				searchRequest.setLeftQuery(magyar);
				searchRequest.setRightQuery(english);
				searchRequest.setStartOffset(0);
				searchRequest.setMaxResults(10);

				SearchResult result = null;
				try {
				    result = searcher.search(searchRequest);
				} catch (SearchException se) {
				    se.printStackTrace();
				}
    				Iterator hitIterator =  result.getHitList().iterator();
	    			while(hitIterator.hasNext()) {
		        	    BiSentence bis = (BiSentence) hitIterator.next();
				    writer.print(bis.getLeftSentence());
				    writer.print (" --- ");
				    writer.println(bis.getRightSentence());
				}
			}
        } catch (Exception e) {
			
			e.printStackTrace();
		} finally {
			ContainerUtil.dispose( m_containerManager );
		}
		
	}
}

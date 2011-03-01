/*
 * Created on Jan 8, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package mokk.nlp.bidictionary;




import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

import mokk.nlp.bicorpus.index.BiCorpusSearcher;
import mokk.nlp.bicorpus.index.SearchRequest;
import mokk.nlp.irutil.SearchException;
import mokk.nlp.irutil.SearchResult;

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
public class DictWeightCalc {


    public static void doJob(BiCorpusSearcher searcher, String dicFile) throws IOException {
        BufferedReader r = new BufferedReader(
				new InputStreamReader(new FileInputStream(dicFile), "ISO-8859-2"));
     	PrintWriter writer = new PrintWriter(new OutputStreamWriter(System.out, "ISO-8859-2"));
    	
		String line = null;
		
		while((line = r.readLine()) != null ) {
			int i = line.indexOf("@");
			if(i < 0) {
				throw new IOException("no @ found in the line:" + line);
			}
		
			String right = line.substring(0, i);
			String left = line.substring(i+1);
			
			writer.print(left);
			writer.print("@");
			writer.print(right);
			SearchResult res = null;
			SearchRequest sr = new SearchRequest();
			sr.setLeftQuery(left);
			sr.setRightQuery(right);
			sr.setStemLeftQuery(true);
			sr.setStemRightQuery(true);
			
            try {
                res = searcher.search(sr);
            } catch (SearchException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            if(res != null) {
         	writer.print("@");
			writer.println("" + res.getTotalCount());
            
            } else {
                writer.println("@");
            }
		}
		writer.close();
		r.close();
    }
    
    
    /**
     * @param args a <code>String[]</code> array of command line arguments
     * @throws Exception
     * @exception java.lang.Exception if an error occurs
     */
    public static final void main( String[] args ) throws Exception
    
    {
    		if (args.length < 3) {
			System.err.println("usage: DictWeightCalc dicfile conffile logconf");
			
			System.exit(-1);
		}
    	
    		
    		
    		
        FortressConfig config = new FortressConfig();
        config.setContainerClass( DefaultContainer.class );
        config.setContainerConfiguration( args[1] );
        config.setLoggerManagerConfiguration( args[2] );
        config.setContextDirectory( "./" );
        config.setWorkDirectory( "./" );
        

        ContainerManager m_containerManager = new DefaultContainerManager( config.getContext() );
        try {
			// ContainerUtil.configure(m_containerManager, new DefaultConfiguration("vacak"));
			ContainerUtil.initialize( m_containerManager );
		
			DefaultContainer container = (DefaultContainer) m_containerManager.getContainer();
        
			ServiceManager manager = container.getServiceManager();
       
		
			
			BiCorpusSearcher searcher = (BiCorpusSearcher) container.getServiceManager().lookup(BiCorpusSearcher.ROLE);
         
			doJob(searcher, args[0]);
			
        } catch (Exception e) {
			
			e.printStackTrace();
		} finally {
			ContainerUtil.dispose( m_containerManager );
		}
   }
}

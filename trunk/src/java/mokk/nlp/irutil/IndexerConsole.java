/*
 * Created by Peter Halacsy <peter at halacsy.com>
 * 
 * This work is licensed under the Creative Commons 
 * Attribution License. To view a copy of this license, 
 * visit http://creativecommons.org/licenses/by/2.0/ 
 * or send a letter to Creative Commons, 559 Nathan Abbott Way, 
 * Stanford, California 94305, USA.
 * 
 * Created on May 23, 2005
 *
 */

package mokk.nlp.irutil;

import org.apache.avalon.framework.service.ServiceManager;

/**
 * @author hp
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class IndexerConsole extends Console {
    public static void main(String args[]) throws Exception {
        IndexerConsole instance = new IndexerConsole();
        instance.buildContainer(args);
    }
    
    
    /* (non-Javadoc)
     * @see mokk.nlp.irutil.Console#doJob(org.apache.avalon.framework.service.ServiceManager)
     */
    public void doJob(ServiceManager manager) throws Exception {
        Indexer indexer = (Indexer) manager.lookup(Indexer.ROLE);
        
        indexer.index();
    }
}

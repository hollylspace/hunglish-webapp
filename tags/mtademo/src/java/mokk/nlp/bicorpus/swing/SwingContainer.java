/* 

 */

package mokk.nlp.bicorpus.swing;

import mokk.nlp.bicorpus.index.BiCorpusSearcher;
import mokk.nlp.irutil.SearchResult;

/**
 * Simple Fortress based container containing a Swing implementation of Hello World.
 * This container creates a small Swing based GUI displaying a combobox of available
 * languages from the translator component.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version CVS $Revision: 1.6 $ $Date: 2005-06-12 16:58:24 $
 */
public final class SwingContainer extends org.apache.avalon.fortress.impl.DefaultContainer
    implements org.apache.avalon.framework.activity.Startable, SearchEventListener, Runnable
{
    // Component references
    private BiCorpusSearcher searcher;

    // GUI references
    private javax.swing.JFrame m_frame;

    // the main panel we get the search request from
    private MainPanel  mainPanel;
    

    // Dictionary key
    private String m_key = "hello-world";

    /**
     * Initializes this component. Creates simple Swing GUI containing
     * available translations for the key 'hello-world'.
     *
     * @exception java.lang.Exception if an error occurs
     */
    public void initialize()
        throws Exception
    {
        super.initialize();

        // obtain searcher component
         searcher = (BiCorpusSearcher) m_serviceManager.lookup(BiCorpusSearcher.ROLE);
        
       
       

        // create the main panel
         mainPanel = new MainPanel(this);
       
        // create main frame
     
        m_frame = new javax.swing.JFrame( "Hello World!" );
        m_frame.setSize(640, 480);
        
        m_frame.setDefaultCloseOperation( javax.swing.JFrame.EXIT_ON_CLOSE );
        m_frame.setContentPane( mainPanel );
        m_frame.pack();

        // all done
        if( getLogger().isDebugEnabled() )
        {
            getLogger().debug( "Initialized" );
        }
    }

    /**
     * Starts the component, makes GUI visible, ready for use.
     */
    public void start()
    {
        m_frame.setVisible( true );

        if( getLogger().isDebugEnabled() )
        {
            getLogger().debug( "GUI Activated" );
        }
    }

    public void run()
    {
        while( m_frame.isVisible() )
        {
            try
            {
                Thread.sleep( 1000 );
            }
            catch( InterruptedException ie )
            {
                m_frame.setVisible( false );
            }
        }
    }

    /**
     * Stops component, make GUI invisible, ready for decomissioning.
     */
    public void stop()
    {
        m_frame.setVisible( false );

        if( getLogger().isDebugEnabled() )
        {
            getLogger().debug( "GUI Disactivated" );
        }
    }

    /**
     * Method called when the user performs a search request
     *
     * @param evt an <code>SearchEvent</code> instance
     */
    public void actionPerformed(SearchEvent se) {
        SearchResult r = new SearchResult(10, 0, 10);
        for(int i = 0; i < 100; i++) {
      //   r.addToHits(new BiSentence("" + i , "", " <b>magzar oldar</b>", "angol oldal"));
        }
       // try {
         //  r = searcher.search(se.getLeft(), se.getRight(), 0, se.getN());
            mainPanel.showSearchResult(r);
      //  } catch (SearchException e) {
            // TODO Auto-generated catch block
        //    e.printStackTrace();
       // }
        
        if( getLogger().isDebugEnabled() )
        {
            getLogger().debug( "search for " + se.getLeft() + " " + se.getRight());
        }
    }

    /**
     * Cleans up references to retrieved components.
     */
    public void dispose()
    {
        if( searcher != null )
            m_serviceManager.release( searcher );

        m_frame.dispose();

        super.dispose();
    }

    
}


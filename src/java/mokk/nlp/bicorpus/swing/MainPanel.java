/*
 * Created by Peter Halacsy <peter at halacsy.com>
 * 
 * This work is licensed under the Creative Commons 
 * Attribution License. To view a copy of this license, 
 * visit http://creativecommons.org/licenses/by/2.0/ 
 * or send a letter to Creative Commons, 559 Nathan Abbott Way, 
 * Stanford, California 94305, USA.
 * 
 * Created on Apr 23, 2005
 *
 */

package mokk.nlp.bicorpus.swing;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Iterator;

import javax.swing.JComboBox;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import mokk.nlp.bicorpus.BiSentence;
import mokk.nlp.irutil.SearchResult;

/**
 * @author hp
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class MainPanel extends JPanel implements ActionListener {
    /**
     * All action is passed to this ActionListener. Please note that MainPanel
     * is responsible only for view not the control.
     */
    protected SearchEventListener eventListener;
    
    /**
     * Left search field.
     */
    protected JComboBox leftField;
    
    /**
     * The right one.
     */
    protected JComboBox rightField;
    
    /**
     * component to show the result of the search
     */
    protected JEditorPane result;
    
    
    public MainPanel( SearchEventListener eventListener) {
        this.eventListener = eventListener;
        setLayout(new BorderLayout());
        
        Container northPanel = new JPanel(new FlowLayout());
        add(northPanel, BorderLayout.NORTH);
        
        
        JLabel leftLabel = new JLabel("hungarian:");
        leftLabel.setLabelFor(leftLabel);
        northPanel.add(leftLabel);
        
        leftField = new JComboBox();
        leftField.setEditable(true);
        northPanel.add(leftField);

        //      listen to the actions
        leftField.addActionListener(new ActionListener()
        {
          public void actionPerformed(ActionEvent ev)
          {
            if (ev.getActionCommand().endsWith("Edited"))
            {
              MainPanel.this.actionPerformed(ev);
            }
          }
        });

        
        // the same for the right label and field
        JLabel rightLabel = new JLabel("english:");
        rightLabel.setLabelFor(rightField);
        northPanel.add(rightLabel);
        
        rightField = new JComboBox();
        rightField.setEditable(true);
        northPanel.add(rightField);

        // listen to the actions
        rightField.addActionListener(new ActionListener()
        {
          public void actionPerformed(ActionEvent ev)
          {
            if (ev.getActionCommand().endsWith("Edited"))
            {
                MainPanel.this.actionPerformed(ev);
            }
          }
        });
        
        
        // the text area for the results
        result = new JEditorPane();
        result.setContentType("text/html");
   
        JScrollPane scroll = new JScrollPane(result);
        
        add(scroll, BorderLayout.CENTER);
    }


    /* (non-Javadoc)
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent arg0) {
        String left  = (String)leftField.getSelectedItem();
        leftField.insertItemAt(left, 0);

        String right  = (String) rightField.getSelectedItem();
        rightField.insertItemAt(right, 0);

        SearchEvent se = new SearchEvent( left, right, 10);
        
        eventListener.actionPerformed(se);
    }
    
    public void showSearchResult(SearchResult r) {
        // Set up an output stream we can print the table to.
        // This is easier than concatenating strings all the time.
        StringWriter sout = new StringWriter();
        PrintWriter out = new PrintWriter(sout);
        
        
        out.print("Total found: " + r.getTotalCount());
        out.print("<table border=1 width=100% >");
        Iterator hitIterator =  r.getHitList().iterator();
        while(hitIterator.hasNext()) {
            BiSentence bis = (BiSentence) hitIterator.next();
            out.print("<tr>");
            out.print("<td>");
            out.print(bis.getLeftSentence());
            out.print("</td><td> ");
            out.print(bis.getRightSentence());
            out.print("</td>");
            out.print("</tr>");
        }
        
        out.print("</table>");
        result.setText(sout.toString());
        result.setCaretPosition(0);
    }
}

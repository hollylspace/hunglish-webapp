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



/*
 * @author hp
 *
 * Event raised if the user performs a new search request
 **/
public class SearchEvent {

    protected String left;
    protected String right;
    protected int n;
    
    
    public SearchEvent(String left, String right, int n) {
     
       this.left = left;
       this.right = right;
       this.n = n;
       
    }

    public String getLeft() {
        return left;
    }
    public int getN() {
        return n;
    }
    public String getRight() {
        return right;
    }
}

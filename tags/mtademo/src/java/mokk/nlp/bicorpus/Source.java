/*
 * Created by Peter Halacsy <peter at halacsy.com>
 * 
 * This work is licensed under the Creative Commons 
 * Attribution License. To view a copy of this license, 
 * visit http://creativecommons.org/licenses/by/2.0/ 
 * or send a letter to Creative Commons, 559 Nathan Abbott Way, 
 * Stanford, California 94305, USA.
 * 
 * Created on Jun 9, 2005
 *
 */

package mokk.nlp.bicorpus;

/**
 * @author hp
 *
 * unmutuable bean representing a source of bisentence. The sources are in a hierachy.
 */
public class Source implements Comparable {
    
    public final static Source UNKNOWN_SOURCE = new Source("unknown", "unknown", null);
    
    protected String id;
    protected String title;
    
    protected Source parent;
    
    int level = 0;
    public Source(String id, String title, Source parent) {
       this(id, false, title, parent);

    }
    
    public Source(String id, boolean absoluteId, String title, Source parent) {
        if (!absoluteId) {
            if(parent == null) {
                this.id = id;
            } else {
                this.id = parent.getId() + "/" + id;
            }
        } else {
            this.id = id;
        }
        
        this.title = title;
        this.parent = parent;
        
        if(parent == null) {
            level = 0;
        } else {
            level = parent.getLevel() + 1;
        }
    }
    /**
     * @return Returns the id.
     */
    public String getId() {
        return id;
    }
   
    public int getLevel() {
        return level;
    }
    /**
     * @return Returns the parent.
     */
    public Source getParent() {
        return parent;
    }
    
    /**
     * @return Returns the title.
     */
    public String getTitle() {
        return title;
    }
    
    
   
    public int hashCode() {
        return id.hashCode();
    }
    /* (non-Javadoc)
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(Object arg0) {
        if(!(arg0 instanceof Source)) {
            return -1;
        }
        
        Source other = (Source) arg0;
        
        return this.id.compareTo(other.id);
    }
}

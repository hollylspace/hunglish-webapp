/*
 * Created by Peter Halacsy <peter at halacsy.com>
 * 
 * This work is licensed under the Creative Commons 
 * Attribution License. To view a copy of this license, 
 * visit http://creativecommons.org/licenses/by/2.0/ 
 * or send a letter to Creative Commons, 559 Nathan Abbott Way, 
 * Stanford, California 94305, USA.
 * 
 * Created on May 29, 2005
 *
 */

package mokk.nlp.dictweb.handlers;
import java.util.ArrayList;
import java.util.List;
/**
 * @author hp
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class Pager {
    
    public static void main (String args[]) {
        Pager pager = new Pager("", 0,  10, 12, "p");
      //  //System.out.printlnln(pager.getPrev().getUrl());
      //System.out.printlnntln(pager.getNext().getUrl());
    }
    private String baseQuery;
    
    private int maxN = 0;
    
    private int pageSize = 10;
    
    private int currentStart = 0;
    
    private ArrayList pages;
    
    private Page currentPage = null;
    // a pages lista hanyadik eleme az aktualis
    // hogy ki tudjuk szedni a kovetkezot es megelozot
    
    int currentPageIndex = 0;
    public Pager(String baseUrl, int currentStart, int pageSize, int maxN, String pp) {
        pages = new ArrayList(20);
        int firstPage = 0;
        
        // csak arra az esetre, ha valami vicces hiba tortent volna
        // ha 10 hit van egy oldalon, akkor legyen 10-el oszthato a currentStart
        currentStart = currentStart - (currentStart % pageSize);
        
        // a mostani oldal hanyadik
        currentPageIndex = currentStart / pageSize;
        
        // az utolso oldal hanyadik 
        int lastPage = Math.min(maxN / pageSize, currentPageIndex + 10);
        
        if(maxN/pageSize > 20) {
            // ha husznal tobb oldal lenne, akkor nem az elso laptol indulunk,
            // mar ha a mostani lap 10 folott van
            if(currentPageIndex < 10) {
                firstPage = 0;
            } else {
                firstPage = Math.max(lastPage - 20, 0);
            }
            
        }
        
        for(int i = firstPage; i <= lastPage; i++) {
            String url = baseUrl + "&" + pp+ "=" + (i * pageSize);
           
            
            
            if(i == lastPage)	{
                // az utolso oldal, lehet h rovidebb, mint pageSize
                pageSize = Math.min(maxN - i*pageSize, pageSize);
            }
            Page page = new Page(i, pageSize ,i == currentPageIndex, url);
            pages.add(page);
            if(i == currentPageIndex) {
                currentPage = page;
                currentPageIndex = pages.size() -1;
            }
        }
    }
    public List getPages() {
        return pages;
    }
    
    public boolean isNext() {
        if(currentPage != null && currentPageIndex < pages.size() - 1 ) {
            return true;
        }
        return false;
    }


    public Page getNext() {
        if(!isNext()) {
            return null;
        }
        return (Page) pages.get(currentPageIndex + 1);
    }
    public boolean isPrev() {
        if(currentPage != null && currentPageIndex > 0) {
            return true;
        }
        return false;
    }

    public Page getCurrentPage() {
        return currentPage;
        
    }
    public Page getPrev() {
        if(!isPrev()) {
            return null;
        }
        return (Page) pages.get(currentPageIndex-1);
    }

    public class Page {
        
        private int index, pageSize;
        private boolean current;
        private String url;
        
        public Page(int index, int pageSize, boolean current, String url) {
            this.index = index;
            this.current = current;
            this.url = url;
            this.pageSize = pageSize;
          
        }
        
        public int getIndex() {
            return index;
        }
        
        public int getStartOffset() {
            return (index*pageSize);
        }
        
        public int getStartOffsetLabel() {
            return getStartOffset() + 1;
        }
        public int getLabel() {
            return index + 1;
        }
        public int getEndOffset() {
            return getStartOffset() + (pageSize -1);
        }
        
        public int getEndOffsetLabel() {
            return getEndOffset() + 1;
        }
        public boolean isCurrent() {
            return current;
        }
        
        public String getUrl() {
            return url;
        }
    }
}

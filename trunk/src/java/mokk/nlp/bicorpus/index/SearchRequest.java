/*
 * Created by Peter Halacsy <peter at halacsy.com>
 * 
 * This work is licensed under the Creative Commons 
 * Attribution License. To view a copy of this license, 
 * visit http://creativecommons.org/licenses/by/2.0/ 
 * or send a letter to Creative Commons, 559 Nathan Abbott Way, 
 * Stanford, California 94305, USA.
 * 
 * Created on May 19, 2005
 *
 */

package mokk.nlp.bicorpus.index;

/**
 * Ez egy sima bean, ami egy search request-et reprezental
 */
public class SearchRequest {

    /**
     * Baloldali query string
     */
    protected String leftQuery = null;
    
    /**
     * Query with no specified field. Using by power users knowing the core lucene query
     * syntax
     */
    protected String commonQuery = null;
    
    /**
     * Jobb oldali query string
     */
    protected String rightQuery = null;
    
    /**
     * Kell-e tovezni a baloldali keresest
     */
    protected boolean stemLeftQuery = true;
    
    /**
     * Kell-e tovezni a jobboldali keresest
     */
    protected boolean stemRightQuery = true;
    
    /**
     * The first returned bisentence (used to implement a pager)
     */
    protected int startOffset = 0;
    
    /**
     * The number of max returned bisentences (used to implement a pager)
     */
    protected int maxResults = 0;
    
    /**
     * Restrict search to a specified source
     */
    protected String sourceId = null;
    
    
    /**
     * filter out duplicate values,
     * see http://code.google.com/p/hunglish-webapp/issues/detail?id=4	
     */
    protected boolean excludeDuplicates = false;
    
    
    public SearchRequest() {
       //TODO
    	System.out.println("remove me from the source");
    }

    
    /**
     * @return Returns the leftQuery.
     */
    public String getLeftQuery() {
        return leftQuery;
    }
    /**
     * @param leftQuery The leftQuery to set.
     */
    public void setLeftQuery(String leftQuery) {
        this.leftQuery = leftQuery;
    }
    
    public boolean isLeftQuery() {
        return leftQuery != null && leftQuery.length() >= 0;
    }
    
    public boolean isRightQuery() {
        return rightQuery != null && rightQuery.length() >= 0;
    }
    /**
     * @return Returns the maxResults.
     */
    public int getMaxResults() {
        return maxResults;
    }
    /**
     * @param maxResults The maxResults to set.
     */
    public void setMaxResults(int maxResults) {
        this.maxResults = maxResults;
    }
    /**
     * @return Returns the rightQuery.
     */
    public String getRightQuery() {
        return rightQuery;
    }
    /**
     * @param rightQuery The rightQuery to set.
     */
    public void setRightQuery(String rightQuery) {
        this.rightQuery = rightQuery;
    }
    /**
     * @return Returns the sourceId.
     */
    public String getSourceId() {
        return sourceId;
    }
    /**
     * @param sourceId The sourceId to set.
     */
    public void setSourceId(String sourceId) {
        this.sourceId = sourceId;
    }
    /**
     * @return Returns the startOffset.
     */
    public int getStartOffset() {
        return startOffset;
    }
    /**
     * @param startOffset The startOffset to set.
     */
    public void setStartOffset(int startOffset) {
        this.startOffset = startOffset;
    }
    /**
     * @return Returns the stemLeftQuery.
     */
    public boolean isStemLeftQuery() {
        return stemLeftQuery;
    }
    /**
     * @param stemLeftQuery The stemLeftQuery to set.
     */
    public void setStemLeftQuery(boolean stemLeftQuery) {
        this.stemLeftQuery = stemLeftQuery;
    }
    /**
     * @return Returns the stemRightQuery.
     */
    public boolean isStemRightQuery() {
        return stemRightQuery;
    }
    /**
     * @param stemRightQuery The stemRightQuery to set.
     */
    public void setStemRightQuery(boolean stemRightQuery) {
        this.stemRightQuery = stemRightQuery;
    }
    /**
     * @return Returns the commonQuery.
     */
    public String getCommonQuery() {
        return commonQuery;
    }
    /**
     * @param commonQuery The commonQuery to set.
     */
    public void setCommonQuery(String commonQuery) {
        this.commonQuery = commonQuery;
    }


	public boolean isExcludeDuplicates() {
		return excludeDuplicates;
	}


	public void setExcludeDuplicates(boolean excludeDuplicates) {
		this.excludeDuplicates = excludeDuplicates;
	}
}

/**
 * 
 */
package hu.mokk.hunglish.lucene;

import org.springframework.roo.addon.javabean.RooJavaBean;
import org.springframework.roo.addon.tostring.RooToString;


/**
 * Ez egy sima bean, ami egy search request-et reprezental
 */
@RooJavaBean
public class SearchRequest {

	/**
	 * Use the special Hunglish query syntax implemented by HunglishQueryParser
	 */
	private Boolean hunglishSyntax = false;
	
	/**
	 * The search terms should be highlighted in HU (that is the left search box) results 
	 */
	protected Boolean highlightHu = true;
	
	/**
	 * The search terms should be highlighted in EN (that is the right search box) results 
	 */
	protected Boolean highlightEn = true;

	/**
     * Baloldali query string
     */
    protected String huQuery = null;
    
    /**
     * Query with no specified field. Used only by power users knowing the core lucene query
     * syntax
     */
    protected String commonQuery = null;
    
    /**
     * Jobb oldali query string
     */
    protected String enQuery = null;
    
    /**
     * Kell-e tovezni a baloldali keresest TODO implement this
     */
    protected boolean stemHuQuery = true;
    
    /**
     * Kell-e tovezni a jobboldali keresest
     */
    protected boolean stemEnQuery = true;
    
    /**
     * The first returned bisentence (used to implement a pager)
     * param page
     */
    protected int startOffset = 0;
    
    /**
     * The number of max returned bisentences (used to implement a pager)
     * param size
     */
    protected int maxResults = 20;
    
    /**
     * Restrict search to a specified source
     * Doc.genre.id
     */
    protected String genreId = null;
    
    
    public SearchRequest() {
       //TODO
    }

    
    /**
     * @return Returns the Hungarian Query.
     */
    public String getHuQuery() {
        return huQuery;
    }
    /**
     * @param huQuery The huQuery to set.
     */
    public void setHuQuery(String huQuery) {
        this.huQuery = huQuery;
    }
    
    public boolean nonEmptyHuQuery() {
        return huQuery != null && huQuery.length() >= 0;
    }
    
    public boolean nonEmptyEnQuery() {
        return enQuery != null && enQuery.length() >= 0;
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
     * @return Returns the enQuery.
     */
    public String getEnQuery() {
        return enQuery;
    }
    /**
     * @param enQuery The enQuery to set.
     */
    public void setEnQuery(String enQuery) {
        this.enQuery = enQuery;
    }
    /**
     * @return Returns the sourceId.
     */
    public String getGenreId() {
        return genreId;
    }
    /**
     * @param sourceId The sourceId to set.
     */
    public void setGenreId(String sourceId) {
        this.genreId = sourceId;
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
     * @return Returns the stemHuQuery.
     */
    public boolean isStemHuQuery() {
        return stemHuQuery;
    }
    /**
     * @param stemHuQuery The stemHuQuery to set.
     */
    public void setStemHuQuery(boolean stemHuQuery) {
        this.stemHuQuery = stemHuQuery;
    }
    /**
     * @return Returns the stemEnQuery.
     */
    public boolean isStemEnQuery() {
        return stemEnQuery;
    }
    /**
     * @param stemEnQuery The stemEnQuery to set.
     */
    public void setStemEnQuery(boolean stemEnQuery) {
        this.stemEnQuery = stemEnQuery;
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


	public Boolean getHighlightHu() {
		return highlightHu;
	}


	public void setHighlightHu(Boolean highlightHu) {
		this.highlightHu = highlightHu;
	}


	public Boolean getHighlightEn() {
		return highlightEn;
	}


	public void setHighlightEn(Boolean highlightEn) {
		this.highlightEn = highlightEn;
	}


	public Boolean getHunglishSyntax() {
		return hunglishSyntax;
	}


	public void setHunglishSyntax(Boolean hunglishSyntax) {
		this.hunglishSyntax = hunglishSyntax;
	}

	public void setHunglishSyntax(String useHunglishSyntax) {
		this.hunglishSyntax = Boolean.parseBoolean(useHunglishSyntax);
	}

	public SearchRequest clone(){
		SearchRequest result = new SearchRequest();
		result.setCommonQuery(commonQuery);
		result.setEnQuery(enQuery);
		result.setGenreId(genreId);
		result.setHighlightEn(highlightEn);
		result.setHighlightHu(highlightHu);
		result.setHunglishSyntax(hunglishSyntax);
		result.setHuQuery(huQuery);
		result.setMaxResults(maxResults);
		result.setStartOffset(startOffset);
		result.setStemEnQuery(stemEnQuery);
		result.setStemHuQuery(stemHuQuery);
		return result;
	}
	
	public String toString(){
        StringBuilder sb = new StringBuilder();
        sb.append("hunglishSyntax: ").append(hunglishSyntax).append(", ");
        sb.append("huQuery: ").append(huQuery).append(", ");
        sb.append("enQuery: ").append(enQuery).append(", ");
        sb.append("enQuery: ").append(enQuery);
        //TODO add moar fields
        return sb.toString();
	}
}

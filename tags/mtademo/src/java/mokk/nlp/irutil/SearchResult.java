
package mokk.nlp.irutil;
import java.util.ArrayList;
import java.util.List;



public class SearchResult {
	private int totalCount;
	
	private int startOffset;
	
	private int endOffset;
	
	private List hitList;
	
	public SearchResult(int totalCount, int start, int end) {
		this.totalCount = totalCount;
		this.startOffset = start;
		this.endOffset = end;
		hitList = new ArrayList(end-start);
		
	}
	
	public SearchResult() {
		this(0, 0, 0);
	}
	/**
	 * @return Returns the endOffset.
	 */
	public int getEndOffset() {
		return endOffset;
	}
	/**
	 * @param endOffset The endOffset to set.
	 */
	public void setEndOffset(int endOffset) {
		this.endOffset = endOffset;
	}
	/**
	 * @return Returns the resultList.
	 */
	public List getHitList() {
		return hitList;
	}
	/**
	 * @param resultList The resultList to set.
	 */
	public void addToHits(Document doc) {
		this.hitList.add(doc);
	}
	
	public Document getHit(int i) {
		return (Document) hitList.get(i);
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
	 * @return Returns the totalCount.
	 */
	public int getTotalCount() {
		return totalCount;
	}
	/**
	 * @param totalCount The totalCount to set.
	 */
	public void setTotalCount(int totalCount) {
		this.totalCount = totalCount;
	}
}

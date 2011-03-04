package hu.mokk.hunglish.lucene;
import hu.mokk.hunglish.domain.Bisen;
import hu.mokk.hunglish.util.Pair;

import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.document.Document;
import org.springframework.roo.addon.javabean.RooJavaBean;


@RooJavaBean
public class SearchResult {
	
	private int totalCount;
	
	private int startOffset;
	
	private int endOffset;
	
	private List<Pair<Document, Bisen>> hitList;
	
	private List<Pair<String, String>> paginationLinks;
	
	public SearchResult(int totalCount, int start, int end) {
		this.totalCount = totalCount;
		this.startOffset = start;
		this.endOffset = end;
		hitList = new ArrayList<Pair<Document, Bisen>>(end-start);
		
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
	public List<Pair<Document, Bisen>> getHitList() {
		return hitList;
	}
	/**
	 * @param resultList The resultList to set.
	 */
	public void addToHits(Pair<Document, Bisen> pair) {
		this.hitList.add(pair);
	}

	public void addToHits(List<Pair<Document, Bisen>> docs) {
		this.hitList.addAll(docs);
	}
	
	
	public Pair<Document, Bisen> getHit(int i) {
		return hitList.get(i);
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

	public List<Pair<String, String>> getPaginationLinks() {
		return paginationLinks;
	}

	public void setPaginationLinks(List<Pair<String, String>> paginationLinks) {
		this.paginationLinks = paginationLinks;
	}

}

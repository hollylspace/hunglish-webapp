/*
 * Created on Nov 28, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package mokk.nlp.bicorpus;
import mokk.nlp.irutil.Document;

/*
 * Az alap dokumentum az egesz hunglish korpusz keresoben. Egy mondat, szo vagy
 * kifejezes par, ket nyelven.
 * 
 */
public class BiSentence implements Document {
	/*
	 * Bal oldali kifejezes
	 */
    protected  String leftSentence;
	
	/*
	 * Jobb oldali kifejezes
	 */
	protected String rightSentence;
	
	protected Source source;
	
	protected String senId;
	public BiSentence(Source source, String senId, String sentence1, String sentence2) {
	    this.source = source;
	    
	    this.senId = senId;
	    this.leftSentence = sentence1;
		this.rightSentence = sentence2;
	}
	
	public String getLeftSentence() {
		return leftSentence;
	}
	public void setLeftSentence(String sentence1) {
		this.leftSentence = sentence1;
	}
	public String getRightSentence() {
		return rightSentence;
	}
	public void setRightSentence(String sentence2) {
		this.rightSentence = sentence2;
	}
	
	public String toString() {
		return leftSentence + "\n" + rightSentence + "\n\n";
	}

   
    public String getDocId() {
      
        return getSource().getId() + "/" + getSenId();
    }
    
    public Source getSource() {
        return source;
    }
    public void setSource(Source source) {
        this.source = source;
    }
    public String getSenId() {
        return senId;
    }
    public void setSenId(String senId) {
        this.senId = senId;
    }
}

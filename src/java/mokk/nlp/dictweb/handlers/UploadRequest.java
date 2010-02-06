package mokk.nlp.dictweb.handlers;

public class UploadRequest {

	protected String author;
	
	protected String title;
	
	protected String source;
	
	protected String huSentence;
	protected String enSentence;
	
	protected String huFilePath;
	protected String enFilePath;
	
	
	public boolean shouldWriteFile(){
		return 
		author != null && !author.isEmpty() &&
		title != null && !author.isEmpty() &&
		source != null && !author.isEmpty();
	}
	
	public boolean shouldWriteSummary(){
		return shouldWriteFile() &&
		(huFilePath != null && !author.isEmpty() ||
		enFilePath != null && !author.isEmpty()) ;
	}
	
	public String toString(){
		return 
		"Author\t"+author+"\n"+
		"Title\t"+title+"\n"+
		"Source\t"+source+"\n"+
		"huFilePath\t"+huFilePath+"\n"+
		"enFilePath\t"+enFilePath+"\n"+
		"huSentence\t"+huSentence+"\n"+
		"enSentence\t"+enSentence+"\n";
		
	}
	
	public String getAuthor() {
		return author;
	}
	public void setAuthor(String author) {
		this.author = author;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getSource() {
		return source;
	}
	public void setSource(String source) {
		this.source = source;
	}
	public String getHuSentence() {
		return huSentence;
	}
	public void setHuSentence(String huSentence) {
		this.huSentence = huSentence;
	}
	public String getEnSentence() {
		return enSentence;
	}
	public void setEnSentence(String enSentence) {
		this.enSentence = enSentence;
	}
	public String getHuFilePath() {
		return huFilePath;
	}
	public void setHuFilePath(String huFilePath) {
		this.huFilePath = huFilePath;
	}
	public String getEnFilePath() {
		return enFilePath;
	}
	public void setEnFilePath(String enFilePath) {
		this.enFilePath = enFilePath;
	} 
	

	
}

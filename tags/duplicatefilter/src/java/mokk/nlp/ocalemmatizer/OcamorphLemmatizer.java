package mokk.nlp.ocalemmatizer;

import java.util.List;


public interface OcamorphLemmatizer {
	
	public String ROLE= OcamorphLemmatizer.class.getName();
	
	public List lemmatize(String word);
	
}

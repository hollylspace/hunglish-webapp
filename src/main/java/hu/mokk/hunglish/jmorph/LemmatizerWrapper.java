package hu.mokk.hunglish.jmorph;

//TODO add license and comments

import java.util.List;
import java.util.ArrayList;

import net.sf.jhunlang.jmorph.lemma.Lemma;
import net.sf.jhunlang.jmorph.lemma.Lemmatizer;

public class LemmatizerWrapper {

	protected Lemmatizer lemmatizer;
	
	protected boolean returnOOVOrig;
	protected boolean returnOrig;
	protected boolean returnPOS;

	private static int MIN_WORD_SIZE = 1;

	// false,true,false is the recommended default.
	public LemmatizerWrapper(Lemmatizer lemmatizer_, boolean returnOOVOrig_, boolean returnOrig_, boolean returnPOS_ ) {
		lemmatizer = lemmatizer_;
		returnOOVOrig = returnOOVOrig_;
		returnOrig = returnOrig_;
		returnPOS = returnPOS_;
	}

	public List<String> lemmatize( String word ) {
		List<String> results = new ArrayList<String>();
		if (word.length() < MIN_WORD_SIZE) {
			return results;
		}

		List<Lemma> lemmas = lemmatizer.lemmatize(word);

		// az eredeti tokent csak akkor adjuk vissza, ha a szo ismeretlen
		// es kertek
		if ((lemmas.size() == 0)) {
			if (returnOOVOrig || returnOrig) {
				results.add(word);
			}
		} else {
			if (returnOrig) {
				results.add(word);
			}
			//boolean isFirst = true;
			for (Lemma lemma : lemmas) {
				String stemmedText;
				if (returnPOS) {
					stemmedText = lemma.getWord() + "/" + lemma.getPOS();
				} else {
					stemmedText = lemma.getWord();
				}
				results.add(stemmedText);
			}
		}
		return results;
	}

	public boolean isReturnOrig() {
		return returnOrig;
	}
}

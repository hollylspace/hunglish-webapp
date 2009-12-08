package lucenedemo;

import net.sf.jhunlang.jmorph.lemma.Lemmatizer;

public class LemmatizerWrapper {

	Lemmatizer lemmatizer;
	
	protected boolean returnOOVOrig;

	protected boolean returnPOS;

	protected boolean returnOrig;

	public Lemmatizer getLemmatizer() {
		return lemmatizer;
	}

	public void setLemmatizer(Lemmatizer lemmatizer) {
		this.lemmatizer = lemmatizer;
	}

	public boolean isReturnOOVOrig() {
		return returnOOVOrig;
	}

	public void setReturnOOVOrig(boolean returnOOVOrig) {
		this.returnOOVOrig = returnOOVOrig;
	}

	public boolean isReturnPOS() {
		return returnPOS;
	}

	public void setReturnPOS(boolean returnPOS) {
		this.returnPOS = returnPOS;
	}

	public boolean isReturnOrig() {
		return returnOrig;
	}

	public void setReturnOrig(boolean returnOrig) {
		this.returnOrig = returnOrig;
	}
	
}

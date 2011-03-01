package lucenedemo;

import org.apache.lucene.util.AttributeImpl;

public final class PartOfSpeechAttributeImpl extends AttributeImpl implements
		PartOfSpeechAttribute {

	private PartOfSpeech pos = PartOfSpeech.Unknown;

	public void setPartOfSpeech(PartOfSpeech pos) {
		this.pos = pos;
	}

	public PartOfSpeech getPartOfSpeech() {
		return pos;
	}

	public void clear() {
		pos = PartOfSpeech.Unknown;
	}

	public void copyTo(AttributeImpl target) {
		((PartOfSpeechAttributeImpl) target).pos = pos;
	}

	public boolean equals(Object other) {
		if (other == this) {
			return true;
		}

		if (other instanceof PartOfSpeechAttributeImpl) {
			return pos == ((PartOfSpeechAttributeImpl) other).pos;
		}

		return false;
	}

	public int hashCode() {
		return pos.ordinal();
	}
}
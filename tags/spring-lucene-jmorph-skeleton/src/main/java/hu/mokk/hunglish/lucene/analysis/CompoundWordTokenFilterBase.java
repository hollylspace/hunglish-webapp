package hu.mokk.hunglish.lucene.analysis;

//TODO add license

import java.io.IOException;
import java.util.LinkedList;

import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.FlagsAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.PayloadAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.apache.lucene.analysis.tokenattributes.TermAttribute;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;

/**
 * Base class for decomposition token filters.
 */
public abstract class CompoundWordTokenFilterBase extends TokenFilter {

	@SuppressWarnings("unchecked")
	protected final LinkedList tokens;

	private TermAttribute termAtt;
	private OffsetAttribute offsetAtt;
	private FlagsAttribute flagsAtt;
	private PositionIncrementAttribute posIncAtt;
	private TypeAttribute typeAtt;
	private PayloadAttribute payloadAtt;

	private final Token wrapper = new Token();

	@SuppressWarnings("unchecked")
	protected CompoundWordTokenFilterBase(TokenStream input) {
		super(input);

		this.tokens = new LinkedList();
		termAtt = addAttribute(TermAttribute.class);
		offsetAtt = addAttribute(OffsetAttribute.class);
		flagsAtt = addAttribute(FlagsAttribute.class);
		posIncAtt = addAttribute(PositionIncrementAttribute.class);
		typeAtt = addAttribute(TypeAttribute.class);
		payloadAtt = addAttribute(PayloadAttribute.class);
	}

	private final void setToken(final Token token) throws IOException {
		termAtt.setTermBuffer(token.termBuffer(), 0, token.termLength());
		flagsAtt.setFlags(token.getFlags());
		typeAtt.setType(token.type());
		offsetAtt.setOffset(token.startOffset(), token.endOffset());
		posIncAtt.setPositionIncrement(token.getPositionIncrement());
		payloadAtt.setPayload(token.getPayload());
	}

	@Override
	public final boolean incrementToken() throws IOException {
		if (tokens.size() > 0) {
			setToken((Token) tokens.removeFirst());
			return true;
		}

		if (input.incrementToken() == false)
			return false;

		wrapper.setTermBuffer(termAtt.termBuffer(), 0, termAtt.termLength());
		wrapper.setStartOffset(offsetAtt.startOffset());
		wrapper.setEndOffset(offsetAtt.endOffset());
		wrapper.setFlags(flagsAtt.getFlags());
		wrapper.setType(typeAtt.type());
		wrapper.setPositionIncrement(posIncAtt.getPositionIncrement());
		wrapper.setPayload(payloadAtt.getPayload());

		decomposeInternal(wrapper);

		if (tokens.size() > 0) {
			setToken((Token) tokens.removeFirst());
			return true;
		} else {
			return false;
		}
	}

	protected abstract void decomposeInternal(final Token token);

	@Override
	public void reset() throws IOException {
		super.reset();
		tokens.clear();
	}
}

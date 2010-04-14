package hu.mokk.hunglish.lucene.analysis;

import hu.mokk.hunglish.jmorph.LemmatizerWrapper;

import java.util.List;

import net.sf.jhunlang.jmorph.lemma.Lemma;

import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;

/**
 * A {@link TokenFilter} that decomposes compound words found in many Germanic
 * languages.
 * <p>
 * "Donaudampfschiff" becomes Donau, dampf, schiff so that you can find
 * "Donaudampfschiff" even when you only enter "schiff".
 * </p>
 */
public class CompoundStemmerTokenFilter extends CompoundWordTokenFilterBase {

	protected LemmatizerWrapper lemmatizerWrapper = null;

	private static int MIN_WORD_SIZE = 1;

	/**
	 * 
	 * @param input
	 *            the {@link TokenStream} to process
	 */
	public CompoundStemmerTokenFilter(TokenStream input,
			LemmatizerWrapper lemmatizerWrapper) {
		super(input);
		this.lemmatizerWrapper = lemmatizerWrapper;

	}

	private void add(Object token) {
		// System.out.println("#token:"+token.toString());
		tokens.add(token);
	}

	@Override
	protected void decomposeInternal(final Token token) {
//System.out.println("### incoming token:"+token.toString());

		// TODO FIXME Only words longer than minWordSize get processed ?
		if (token.termLength() < this.MIN_WORD_SIZE) {
			return;
		}
		// this was a nasty bug: token.termBuffer()
		String origWord = new String(token.term());
		// System.out.println("### incoming origWord:"+origWord);
		List<Lemma> lemmas = lemmatizerWrapper.getLemmatizer().lemmatize(
				origWord);
		// az eredeti tokent csak akkor adjuk vissza, ha a szo ismeretlen
		// es kertek
		if ((lemmas.size() == 0)) {
			// System.out.println("%%%%%% lemma size == 0 origWord:"+origWord);
			if (lemmatizerWrapper.isReturnOOVOrig()
					|| lemmatizerWrapper.isReturnOrig()) {
				add(token.clone());
			}
		} else {
			// System.out.println("YYYYYY lemma size > 0 origWord:"+origWord);
			if (lemmatizerWrapper.isReturnOrig()) {
				add(token.clone());
			}
			boolean isFirst = true;
			for (Lemma lemma : lemmas) {
				Token stemToken = null;
				String stemmedText;
				if (lemmatizerWrapper.isReturnPOS()) {
					stemmedText = lemma.getWord() + "/" + lemma.getPOS();
				} else {
					stemmedText = lemma.getWord();
				}
//System.out.println("$$$temmed:"+stemmedText);
				stemToken = new Token(stemmedText, token.startOffset(), token
						.endOffset(), token.type());

				// put the token representing the stem to the same position as
				// the original word if the orig word won't be returned
				if (lemmatizerWrapper.isReturnOrig() || !isFirst) {
					stemToken.setPositionIncrement(0);
				}
				// if the original token is the same as the stemmed text
				// and the origian token was returned as well
				// then no need to return the stemmed token
				if (!(lemmatizerWrapper.isReturnOrig() && origWord
						.toLowerCase().equals(stemmedText.toLowerCase()))) {
					add(stemToken);
				}
				isFirst = false;
			}
		}

	}
}

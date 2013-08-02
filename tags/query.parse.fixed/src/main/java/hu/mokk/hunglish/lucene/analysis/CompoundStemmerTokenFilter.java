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
		if (input == null){
			throw new IllegalArgumentException("TokenStream cannot be null");
		}
		if (lemmatizerWrapper == null){
			throw new IllegalArgumentException("LemmatizerWrapper cannot be null");
		}

	}

	@SuppressWarnings("unchecked")
	private void add(Object token) {
		tokens.add(token);
	}

	@Override
	protected void decomposeInternal(final Token token) {

		String origWord = new String(token.term());
		List<String> lemmas = lemmatizerWrapper.lemmatize(origWord);

		// the original token will be preserved?
		if (lemmatizerWrapper.isReturnOrig() || lemmas.size() == 0) {
			add(token.clone());
		}

		boolean isFirst = true;
		for (String lemma : lemmas) {
			// if the original token is the same as the stemmed text
			// and the origianal token was returned as well
			// then no need to return the stemmed token
			if (!origWord.toLowerCase().equals(lemma.toLowerCase())) {

				Token stemToken = new Token(lemma, token.startOffset(), token
						.endOffset(), token.type());

				// put the token representing the stem to the same position as
				// the original word if the orig word won't be returned
				if (lemmatizerWrapper.isReturnOrig() || !isFirst) {
					stemToken.setPositionIncrement(0);
				}
				add(stemToken);
			}
			isFirst = false;
		}
	}
}

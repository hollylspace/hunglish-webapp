package hu.mokk.hunglish.lucene.analysis;

import hu.mokk.hunglish.jmorph.LemmatizerWrapper;

import java.io.IOException;
import java.io.Reader;
import java.util.Map;
import java.util.Set;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.LowerCaseFilter;
import org.apache.lucene.analysis.StopFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.standard.StandardFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.util.Version;

/**
 * This class is created from Lucene {@link StandardAnalyzer}. 
 * It filters {@link StandardTokenizer} 
 * with {@link CompoundStemmerTokenFilter},
 * {@link LowerCaseFilter} 
 * and {@link StopFilter}, using a list of stop words provided.
 * 
 * User of the class will provide a map of fieldNames and {@link LemmatizerWrapper}.
 * 
 * 
 * <a name="version"/>
 * <p>
 * You must specify the required {@link Version} compatibility when creating
 * StandardAnalyzer:
 * <ul>
 * <li>As of 2.9, StopFilter preserves position increments
 * <li>As of 2.4, Tokens incorrectly identified as acronyms are corrected (see
 * <a href="https://issues.apache.org/jira/browse/LUCENE-1068">LUCENE-1608</a>
 * </ul>
 */
public class StemmerAnalyzer extends Analyzer {

	/**
	 * assigns a lemmatizer to a fieldName
	 */
	protected Map<String, LemmatizerWrapper> lemmatizers = null;

	/**
	 * assigns a stopset to a fieldName
	 */
	protected Map<String, Set<?>> stopsets = null;

	// private Set<?> stopSetEn = null;
	// private Set<?> stopSetHu = null;
	/**
	 * An unmodifiable set containing some common English words that are usually
	 * not useful for searching.
	 */
	// TODO
	// public static final Set<?> STOP_WORDS_SET_HU = null;
	// public static final Set<?> STOP_WORDS_SET_EN =
	// StopAnalyzer.ENGLISH_STOP_WORDS_SET;

	/**
	 * Specifies whether deprecated acronyms should be replaced with HOST type.
	 * See {@linkplain https://issues.apache.org/jira/browse/LUCENE-1068}
	 */
	private final boolean replaceInvalidAcronym, enableStopPositionIncrements;

	private final Version matchVersion;

	/**
	 * Builds an analyzer with the default stop words ({@link #STOP_WORDS_SET}).
	 * 
	 * @param matchVersion
	 *            Lucene version to match See
	 *            {@link <a href="#version">above</a>}
	 */
	public StemmerAnalyzer(Version matchVersion,
			Map<String, LemmatizerWrapper> lemmatizers) {
		this(matchVersion, lemmatizers, null);
	}

	/**
	 * Builds an analyzer with the given stop words.
	 * 
	 * @param matchVersion
	 *            Lucene version to match See
	 *            {@link <a href="#version">above</a>}
	 * @param stopWords
	 *            stop words
	 */
	@SuppressWarnings("deprecation")
	public StemmerAnalyzer(Version matchVersion,
			Map<String, LemmatizerWrapper> lemmatizers,
			Map<String, Set<?>> stopsets) {
		this.stopsets = stopsets;
		// TODO Why
		setOverridesTokenStreamMethod(StandardAnalyzer.class);
		enableStopPositionIncrements = StopFilter
				.getEnablePositionIncrementsVersionDefault(matchVersion);
		replaceInvalidAcronym = matchVersion.onOrAfter(Version.LUCENE_24);
		this.matchVersion = matchVersion;
		this.lemmatizers = lemmatizers;

	}

	private LemmatizerWrapper getLemmatizerWrapper(String fieldName) {
		LemmatizerWrapper lemmatizer = null;
		if (lemmatizers != null) {
			lemmatizer = lemmatizers.get(fieldName);
		}
		return lemmatizer;
	}

	private Set<?> getStopSet(String fieldName) {
		Set<?> stopset = null;
		if (stopsets != null) {
			stopset = stopsets.get(fieldName);
		}
		return stopset;
	}

	/**
	 * Constructs a {@link StandardTokenizer} filtered by a
	 * {@link StandardFilter}, a {@link LowerCaseFilter} and a
	 * {@link StopFilter}.
	 */
	@Override
	public TokenStream tokenStream(String fieldName, Reader reader) {

		StandardTokenizer tokenStream = new StandardTokenizer(matchVersion,
				reader);
		tokenStream.setMaxTokenLength(maxTokenLength);
		LemmatizerWrapper lemmatizerWrapper = getLemmatizerWrapper(fieldName);
		TokenStream result = null;
		if (lemmatizerWrapper != null) {
			result = new CompoundStemmerTokenFilter(tokenStream,
					lemmatizerWrapper);
		} else {
			result = new StandardFilter(tokenStream);
		}
		result = new LowerCaseFilter(result);
		Set<?> stopSet = getStopSet(fieldName);
		if (stopSet != null) {
			result = new StopFilter(enableStopPositionIncrements, result,
					stopSet);
		}
		return result;
	}

	private static final class SavedStreams {
		StandardTokenizer tokenStream;
		TokenStream filteredTokenStream;
	}

	/** Default maximum allowed token length */
	public static final int DEFAULT_MAX_TOKEN_LENGTH = 255;

	private int maxTokenLength = DEFAULT_MAX_TOKEN_LENGTH;

	/**
	 * Set maximum allowed token length. If a token is seen that exceeds this
	 * length then it is discarded. This setting only takes effect the next time
	 * tokenStream or reusableTokenStream is called.
	 */
	public void setMaxTokenLength(int length) {
		maxTokenLength = length;
	}

	/**
	 * @see #setMaxTokenLength
	 */
	public int getMaxTokenLength() {
		return maxTokenLength;
	}

	@Override
	public TokenStream reusableTokenStream(String fieldName, Reader reader)
			throws IOException {
		if (overridesTokenStreamMethod) {
			// LUCENE-1678: force fallback to tokenStream() if we
			// have been subclassed and that subclass overrides
			// tokenStream but not reusableTokenStream
			return tokenStream(fieldName, reader);
		}
		SavedStreams streams = (SavedStreams) getPreviousTokenStream();
		if (streams == null) {
			streams = new SavedStreams();
			setPreviousTokenStream(streams);
			streams.tokenStream = new StandardTokenizer(matchVersion, reader);
			LemmatizerWrapper lemmatizerWrapper = getLemmatizerWrapper(fieldName);
			if (lemmatizerWrapper != null) {
				streams.filteredTokenStream = new CompoundStemmerTokenFilter(
						streams.tokenStream, lemmatizerWrapper);
			} else {
				streams.filteredTokenStream = new StandardFilter(
						streams.tokenStream);
			}
			streams.filteredTokenStream = new LowerCaseFilter(
					streams.filteredTokenStream);

			Set<?> stopSet = getStopSet(fieldName);
			if (stopSet != null) {
				streams.filteredTokenStream = new StopFilter(
						enableStopPositionIncrements,
						streams.filteredTokenStream, stopSet);
			}
		} else {
			streams.tokenStream.reset(reader);
		}
		streams.tokenStream.setMaxTokenLength(maxTokenLength);

		streams.tokenStream.setReplaceInvalidAcronym(replaceInvalidAcronym);

		return streams.filteredTokenStream;
	}
}

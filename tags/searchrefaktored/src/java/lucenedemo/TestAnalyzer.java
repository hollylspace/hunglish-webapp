package lucenedemo;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.WhitespaceTokenizer;
import org.apache.lucene.analysis.tokenattributes.TermAttribute;

public class TestAnalyzer extends Analyzer {

	public TokenStream tokenStream(String fieldName, Reader reader) {
		TokenStream stream = new WhitespaceTokenizer(reader);
		stream = new PartOfSpeechTaggingFilter(stream);
		return stream;
	}

	public static void main(String[] args) throws IOException {
		// text to tokenize
		final String text = "This is a demo of the new TokenStream API";

		TestAnalyzer analyzer = new TestAnalyzer();
		TokenStream stream = analyzer.tokenStream("field", new StringReader(
				text));

		// get the TermAttribute from the TokenStream
		TermAttribute termAtt = stream.addAttribute(TermAttribute.class);

		// get the PartOfSpeechAttribute from the TokenStream
		PartOfSpeechAttribute posAtt = stream
				.addAttribute(PartOfSpeechAttribute.class);

		stream.reset();

		// print all tokens until stream is exhausted
		while (stream.incrementToken()) {
			System.out
					.println(termAtt.term() + ": " + posAtt.getPartOfSpeech());
		}

		stream.end();
		stream.close();
	}
}

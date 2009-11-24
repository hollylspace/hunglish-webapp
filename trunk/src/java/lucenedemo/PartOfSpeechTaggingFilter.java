package lucenedemo;

import java.io.IOException;

import lucenedemo.PartOfSpeechAttribute.PartOfSpeech;

import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.TermAttribute;

public class PartOfSpeechTaggingFilter extends TokenFilter {
    PartOfSpeechAttribute posAtt;
    TermAttribute termAtt;
    
    protected PartOfSpeechTaggingFilter(TokenStream input) {
      super(input);
      posAtt = addAttribute(PartOfSpeechAttribute.class);
      termAtt = addAttribute(TermAttribute.class);
    }
    
    public boolean incrementToken() throws IOException {
      if (!input.incrementToken()) {return false;}
      posAtt.setPartOfSpeech(determinePOS(termAtt.termBuffer(), 0, termAtt.termLength()));
      return true;
    }
    
    // determine the part of speech for the given term
    protected PartOfSpeech determinePOS(char[] term, int offset, int length) {
      // naive implementation that tags every uppercased word as noun
      if (length > 0 && Character.isUpperCase(term[0])) {
        return PartOfSpeech.Noun;
      }
      return PartOfSpeech.Unknown;
    }
  }
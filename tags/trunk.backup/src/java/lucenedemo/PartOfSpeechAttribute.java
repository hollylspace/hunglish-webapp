package lucenedemo;

import org.apache.lucene.util.Attribute;

public interface PartOfSpeechAttribute extends Attribute {
    public static enum PartOfSpeech {
      Noun, Verb, Adjective, Adverb, Pronoun, Preposition, Conjunction, Article, Unknown
    }
  
    public void setPartOfSpeech(PartOfSpeech pos);
  
    public PartOfSpeech getPartOfSpeech();
  }
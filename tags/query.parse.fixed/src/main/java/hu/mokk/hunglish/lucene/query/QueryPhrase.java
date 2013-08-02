/**
 * 
 */
package hu.mokk.hunglish.lucene.query;

/**
 * @author Peter Halacsy <peter at halacsy.com>
 * 
 */
public class QueryPhrase {

	public static final class Qualifier {
		public static final Qualifier SHOULD = new Qualifier();
		public static final Qualifier MUST = new Qualifier();
		public static final Qualifier MUSTNOT = new Qualifier();

	};

	public static final class Field {
		public static final Field HU = new Field();
		public static final Field EN = new Field();
	};

	public Qualifier qualifier;
	private boolean stemmed;
	public Field field;

	String[] terms;

	/**
	 * @param field2
	 * @param terms2
	 * @param qualifier2
	 * @param stemmed2
	 */
	public QueryPhrase(Field field, String[] terms, Qualifier qualifier,
			boolean stemmed) {
		this.field = field;
		this.terms = terms;
		this.qualifier = qualifier;
		this.stemmed = stemmed;

	}

	public String[] getTerms() {
		return terms;
	}

	public String getTermsSpaceSeparated() {
		String result = "";
		if (terms.length > 0){
			for (String term : terms){
				result += term + " ";
			}
		}
		return result.trim();
	}
	
	public boolean isStemmed() {
		return stemmed;
	}

	public Qualifier getQualifier() {
		return qualifier;
	}

	public Field getField() {
		return field;
	}

	public String toString() {
		StringBuffer buff = new StringBuffer();
		buff.append("Phrase:");
		for (int i = 0; i < terms.length; ++i) {
			buff.append(" ");
			buff.append(terms[i]);
		}
		buff.append(" ");
		buff.append(qualifier == Qualifier.SHOULD ? "SHOULD"
				: (qualifier == Qualifier.MUST ? "MUST" : "MUSTNOT"));
		buff.append(" ");
		buff.append(field == Field.HU ? "HU" : "EN");
		buff.append(" ");

		buff.append(stemmed ? "stemmed" : "not stemmed");
		buff.append('\n');
		return buff.toString();

	}

}

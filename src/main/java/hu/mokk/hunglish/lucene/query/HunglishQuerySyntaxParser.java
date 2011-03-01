package hu.mokk.hunglish.lucene.query;

import java.util.ArrayList;
import java.util.LinkedList;

/**
 * 
 * @author Peter Halacsy <peter at halacsy.com>
 *
 */
public class HunglishQuerySyntaxParser {

    public static void main(String args[]) throws Exception {
        HunglishQuerySyntaxParser qp = new HunglishQuerySyntaxParser();
        System.out.println(qp.parse("-<ablak> \"ajto ablak\"", "-<don't know>"));
    }
    public QueryStructure parse(String hu, String en) throws Exception {
        QueryStructure qs = new QueryStructure();

        if(hu != null && hu.length() > 0) {
            parseHunglishSearchbox(hu, QueryPhrase.Field.HU, qs);
        }
        
        if(en != null && en.length() > 0) {
            parseHunglishSearchbox(en, QueryPhrase.Field.EN, qs);
        }
        return qs;
    }

 

    void parseHunglishSearchbox(String q, QueryPhrase.Field field,
            QueryStructure queryStructure) throws Exception {
        String[] ps = primitiveParse(q);

        for (int i = 0; i < ps.length; ++i) {
            String p = ps[i];

            QueryPhrase queryPhrase = parseAnnotatedPhrase(p, field);

            queryStructure.addPhrase(queryPhrase);
        }
    }

    private QueryPhrase parseAnnotatedPhrase(String pc, QueryPhrase.Field field)
            throws Exception {
        int mode = noparaMode;

        QueryPhrase.Qualifier qualifier;
        boolean stemmed;

        String[] terms;

        String p = pc;

        if (p.charAt(0) == '-') {
            qualifier = QueryPhrase.Qualifier.MUSTNOT;
            p = p.substring(1);
        } else if (p.charAt(0) == '+') {
            qualifier = QueryPhrase.Qualifier.MUST;
            p = p.substring(1);
        } else {
            qualifier = QueryPhrase.Qualifier.SHOULD;
        }

        char start = p.charAt(0);
        char end = p.charAt(p.length() - 1);

        if (start == '<') {
            mode = bracketMode;

            if (end != '>') {
                throw new Exception("no closing bracket: " + p);
            }
            p = p.substring(1, p.length() - 1);
        } else if (start == '"') {
            mode = quoteMode;

            if (end != '"') {
                throw new Exception("no closing quotation mark: " + p);
            }
            p = p.substring(1, p.length() - 1);
        }

        stemmed = (mode != bracketMode);

        terms = parsePhrase(p);
        return new QueryPhrase(field, terms, qualifier, stemmed);
    }

    private String[] parsePhrase(String s) throws Exception {
        LinkedList<String> terms = new LinkedList<String>();

        String q = s + " ";

        StringBuffer collected = new StringBuffer();

        for (int i = 0; i < q.length(); ++i) {

            char c = q.charAt(i);

            if (Character.isWhitespace(c) || (c == '-') || (c == '.')) {

                if (collected.length() > 0) {

                    terms.add(collected.toString());
                    collected = new StringBuffer();
                }
            } else {

                collected.append(c);
            }

            if ((c == '"') || (c == '<') || (c == '>') || (c == '+')) {
                {
                    throw new Exception("not valid character found: " + c);
                }
            }
        }
        return (String[]) terms.toArray(new String[0]);
    }

    final static int noparaMode = 1;

    final static int bracketMode = 2;

    final static int quoteMode = 3;

    /*
     * Tokenize the text but leaves the " - + < > signs sticked to the tokens
     */
    private String[] primitiveParse(final String qc) throws Exception {
        ArrayList<String> ps = new ArrayList<String>();

        String q = qc + " ";

        StringBuffer collected = new StringBuffer();
        int mode = noparaMode;

        for (int i = 0; i < q.length(); ++i) {
            char c = q.charAt(i);

            if ((Character.isWhitespace(c)) && (mode == noparaMode)) {
                if (collected.length() > 0) {
                    ps.add(collected.toString());
                    collected = new StringBuffer();
                }
            } else {
                collected.append(c);
            }

            if (c == '"') {
                switch (mode) {
                case noparaMode:
                    mode = quoteMode;
                    break;

                case bracketMode:
                    throw new Exception(
                            "found bracket inside quotation mark");

                case quoteMode:
                    mode = noparaMode;
                    break;
                }
            }

            if (c == '<') {
                switch (mode) {
                case noparaMode:
                    mode = bracketMode;
                    break;

                case bracketMode:
                    throw new Exception(
                            "found opening bracket after other ");

                case quoteMode:
                    throw new Exception(
                            "found opening bracket inside quatation mark");

                }
            }

            if (c == '>') {
                switch (mode) {
                case noparaMode:
                    throw new Exception(
                            "found closing bracket but no opening one");

                case bracketMode:
                    mode = noparaMode;
                    break;

                case quoteMode:
                    throw new Exception(
                            "found closing bracket inside quatation marks");

                }
            }
        }

        if (mode != noparaMode) {
            throw new Exception("no closing bracket or quotation mark");
        }
        return (String[]) ps.toArray(new String[0]);
    }

}
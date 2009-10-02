/*
 * Created by Peter Halacsy <peter at halacsy.com>
 * 
 * This work is licensed under the Creative Commons Attribution License. To view
 * a copy of this license, visit http://creativecommons.org/licenses/by/2.0/ or
 * send a letter to Creative Commons, 559 Nathan Abbott Way, Stanford,
 * California 94305, USA.
 * 
 * Created on Jul 1, 2005
 *  
 */

package mokk.nlp.bicorpus.index.query;

import java.util.*;

import mokk.nlp.bicorpus.index.query.QueryPhrase.Field;
import mokk.nlp.bicorpus.index.query.QueryPhrase.Qualifier;

public class HunglishQueryParser {

    public static void main(String args[]) throws ParseException {
        HunglishQueryParser qp = new HunglishQueryParser();
        System.out.println(qp.parse("-<ablak> \"ajto ablak\"", "-<don't know>"));
    }
    public QueryStructure parse(String left, String right) throws ParseException {
        QueryStructure qs = new QueryStructure();

        if(left != null && left.length() > 0) {
            parseHunglishSearchbox(left, QueryPhrase.Field.LEFT, qs);
        }
        
        if(right != null && right.length() > 0) {
            parseHunglishSearchbox(right, QueryPhrase.Field.RIGHT, qs);
        }
        return qs;
    }

 

    void parseHunglishSearchbox(String q, QueryPhrase.Field field,
            QueryStructure queryStructure) throws ParseException {
        String[] ps = primitiveParse(q);

        for (int i = 0; i < ps.length; ++i) {
            String p = ps[i];

            QueryPhrase queryPhrase = parseAnnotatedPhrase(p, field);

            queryStructure.addPhrase(queryPhrase);
        }
    }

    private QueryPhrase parseAnnotatedPhrase(String pc, QueryPhrase.Field field)
            throws ParseException {
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
                throw new ParseException("no closing bracket: " + p);
            }
            p = p.substring(1, p.length() - 1);
        } else if (start == '"') {
            mode = quoteMode;

            if (end != '"') {
                throw new ParseException("no closing quotation mark: " + p);
            }
            p = p.substring(1, p.length() - 1);
        }

        stemmed = (mode != bracketMode);

        terms = parsePhrase(p);
        return new QueryPhrase(field, terms, qualifier, stemmed);
    }

    private String[] parsePhrase(String s) throws ParseException {
        LinkedList terms = new LinkedList();

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
                    throw new ParseException("not valid character found: " + c);
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
    private String[] primitiveParse(final String qc) throws ParseException {
        ArrayList ps = new ArrayList();

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
                    throw new ParseException(
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
                    throw new ParseException(
                            "found opening bracket after other ");

                case quoteMode:
                    throw new ParseException(
                            "found opening bracket inside quatation mark");

                }
            }

            if (c == '>') {
                switch (mode) {
                case noparaMode:
                    throw new ParseException(
                            "found closing bracket but no opening one");

                case bracketMode:
                    mode = noparaMode;
                    break;

                case quoteMode:
                    throw new ParseException(
                            "found closing bracket inside quatation marks");

                }
            }
        }

        if (mode != noparaMode) {
            throw new ParseException("no closing bracket or quotation mark");
        }
        return (String[]) ps.toArray(new String[0]);
    }

}
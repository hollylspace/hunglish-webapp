/*
 * Created by Peter Halacsy <peter at halacsy.com>
 * 
 * This work is licensed under the Creative Commons 
 * Attribution License. To view a copy of this license, 
 * visit http://creativecommons.org/licenses/by/2.0/ 
 * or send a letter to Creative Commons, 559 Nathan Abbott Way, 
 * Stanford, California 94305, USA.
 * 
 * Created on May 10, 2005
 *
 */

package mokk.nlp.jmorph;



/**
 * Simple avalon-aware wrapper around jmorph lemmatizer interface adding
 * ROLE
 */
public interface Lemmatizer extends net.sf.jhunlang.jmorph.lemma.Lemmatizer {
    public String ROLE=Lemmatizer.class.getName();
    
}

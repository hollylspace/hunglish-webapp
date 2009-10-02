/*
 * Created by Peter Halacsy <peter at halacsy.com>
 * 
 * This work is licensed under the Creative Commons 
 * Attribution License. To view a copy of this license, 
 * visit http://creativecommons.org/licenses/by/2.0/ 
 * or send a letter to Creative Commons, 559 Nathan Abbott Way, 
 * Stanford, California 94305, USA.
 * 
 * Created on Jun 11, 2005
 *
 */

package mokk.nlp.bicorpus;

import java.util.Collection;
/**
 * Mivel indexeleshez csak sourceId-ket hasznalunk, ezert kell egy id - Source object
 * adatbazis. Egy ilyet epithetunk indexeleskor (katalogus alapjan), majd elmentve lehet
 * kereseshez hasznalni.
 */
public interface SourceDB {


    
    public static String ROLE = SourceDB.class.getName();

    public Source get(String id);
    
    public Collection getKnownSources();
    
}

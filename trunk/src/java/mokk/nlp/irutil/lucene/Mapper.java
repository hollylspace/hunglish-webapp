/*
 * Created by Peter Halacsy <peter at halacsy.com>
 * 
 * This work is licensed under the Creative Commons 
 * Attribution License. To view a copy of this license, 
 * visit http://creativecommons.org/licenses/by/2.0/ 
 * or send a letter to Creative Commons, 559 Nathan Abbott Way, 
 * Stanford, California 94305, USA.
 * 
 * Created on Apr 25, 2005
 *
 */

package mokk.nlp.irutil.lucene;

import mokk.nlp.irutil.Document;



/*
 * Az hid a logikai dokumentumok (cikkek, weboldalak, bimondatok) es a Lucene legkisebb
 * indexelheto es keresheto egysege a Document kozott. Minden alkalmazasnak el kell
 * keszitenie a konkret alkalmazas fuggo Mapper implementalo komponenset.
 * @author hp
 *
 */
public interface Mapper {
    public String ROLE = Mapper.class.getName();

    public org.apache.lucene.document.Document toLucene(Document d);
    
    public Document toResource(org.apache.lucene.document.Document luceneDocument);
}

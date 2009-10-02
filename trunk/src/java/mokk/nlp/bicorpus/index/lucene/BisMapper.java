/*
 * Created on Nov 28, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package mokk.nlp.bicorpus.index.lucene;

import mokk.nlp.bicorpus.BiSentence;
import mokk.nlp.bicorpus.Source;
import mokk.nlp.bicorpus.SourceDB;
import mokk.nlp.irutil.lucene.Mapper;

import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.activity.Initializable;
import org.apache.avalon.framework.component.Component;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;


/*
 * Az a komponens, ami egy BiSentence objektumot atalakit Lucene indexelhetove.
 * Sajnos a Lucene szarul van megtervezve, mert elore el kell kesziteni egy
 * Document objektumot, amire o meghivja az egyetlen Analyzerunket. Igy mezo
 * nevekbe kell bekodolni, hogy hogyan is tokenizalni a dolgokat
 * 
 * A bimondat ket stringbol all. Ezekbol egy egyszeru tokenized, stored field
 * lesz, hogy kereses utan meg tudjuk mutatni a mondatot. Ket masik mezoben
 * ugyanezen ket mondat stemmelt valtozata kerul. Hogy miert rakjuk kulon mezobe?
 * Mert a lucene nem kepes token es token kozott kulonbseget tenni, es mi szeretnenk
 * neha stemmeletlenul is keresni.
 * 
 * SourceId siman megy untokenized, stored, indexed, a sentenceId untokenized,
 * stored, unindexed, mert nincs ertelme mondatra keresni (egyenlore).
 *  
 * Ez az implementacio egy singleton avalon komponens.
 *  
 * @author hp
 *
 */

/**
 * @avalon.component
 * @avalon.service type=mokk.nlp.irutil.lucene.Mapper
 * @x-avalon.info name=bis-mapper
 * @x-avalon.lifestyle type="singleton"
 */
public class BisMapper implements Mapper, Component, Serviceable, Initializable, Disposable {
	
    /*
     * TODO: configuracioba vele
     */
    private static String leftFieldName = "left";
    private static String leftStemmedFieldName = "left_stemmed";
    private static String rightFieldName = "right";
    private static String rightStemmedFieldName = "right_stemmed";
	

	
	ServiceManager manager = null;
	
	SourceDB sourceDb = null;
	/**
	 * @avalon.dependency type="mokk.nlp.bicorpus.SourceDB"
	 */
	public void service(ServiceManager manager) throws ServiceException {
		this.manager = manager;

	}
	

    public void initialize() throws Exception {
        sourceDb = (SourceDB) manager.lookup(SourceDB.ROLE);
        
    }
	 
    public void dispose() {
        if(sourceDb != null) {
            manager.release(sourceDb);
        }
        
    }

    public org.apache.lucene.document.Document toLucene(mokk.nlp.irutil.Document doc) {

        BiSentence bis = (BiSentence) doc;
		Document d = new Document();
		
		/**
		 * String name,
             String string,
             boolean store,
             boolean index,
             boolean token
		 */
		d.add( new Field(leftFieldName, bis.getLeftSentence(),Field.Store.YES, Field.Index.TOKENIZED, Field.TermVector.WITH_OFFSETS));
		d.add( new Field(leftStemmedFieldName, bis.getLeftSentence(), Field.Store.NO, Field.Index.TOKENIZED, Field.TermVector.WITH_OFFSETS));
		d.add( new Field(rightFieldName, bis.getRightSentence(),Field.Store.YES, Field.Index.TOKENIZED, Field.TermVector.WITH_OFFSETS));
		d.add( new Field(rightStemmedFieldName, bis.getRightSentence(), Field.Store.NO, Field.Index.TOKENIZED, Field.TermVector.WITH_OFFSETS));
		
		Source source;
	
		for(source = bis.getSource(); source != null; source = source.getParent()) {
		    
		    d.add(  new Field("source", source.getId(),Field.Store.YES, Field.Index.UN_TOKENIZED, Field.TermVector.NO));
		}
		d.add(  new Field("sen", bis.getSenId(),Field.Store.YES, Field.Index.NO, Field.TermVector.NO));
		
		
		return d;
	}
	
    public mokk.nlp.irutil.Document toResource(org.apache.lucene.document.Document luceneDocument) {
		String left = luceneDocument.getField(leftFieldName).stringValue();
		String right = luceneDocument.getField(rightFieldName).stringValue();
		String sourceId = luceneDocument.getField("source").stringValue();
		
		// a leghosszabb sourceId az aktualis, es mivel ugyanaz a prefixuk, maximumot kell keresni
		Source source = null;
		String[] sources = luceneDocument.getValues("source");
		int deepeth = -1;
		if(sources.length == 0) {
		    source = Source.UNKNOWN_SOURCE;
		} else {
		    	
		 
		    for(int i = 0; i < sources.length; i++)  {
		    //    System.out.println(sources[i]);
		        Source s = sourceDb.get( sources[i]);
		        if(s != null) {
		      //      System.out.println(s.getId() + " " + s.getLevel());
		            if(s.getLevel() > deepeth ) {
		                source = s;
		                deepeth = s.getLevel();
		            }
		        }
		    }
		    
		  
		    if(source == null) {
		        source = Source.UNKNOWN_SOURCE;
		    }
		}
		
		String senId = luceneDocument.getField("sen").stringValue();
		return new BiSentence(source, senId, left, right);
	}



  
  
}

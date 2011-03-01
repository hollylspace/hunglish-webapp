package mokk.nlp.irutil.lucene.analysis;

import org.apache.lucene.analysis.Analyzer;
/*
 * A Lucene Analyzerbol nem csinalhato egyszeruen Avalon komponens,
 * mert nem interfesz, hanem abstract osztaly. Ezert Avalon
 * komponenseknek egy AnalyzerFactory peldanyt adunk at, ami
 * legyartja a konkret analyzert.
 * 
 * @author hp
 */
public interface AnalyzerFactory {

	public  String ROLE = AnalyzerFactory.class.getName();
	
    /*
     * Legyart egy analyzert
     */
	public Analyzer getAnalyzer();
}

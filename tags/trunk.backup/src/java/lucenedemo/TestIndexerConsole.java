package lucenedemo;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import mokk.nlp.bicorpus.index.lucene.BisMapper;
import mokk.nlp.irutil.lucene.analysis.LemmatizerWrapper;
import mokk.nlp.irutil.lucene.analysis.StemmerAnalyzer;
import net.sf.jhunlang.jmorph.analysis.Analyser;
import net.sf.jhunlang.jmorph.analysis.AnalyserContext;
import net.sf.jhunlang.jmorph.analysis.AnalyserControl;
import net.sf.jhunlang.jmorph.factory.Definition;
import net.sf.jhunlang.jmorph.factory.JMorphFactory;
import net.sf.jhunlang.jmorph.lemma.Lemmatizer;
import net.sf.jhunlang.jmorph.parser.ParseException;
import net.sf.jhunlang.jmorph.sample.AnalyserConfig;
import net.sf.jhunlang.jmorph.sword.parser.EnglishAffixReader;
import net.sf.jhunlang.jmorph.sword.parser.EnglishReader;
import net.sf.jhunlang.jmorph.sword.parser.SwordAffixReader;
import net.sf.jhunlang.jmorph.sword.parser.SwordReader;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.SimpleFSDirectory;
import org.apache.lucene.util.Version;

public class TestIndexerConsole {
	public static String encoding = "ISO-8859-2";

	String indexDir = "C:\\workspaces\\hunglish-webapp\\trunk\\index";

	private String huAff = "C:\\workspaces\\hunglish-webapp\\trunk\\data\\jmorph\\mispellRC1.aff";
	private String huDic = "C:\\workspaces\\hunglish-webapp\\trunk\\data\\jmorph\\mispellRC1.dic";
	private String huDerivatives = "C:\\workspaces\\hunglish-webapp\\trunk\\data\\jmorph\\derivatives.lst";
	private String huCompounds = "C:\\workspaces\\hunglish-webapp\\trunk\\data\\jmorph\\compound.lst";
	private int huRecursionDepth = 4;
	private String enAff = "C:\\workspaces\\hunglish-webapp\\trunk\\data\\jmorph\\en.aff";
	private String enDic = "C:\\workspaces\\hunglish-webapp\\trunk\\data\\jmorph\\en.dic";
	private int enRecursionDepth = 2;

	Map<String, LemmatizerWrapper> lemmatizerMap;	
	/**
	 * The left lemmatizer
	 */
	private Lemmatizer leftLemmatizer;
	
	
	/**
	 * Whether the tokenfilter should return original word 
	 */
	//private boolean returnOrig = false;
	
	/**
	 * Whether the tokenfilter should return OOV words 
	 */
	private boolean leftReturnOOVs;
	/**
	 * Must the tokenfilter append the POS to the term.
	 */
	private boolean leftReturnPOS;
	/**
	 * Must the lemmatizer strip derivates
	 */
	private boolean leftStripDerivates;
	
	/**
	 * The depth the analyser uses
	 */
	private int leftDepth;
	
	/**
	 * The analyser component used be the left lemmatization
	 */
	private Analyser leftAnalyser;
	
	/**
	 * Whether the tokenfilter should return OOV words 
	 */
	private boolean rightReturnOOVs;
	/**
	 * Must the tokenfilter append the POS to the term.
	 */
	private boolean rightReturnPOS;
	/**
	 * Must the lemmatizer strip derivates
	 */
	private boolean rightStripDerivates;
	
	/**
	 * The depth the analyser uses
	 */
	private int rightDepth;
	
	/**
	 * The analyser component used be the left lemmatization
	 */
	private Analyser rightAnalyser;
	
	/**
	 * The right lemmatizer
	 */
	private Lemmatizer rightLemmatizer;

	private boolean leftReturnOrig, rightReturnOrig;
	
	
	public static void main(String[] args) throws Exception {
		TestIndexerConsole instance = new TestIndexerConsole();
		instance.doJob();
	}

	private void configureAnalizer() {
		//setReturnOrig(true);
		
		setLeftStripDerivates(false);
		setLeftReturnOOVs(true);
		setLeftReturnOrig(true);
		setLeftReturnPOS(false);
		setLeftDepth(AnalyserControl.ALL_COMPOUNDS);

		setRightStripDerivates(false);
		setRightReturnOOVs(true);
		setRightReturnOrig(true);
		setRightReturnPOS(false);
		setRightDepth(AnalyserControl.ALL_COMPOUNDS);
	}


	private void initHuAnalyser() throws IOException,
			IllegalAccessException, InstantiationException,
			net.sf.jhunlang.jmorph.parser.ParseException {
		AnalyserConfig acHu = new AnalyserConfig();
		Definition affixDef = acHu.createDefinition(huAff, encoding,
				SwordAffixReader.class);
		Definition dicDef = acHu.createDefinition(huDic, encoding,
				SwordReader.class);
		acHu.setDerivativeFile(affixDef, huDerivatives);
		acHu.setRecursionDepth(affixDef, huRecursionDepth);
		acHu.setCompoundFile(affixDef, huCompounds);
		JMorphFactory jf = new JMorphFactory();
		Analyser analyserHu = jf.build(new Definition[] { affixDef, dicDef });
		setLeftAnalyser(analyserHu);
	}


	private void initEnAnalyser() throws IOException,
			IllegalAccessException, InstantiationException,
			net.sf.jhunlang.jmorph.parser.ParseException {
		AnalyserConfig acEn = new AnalyserConfig();
		Definition affixDef = acEn.createDefinition(enAff, encoding,
				EnglishAffixReader.class);
		Definition dicDef = acEn.createDefinition(enDic, encoding,
				EnglishReader.class);
		acEn.setRecursionDepth(affixDef, enRecursionDepth);
		JMorphFactory jf = new JMorphFactory();
		Analyser analyserHu = jf.build(new Definition[] { affixDef, dicDef });
		setRightAnalyser(analyserHu);
	}

	private void initLemmatizer() throws IOException, IllegalAccessException, InstantiationException, ParseException{
		configureAnalizer();
		initHuAnalyser();
		initEnAnalyser();
		AnalyserControl acLeft = new AnalyserControl(leftDepth);
		AnalyserContext analyserContextLeft = new AnalyserContext(acLeft);
		setLeftLemmatizer(new net.sf.jhunlang.jmorph.lemma.LemmatizerImpl(
				leftAnalyser, leftStripDerivates,
				analyserContextLeft));
		
		AnalyserControl acRight = new AnalyserControl(rightDepth);
		AnalyserContext analyserContextRight = new AnalyserContext(acRight);
		setRightLemmatizer(new net.sf.jhunlang.jmorph.lemma.LemmatizerImpl(
						rightAnalyser, rightStripDerivates,
						analyserContextRight));
	
		lemmatizerMap = new HashMap<String, LemmatizerWrapper>();
		LemmatizerWrapper leftLemmatizerWrapper = new LemmatizerWrapper();
		leftLemmatizerWrapper.setLemmatizer(leftLemmatizer);
		leftLemmatizerWrapper.setReturnOOVOrig(leftReturnOOVs);
		leftLemmatizerWrapper.setReturnOrig(leftReturnOrig);
		leftLemmatizerWrapper.setReturnPOS(leftReturnPOS);
		lemmatizerMap.put(BisMapper.leftStemmedFieldName, leftLemmatizerWrapper);

		LemmatizerWrapper rightLemmatizerWrapper = new LemmatizerWrapper();
		rightLemmatizerWrapper.setLemmatizer(rightLemmatizer);
		rightLemmatizerWrapper.setReturnOOVOrig(rightReturnOOVs);
		rightLemmatizerWrapper.setReturnOrig(rightReturnOrig);
		rightLemmatizerWrapper.setReturnPOS(rightReturnPOS);
		lemmatizerMap.put(BisMapper.rightStemmedFieldName, rightLemmatizerWrapper);
		
	}
	

	private void addDoc(IndexWriter iwriter, String texten, String texthu) throws CorruptIndexException, IOException{
		Document doc = new Document();
		doc.add(new Field(BisMapper.rightStemmedFieldName, texten,
				Field.Store.NO, Field.Index.ANALYZED));
		doc.add(new Field(BisMapper.rightFieldName, texten, Field.Store.YES,
				Field.Index.ANALYZED));
		doc.add(new Field(BisMapper.leftStemmedFieldName, texthu,
				Field.Store.NO, Field.Index.ANALYZED));
		doc.add(new Field(BisMapper.leftFieldName, texthu, Field.Store.YES,
				Field.Index.ANALYZED));
		iwriter.addDocument(doc);
	}
	
	private void search(IndexSearcher isearcher, Query query) throws IOException{
		System.out.println(">>>> new query:"+query.toString());
		Set<Term> terms = new HashSet<Term>();
		query.extractTerms(terms);
		for (Term term : terms) {
			System.out.println(term.toString() + "==" + term.text());
		}
		ScoreDoc[] hits = isearcher.search(query, null, 1000).scoreDocs;
		System.out.println("hits.length==" + hits.length);
		// Iterate through the results:
		for (int i = 0; i < hits.length; i++) {
			Document hitDoc = isearcher.doc(hits[i].doc);
			System.out.println("hitDoc==" + hitDoc.get(BisMapper.leftFieldName) + "~~" + hitDoc.get(BisMapper.rightFieldName));
		}		
	}
	
	/**
	 * http://hudson.zones.apache.org/hudson/job/Lucene-trunk/javadoc/core/index
	 * .html
	 * 
	 * @param args
	 * @throws Exception
	 */
	public void doJob() throws Exception {
		
		initLemmatizer();
		
		Analyzer analyzer = new StemmerAnalyzer(Version.LUCENE_CURRENT, lemmatizerMap);
		//Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_CURRENT);
		Directory directory = SimpleFSDirectory.open(new File(indexDir));
		System.out.println("directory opened:" + indexDir);
		IndexWriter iwriter = new IndexWriter(directory, analyzer, true,
				new IndexWriter.MaxFieldLength(25000));
		System.out.println("indexwriter opened");

		String texten = "This is the text to be indexed.";
		String texthu = "Ezeket a szavakat elmentjük az adatbázisba. Nem ismerjük a magyar nyelvet.";
		addDoc(iwriter, texten, texthu);

		//texten = "The Hungarian language is so nice.";
		//texthu = "Nincs rá szó, nincs rá fogalom. A magyar nyelv ezt nem tudja kezelni.";
		//addDoc(iwriter, texten, texthu);

		iwriter.close();
		System.out.println("indexwriter closed");

		// Now search the index:
		IndexSearcher isearcher = new IndexSearcher(directory, true); // read-only=true
		// Parse a simple query that searches for "text":

		QueryParser parser = new QueryParser(Version.LUCENE_CURRENT,
				BisMapper.leftStemmedFieldName, analyzer);
		Query query = parser.parse("szó"); // "text index"
		search(isearcher, query);

		parser = new QueryParser(Version.LUCENE_CURRENT,
				BisMapper.leftStemmedFieldName, analyzer);
		query = parser.parse("magyar"); 
		search(isearcher, query);
		
		
		parser = new QueryParser(Version.LUCENE_CURRENT,
				BisMapper.leftStemmedFieldName, analyzer);
		query = parser.parse("szavakat"); // "text index"
		search(isearcher, query);
		
		parser = new QueryParser(Version.LUCENE_CURRENT,
				BisMapper.rightStemmedFieldName, analyzer);
		query = parser.parse("index"); // "text index"
		search(isearcher, query);
		
		isearcher.close();
		directory.close();
	}

	public Lemmatizer getLeftLemmatizer() {
		return leftLemmatizer;
	}

	public void setLeftLemmatizer(Lemmatizer leftLemmatizer) {
		this.leftLemmatizer = leftLemmatizer;
	}

	//public boolean isReturnOrig() {
	//	return returnOrig;
	//}

	//public void setReturnOrig(boolean returnOrig) {
	//	this.returnOrig = returnOrig;
	//}

	public boolean isLeftReturnOOVs() {
		return leftReturnOOVs;
	}

	public void setLeftReturnOOVs(boolean leftReturnOOVs) {
		this.leftReturnOOVs = leftReturnOOVs;
	}

	public boolean isLeftReturnPOS() {
		return leftReturnPOS;
	}

	public void setLeftReturnPOS(boolean leftReturnPOS) {
		this.leftReturnPOS = leftReturnPOS;
	}

	public boolean isLeftStripDerivates() {
		return leftStripDerivates;
	}

	public void setLeftStripDerivates(boolean leftStripDerivates) {
		this.leftStripDerivates = leftStripDerivates;
	}

	public int getLeftDepth() {
		return leftDepth;
	}

	public void setLeftDepth(int leftDepth) {
		this.leftDepth = leftDepth;
	}

	public Analyser getLeftAnalyser() {
		return leftAnalyser;
	}

	public void setLeftAnalyser(Analyser leftAnalyser) {
		this.leftAnalyser = leftAnalyser;
	}

	public boolean isRightReturnOOVs() {
		return rightReturnOOVs;
	}

	public void setRightReturnOOVs(boolean rightReturnOOVs) {
		this.rightReturnOOVs = rightReturnOOVs;
	}

	public boolean isRightReturnPOS() {
		return rightReturnPOS;
	}

	public void setRightReturnPOS(boolean rightReturnPOS) {
		this.rightReturnPOS = rightReturnPOS;
	}

	public boolean isRightStripDerivates() {
		return rightStripDerivates;
	}

	public void setRightStripDerivates(boolean rightStripDerivates) {
		this.rightStripDerivates = rightStripDerivates;
	}

	public int getRightDepth() {
		return rightDepth;
	}

	public void setRightDepth(int rightDepth) {
		this.rightDepth = rightDepth;
	}

	public Analyser getRightAnalyser() {
		return rightAnalyser;
	}

	public void setRightAnalyser(Analyser rightAnalyser) {
		this.rightAnalyser = rightAnalyser;
	}

	public Lemmatizer getRightLemmatizer() {
		return rightLemmatizer;
	}

	public void setRightLemmatizer(Lemmatizer rightLemmatizer) {
		this.rightLemmatizer = rightLemmatizer;
	}

	public boolean isLeftReturnOrig() {
		return leftReturnOrig;
	}

	public void setLeftReturnOrig(boolean leftReturnOrig) {
		this.leftReturnOrig = leftReturnOrig;
	}

	public boolean isRightReturnOrig() {
		return rightReturnOrig;
	}

	public void setRightReturnOrig(boolean rightReturnOrig) {
		this.rightReturnOrig = rightReturnOrig;
	}

	public String getHuAff() {
		return huAff;
	}

	public void setHuAff(String huAff) {
		this.huAff = huAff;
	}

	public String getHuDic() {
		return huDic;
	}

	public void setHuDic(String huDic) {
		this.huDic = huDic;
	}

	public String getHuDerivatives() {
		return huDerivatives;
	}

	public void setHuDerivatives(String huDerivatives) {
		this.huDerivatives = huDerivatives;
	}

	public String getHuCompounds() {
		return huCompounds;
	}

	public void setHuCompounds(String huCompounds) {
		this.huCompounds = huCompounds;
	}

	public int getHuRecursionDepth() {
		return huRecursionDepth;
	}

	public void setHuRecursionDepth(int huRecursionDepth) {
		this.huRecursionDepth = huRecursionDepth;
	}

	public String getEnAff() {
		return enAff;
	}

	public void setEnAff(String enAff) {
		this.enAff = enAff;
	}

	public String getEnDic() {
		return enDic;
	}

	public void setEnDic(String enDic) {
		this.enDic = enDic;
	}

	public int getEnRecursionDepth() {
		return enRecursionDepth;
	}

	public void setEnRecursionDepth(int enRecursionDepth) {
		this.enRecursionDepth = enRecursionDepth;
	}

	public String getIndexDir() {
		return indexDir;
	}

	public void setIndexDir(String indexDir) {
		this.indexDir = indexDir;
	}

}

package hu.mokk.hunglish.lucene;

import hu.mokk.hunglish.jmorph.LemmatizerWrapper;
import hu.mokk.hunglish.lucene.analysis.StemmerAnalyzer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import net.sf.jhunlang.jmorph.analysis.Analyser;
import net.sf.jhunlang.jmorph.analysis.AnalyserContext;
import net.sf.jhunlang.jmorph.analysis.AnalyserControl;
import net.sf.jhunlang.jmorph.factory.Definition;
import net.sf.jhunlang.jmorph.factory.JMorphFactory;
import net.sf.jhunlang.jmorph.lemma.Lemma;
import net.sf.jhunlang.jmorph.lemma.Lemmatizer;
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
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.LockObtainFailedException;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;

public class HungarianLuceneTester {

	public static String resouceEncodingEn = "ISO-8859-1";
	public static String resouceEncodingHu = "ISO-8859-2";

//	private static String huAff = "/Users/daniel/experiments/hunglish-webapp/hunglish-webapp/resources-lang/jmorph/mispellRC1.aff";
//	private static String huDic = "/Users/daniel/experiments/hunglish-webapp/hunglish-webapp/resources-lang/jmorph/mispellRC1.dic";
//	private static String huDerivatives = "/Users/daniel/experiments/hunglish-webapp/hunglish-webapp/resources-lang/jmorph/derivatives.lst";
//	private static String huCompounds = "/Users/daniel/experiments/hunglish-webapp/hunglish-webapp/resources-lang/jmorph/compound.lst";
//	private static String enAff = "/Users/daniel/experiments/hunglish-webapp/hunglish-webapp/resources-lang/jmorph/en.aff";
//	private static String enDic = "/Users/daniel/experiments/hunglish-webapp/hunglish-webapp/resources-lang/jmorph/en.dic" ; 
//	private static String testDataQueryHu = "/Users/daniel/experiments/hunglish-webapp/hunglish-webapp/resources-lang/test-data.txt";
//	private static String testDataSenHu = "/Users/daniel/experiments/hunglish-webapp/hunglish-webapp/resources-lang/test-data-sen.txt";

	private static String huAff = "C:\\workspaces\\hunglish-webapp\\trunk\\data\\jmorph\\mispellRC1.aff";
	private static String huDic = "C:\\workspaces\\hunglish-webapp\\trunk\\data\\jmorph\\mispellRC1.dic";
	private static String huDerivatives = "C:\\workspaces\\hunglish-webapp\\trunk\\data\\jmorph\\derivatives.lst";
	private static String huCompounds = "C:\\workspaces\\hunglish-webapp\\trunk\\data\\jmorph\\compound.lst";
	private static int huRecursionDepth = 4;
	private static String enAff = "C:\\workspaces\\hunglish-webapp\\trunk\\data\\jmorph\\en.aff";
	private static String enDic = "C:\\workspaces\\hunglish-webapp\\trunk\\data\\jmorph\\en.dic";
	private static int enRecursionDepth = 2;
	private static String testDataQueryHu = "C:\\work\\hunmorph\\root-test\\resources-lang\\test-data.txt";

	private static int huRecursionDepth = 4;
	private static int enRecursionDepth = 2;

	public static String testDataEncodingHu = "UTF-8";
	List<String> huQueries ;

	private Analyzer analyzer;
	private Directory directory;
	private IndexWriter iwriter;
	private IndexSearcher isearcher;

	public static String huFieldName = "left";
	// public static String huStemmedFieldName = "left_stemmed";
	public static String enFieldName = "right";
	// public static String enStemmedFieldName = "right_stemmed";

	private Analyser huAnalyser;
	private Analyser enAnalyser;

	Map<String, LemmatizerWrapper> lemmatizerMap;

	/**
	 * @param args
	 * @throws IOException
	 * @throws LockObtainFailedException
	 * @throws CorruptIndexException
	 * @throws ParseException
	 * @throws net.sf.jhunlang.jmorph.parser.ParseException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 */
	public static void main(String[] args) throws CorruptIndexException,
			LockObtainFailedException, IOException, ParseException,
			IllegalAccessException, InstantiationException,
			net.sf.jhunlang.jmorph.parser.ParseException {

		HungarianLuceneTester instance = new HungarianLuceneTester();
		instance.justDoIt();
	}

	private void justDoIt() throws CorruptIndexException,
			LockObtainFailedException, IOException, ParseException,
			IllegalAccessException, InstantiationException,
			net.sf.jhunlang.jmorph.parser.ParseException {

		huQueries = getLines(testDataQueryHu, testDataEncodingHu);
		initLemmatizer();

		// analyzer = new StandardAnalyzer(Version.LUCENE_30);

		System.out.println("------init done---------------");
		analyzer = new StemmerAnalyzer(Version.LUCENE_CURRENT, lemmatizerMap);

		// Store the index in memory:
		directory = new RAMDirectory();
		// To store an index on disk, use this instead:
		// Directory directory = FSDirectory.open("/tmp/testindex");
		//analyzer = new StandardAnalyzer(Version.LUCENE_CURRENT);
		iwriter = new IndexWriter(directory, analyzer, true,
				new IndexWriter.MaxFieldLength(25000));
		Document doc = new Document();
		String enText = "This is the text to be index.";
		// List<String> huSens = getLines(testDataSenHu, testDataEncodingHu);
		doc.add(new Field(enFieldName, enText, Field.Store.YES,
				Field.Index.ANALYZED));
		doc.add(new Field(huFieldName, huQueries.get(0), Field.Store.YES,
				Field.Index.ANALYZED));
		//doc.add(new Field("vacak", "this must work", Field.Store.YES, Field.Index.NOT_ANALYZED));
		
		iwriter.addDocument(doc);
		iwriter.close();
		System.out.println("------indexing done---------------");

		// Now search the index:
		isearcher = new IndexSearcher(directory, true); // read-only=true

		query(enFieldName, "indexed", analyzer, isearcher);
		query(enFieldName, "index", analyzer, isearcher);
		//query("vacak", "must");
		//query("vacak", "nono");
		
		for (String query : huQueries) {
			query(huFieldName, query, analyzer, isearcher);
		}

		isearcher.close();
		directory.close();

	}

	public static void query(String fieldName, String term, Analyzer analyzerr, 
			IndexSearcher isearcherr) throws ParseException,
			IOException {
		QueryParser parser = new QueryParser(Version.LUCENE_30, fieldName,
				analyzerr);
		Query query = parser.parse(term);
		ScoreDoc[] hits = isearcherr.search(query, null, 1000).scoreDocs;
		System.out.println("searchd fieldName:" + fieldName + ";term:" + term + ";hits.length:" + hits.length);
		// org.junit.Assert.assertEquals(1, hits.length);
		// Iterate through the results:
		for (int i = 0; i < hits.length; i++) {
			Document hitDoc = isearcherr.doc(hits[i].doc);
			System.out
					.println("hitDoc.get(fieldname):" + hitDoc.get(fieldName));
			// org.junit.Assert.assertEquals(enText,
			// hitDoc.get(enFieldName));
		}
	}

	private void initHuAnalyser() throws IOException, IllegalAccessException,
			InstantiationException,
			net.sf.jhunlang.jmorph.parser.ParseException {
		AnalyserConfig acHu = new AnalyserConfig();
		Definition affixDef = acHu.createDefinition(huAff, resouceEncodingHu,
				SwordAffixReader.class);
		Definition dicDef = acHu.createDefinition(huDic, resouceEncodingHu,
				SwordReader.class);
		acHu.setDerivativeFile(affixDef, huDerivatives);
		acHu.setRecursionDepth(affixDef, huRecursionDepth);
		acHu.setCompoundFile(affixDef, huCompounds);
		JMorphFactory jf = new JMorphFactory();
		huAnalyser = jf.build(new Definition[] { affixDef, dicDef });
	}

	private void initEnAnalyser() throws IOException, IllegalAccessException,
			InstantiationException,
			net.sf.jhunlang.jmorph.parser.ParseException {
		AnalyserConfig acEn = new AnalyserConfig();
		Definition affixDef = acEn.createDefinition(enAff, resouceEncodingEn,
				EnglishAffixReader.class);
		Definition dicDef = acEn.createDefinition(enDic, resouceEncodingEn,
				EnglishReader.class);
		acEn.setRecursionDepth(affixDef, enRecursionDepth);
		JMorphFactory jf = new JMorphFactory();
		enAnalyser = jf.build(new Definition[] { affixDef, dicDef });
	}

	public void initLemmatizer() throws IOException, IllegalAccessException,
			InstantiationException, ParseException,
			net.sf.jhunlang.jmorph.parser.ParseException {
		initHuAnalyser();
		initEnAnalyser();
		AnalyserControl acHu = new AnalyserControl(
				AnalyserControl.ALL_COMPOUNDS);
		AnalyserContext analyserContextHu = new AnalyserContext(acHu);
		boolean huStripDerivates = true;
		Lemmatizer huLemmatizer = new net.sf.jhunlang.jmorph.lemma.LemmatizerImpl(
				huAnalyser, huStripDerivates, analyserContextHu);

		AnalyserControl acRight = new AnalyserControl(
				AnalyserControl.ALL_COMPOUNDS);
		AnalyserContext analyserContextRight = new AnalyserContext(acRight);
		boolean enStripDerivates = true;
		Lemmatizer enLemmatizer = new net.sf.jhunlang.jmorph.lemma.LemmatizerImpl(
				enAnalyser, enStripDerivates, analyserContextRight);

		System.out.println("en lemmatizer created---------------");
		testLemmatizer(enLemmatizer, "indexed");
		testLemmatizer(enLemmatizer, "tests");

		lemmatizerMap = new HashMap<String, LemmatizerWrapper>();

		LemmatizerWrapper huLemmatizerWrapper = new LemmatizerWrapper(huLemmatizer,false/*returnOOVOrig*/,true/*returnOrig*/,false/*returnPOS*/);
		lemmatizerMap.put(huFieldName, huLemmatizerWrapper);

		System.out.println("hu lemmatizer created--------------");

		if (huQueries != null) {
			for (String query : huQueries) {
				testLemmatizer(huLemmatizer, query);
			}
		}

		LemmatizerWrapper enLemmatizerWrapper = new LemmatizerWrapper(enLemmatizer,false/*returnOOVOrig*/,true/*returnOrig*/,false/*returnPOS*/);
		lemmatizerMap.put(enFieldName, enLemmatizerWrapper);
	}

	private void testLemmatizer(Lemmatizer lemmatizer, String term) {
		System.out.println("stems for:" + term);
		List<Lemma> lemmas = lemmatizer.lemmatize(term);
		for (Lemma lemma : lemmas) {
			System.out.println(lemma.getWord());
		}

	}

	private static List<String> getLines(String path, String encoding)
			throws FileNotFoundException {
		List<String> result = new ArrayList<String>();
		Scanner scanner = new Scanner(new File(path), encoding);
		try {
			// first use a Scanner to get each line
			while (scanner.hasNextLine()) {
				result.add(scanner.nextLine());
			}
		} finally {
			// ensure the underlying stream is always closed
			scanner.close();
		}
		return result;
	}

	public Map<String, LemmatizerWrapper> getLemmatizerMap() {
		return lemmatizerMap;
	}

	public void setLemmatizerMap(Map<String, LemmatizerWrapper> lemmatizerMap) {
		this.lemmatizerMap = lemmatizerMap;
	}

}

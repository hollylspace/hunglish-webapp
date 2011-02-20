/**
 * @author bpgergo
 *
 */
package hu.mokk.hunglish.lucene;

import hu.mokk.hunglish.domain.Bisen;
import hu.mokk.hunglish.lucene.query.QueryPhrase;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.util.Version;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@Configurable
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = 
	{"classpath:/META-INF/spring/applicationContext.xml", 
	"classpath:/test-applicationContext.xml"})
public class SearcherTest {

	@Autowired
	private Searcher searcher;

	@Autowired
	private TestData testData;

	@Test
	public void testIndexSearcher() throws CorruptIndexException, IOException, ParseException {
		IndexSearcher is = searcher.getSearcher();
		System.out.println("maxDox:"+is.maxDoc());
		
		for (int i = 0; i < 10; i++){
			System.out.println(is.doc(i));
		}
		System.out.println("------------");
		
		String term = "about"; String field = "enSenStemmed";
		Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_CURRENT); 
		QueryParser qp = new QueryParser(Version.LUCENE_CURRENT, field, analyzer);
		Query query = qp.parse(term);
		ScoreDoc[] scs = is.search(query, 10).scoreDocs;
		System.out.println("search results count for term:"+term+" and in the field:"+field+" =>"+scs.length+" list of results:");
		for (ScoreDoc scoreDoc : scs){
			System.out.println(is.doc(scoreDoc.doc));			
		}
	}
	
	List<String> testQueryListHu;
	List<String> testQueryListEn;
	
	@Test
	public void testSearch2Sides() throws FileNotFoundException {
		testQueryListHu = getLines(testData.testQueriesHu, testData.testDataEncoding);
		testQueryListEn = getLines(testData.testQueriesEn, testData.testDataEncoding);
		
		String useTheHunglishSyn = "true";
		System.out.println("+++++++++++++++++++++++++++++++++++++++++++++++++++++useTheHunglishSyn:"+useTheHunglishSyn);
		//searcher.setUseHunglishSyntax(useTheHunglishSyn);
		testSearch();
		
		useTheHunglishSyn = "false";
		System.out.println("+++++++++++++++++++++++++++++++++++++++++++++++++++++useTheHunglishSyn:"+useTheHunglishSyn);
		//searcher.setUseHunglishSyntax(useTheHunglishSyn);
		testSearch();
		
	}
	
	private void testSearch() throws FileNotFoundException {		
		for ( String query : testQueryListHu ) {
			runSingleSearch(query, QueryPhrase.Field.HU);
		}
		for ( String query : testQueryListEn ) {
			runSingleSearch(query, QueryPhrase.Field.EN);
		}		
	}

	private void runMultiSearch(String enQuery, QueryPhrase.Field enField, 
			String huQuery, QueryPhrase.Field huField){		
	}
	
	private void runSingleSearch(String query, QueryPhrase.Field field){
		System.out.println("-------------------------Test searchin:"+query);
		try {
			SearchRequest request = new SearchRequest();
			if (QueryPhrase.Field.HU.equals(field)){
				request.setHuQuery(query);
			} else {
				request.setEnQuery(query);
			}
			request.setMaxResults(10);
			SearchResult searchResult = searcher.search(request);
			List<Bisen> hits = searchResult.getHitList();
			printList(hits);
			
		} catch (Exception e) {
			System.err.println("Exception when searchin:"+query);
			e.printStackTrace();
		}
	}
	
	
	private static void printList(List list){
		System.out.println("printList size"+list.size());
		for (Object o : list){
			System.out.println(o);
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


}

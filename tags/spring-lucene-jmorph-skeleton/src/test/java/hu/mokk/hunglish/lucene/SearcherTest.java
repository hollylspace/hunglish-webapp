/**
 * @author bpgergo
 *
 */
package hu.mokk.hunglish.lucene;

import hu.mokk.hunglish.domain.Bisen;

import java.util.List;
import java.util.ArrayList;
import java.io.FileNotFoundException;
import java.io.File;
import java.util.Scanner;


import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@Configurable
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:/META-INF/spring/applicationContext.xml")
public class SearcherTest {

	@Autowired
	private Searcher searcher;

	@Autowired
	private TestData testData;

	@Test
	public void testSearch() throws FileNotFoundException {
		
		List<String> testQueryListHu = getLines(testData.testQueriesHu, testData.testDataEncoding);
		List<String> testQueryListEn = getLines(testData.testQueriesEn, testData.testDataEncoding);

		for ( String query : testQueryListHu ) {
			SearchRequest request = new SearchRequest();
			request.setLeftQuery(query);
			request.setMaxResults(10);
			SearchResult searchResult = searcher.search(request);
			List<Bisen> hits = searchResult.getHitList();
			printList(hits);
		}
		for ( String query : testQueryListEn ) {
			SearchRequest request = new SearchRequest();
			request.setRightQuery(query);
			request.setMaxResults(10);
			SearchResult searchResult = searcher.search(request);
			List<Bisen> hits = searchResult.getHitList();
			printList(hits);
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

/**
 * @author bpgergo
 *
 */
package hu.mokk.hunglish.lucene;

import hu.mokk.hunglish.domain.Bisen;

import java.util.List;

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

	@Test
	public void testSearch() {
		SearchRequest request = new SearchRequest();
		request.setLeftQuery("nem");
		request.setMaxResults(10);
		SearchResult searchResult = searcher.search(request);
		List<Bisen> hits = searchResult.getHitList();
		System.out.println(Integer.toString(hits.size())+" results returned by searcher.");			
		for (Bisen hit : hits) {
			System.out.println(hit.getEnSentence());			
		}
		System.out.println("Wow, testSearch() finished without run-time errors.");
	}

}

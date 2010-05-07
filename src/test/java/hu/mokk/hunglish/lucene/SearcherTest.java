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
		request.setLeftQuery("katonai");
		request.setMaxResults(10);
		SearchResult searchResult = searcher.search(request);
		List<Bisen> hits = searchResult.getHitList();
		printList(hits);
		
		request = new SearchRequest();
		request.setRightQuery("keep");
		request.setMaxResults(10);

		searchResult = searcher.search(request);
		hits = searchResult.getHitList();
		printList(hits);
		
	}

	private static void printList(List list){
		System.out.println("printList size"+list.size());
		for (Object o : list){
			System.out.println(o);
		}
	}
}

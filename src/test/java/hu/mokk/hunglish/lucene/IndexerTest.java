/**
 * 
 */
package hu.mokk.hunglish.lucene;

import hu.mokk.hunglish.domain.Bisen;

import java.io.IOException;
import java.util.List;

import net.sf.jhunlang.jmorph.parser.ParseException;

import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.store.LockObtainFailedException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * @author bpgergo
 *
 */
@Configurable
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:/META-INF/spring/applicationContext.xml")
public class IndexerTest {

	@Autowired
	Indexer indexer;
	
	@Autowired
	private TestData testData;

	@Test
	public void testIndex() throws CorruptIndexException, LockObtainFailedException, IOException, IllegalAccessException, InstantiationException, ParseException {
		indexer.deleteTmpDirectory();
		//indexer.indexDoc(new Long(2), true/*true=temp that is index will be created in hunglishIndexTmp, false=main*/);
//		indexer.mergeTmpIndex();
	}
	
	
}

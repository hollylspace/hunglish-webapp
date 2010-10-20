package hu.mokk.hunglish.lucene;

import java.io.IOException;

import net.sf.jhunlang.jmorph.parser.ParseException;

import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.store.LockObtainFailedException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class Launcher {

	/**
	 * @param args
	 * @throws ParseException 
	 * @throws InstantiationException 
	 * @throws IllegalAccessException 
	 * @throws IOException 
	 * @throws LockObtainFailedException 
	 * @throws CorruptIndexException 
	 */
	public static void main(String[] args) throws CorruptIndexException, LockObtainFailedException, IOException, IllegalAccessException, InstantiationException, ParseException {
		ApplicationContext context = new ClassPathXmlApplicationContext(
				new String[] { "applicationContext.xml",
						"applicationContext-security.xml" });
		
		Indexer indexer = (Indexer)context.getBean("indexer");
		indexer.indexAll(true/*true=temp that is index will be created in hunglishIndexTmp, false=main*/);
		indexer.mergeTmpIndex();
		
	}

}

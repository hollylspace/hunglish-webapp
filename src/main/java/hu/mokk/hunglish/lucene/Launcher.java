package hu.mokk.hunglish.lucene;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class Launcher {

	/**
	 * command line interface for the spellcheck index builder
	 * 
	 */
	public static void main(String[] args) {
		try {
			ApplicationContext context = new ClassPathXmlApplicationContext(
					new String[] { "applicationContext.xml" });
			
			Indexer indexer = (Indexer)context.getBean("indexer");
			indexer.reBuildSpellIndex();
		} catch (Exception e) {
			System.err.println("------------FATAL ERROR------------");
			e.printStackTrace();
			System.exit(-1);
		}
	}

}

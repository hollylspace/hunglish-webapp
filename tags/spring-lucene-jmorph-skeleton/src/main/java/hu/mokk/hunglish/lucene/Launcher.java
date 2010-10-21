package hu.mokk.hunglish.lucene;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class Launcher {

	/**
	 * command line interface for the harness pipeline
	 * this will be the last program executed by 
	 * src/main/python/control_harness.py
	 * 
	 */
	public static void main(String[] args) {
		try {
			ApplicationContext context = new ClassPathXmlApplicationContext(
					new String[] { "applicationContext.xml" });
			
			Indexer indexer = (Indexer)context.getBean("indexer");
			indexer.indexAll(true/*true=temp that is index will be created in hunglishIndexTmp, false=main*/);
			indexer.mergeTmpIndex();
		} catch (Exception e) {
			System.err.println("------------FATAL ERROR------------");
			e.printStackTrace();
			System.exit(-1);
		}
	}

}

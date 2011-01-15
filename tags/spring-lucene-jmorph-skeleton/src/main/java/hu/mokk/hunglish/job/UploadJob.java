package hu.mokk.hunglish.job;

import hu.mokk.hunglish.lucene.Indexer;
import hu.mokk.hunglish.lucene.Searcher;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class UploadJob implements Job {
	private Log log = LogFactory.getLog(UploadJob.class);

	// @ Autowired
	// private Indexer indexer;

	// @ Autowired
	// private Searcher searcher;

	private ApplicationContext getContext() {
		ApplicationContext context = new ClassPathXmlApplicationContext(
				new String[] { "applicationContext.xml" });
		return context;
	}

	@Override
	public void execute(JobExecutionContext jc) throws JobExecutionException {
		try {
			ApplicationContext context = getContext();
			Indexer indexer = (Indexer) context.getBean("indexer");
			Searcher searcher = (Searcher) context.getBean("searcher");

			log.debug("Executing upload processing job ... ");
			if (indexer != null && searcher != null) {
				SystemCall.execute(indexer.getUploadJobPath());
				indexer.indexAll();
				searcher.reInitSearcher();
			} else {
				log.error("WTF, indexer or searcher is null! ");
			}
			log.debug("Executed upload processing job!");
		} catch (Exception e) {
			log.error("Failed to execute upload processing job.", e);
		}
	}

}

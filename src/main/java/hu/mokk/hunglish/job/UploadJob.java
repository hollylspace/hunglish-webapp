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
	private static int HARNESS_SUCCESS_CODE = 0;
	
	@Override
	public void execute(JobExecutionContext jc) throws JobExecutionException {
		/*
		try {
			Indexer indexer = Indexer.getInstance();
			Searcher searcher = Searcher.getInstance();
			log.debug("Executing upload processing job ... ");
			if (indexer != null && searcher != null) {
				//if harness exit status is not OK, then do not start indexer
				//I just put it here, so when, we change this to asynchronous processing,  
				//then this logic should be implemented
				int exitCode = SystemCall.execute(indexer.getUploadJobPath());
				log.debug("harness job finished, exit code:"+exitCode);
				if (HARNESS_SUCCESS_CODE == exitCode){
					indexer.indexAll();
					searcher.reInitSearcher();
				}
			} else {
				log.error("WTF, indexer or searcher is null! ");
			}
			log.debug("Executed upload processing job!");
		} catch (Exception e) {
			log.error("Failed to execute upload processing job.", e);
		} */
	}

}

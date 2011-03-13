package hu.mokk.hunglish.job;

import hu.mokk.hunglish.lucene.Indexer;
import hu.mokk.hunglish.lucene.Searcher;

import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.StatefulJob;
import org.springframework.scheduling.quartz.QuartzJobBean;

public class MockJob { // extends QuartzJobBean implements StatefulJob {
	transient private static Log logger = LogFactory.getLog(MockJob.class);
	private static int HARNESS_SUCCESS_CODE = 0;
	
	public void doSomeAction() {

		logger.info("mokk-job doing some action ...");
		
		try {
			Indexer indexer = Indexer.getInstance();
			Searcher searcher = Searcher.getInstance();
			logger.debug("Executing upload processing job ... ");
			if (indexer != null && searcher != null) {
				//if harness exit status is not OK, then do not start indexer
				//I just put it here, so when, we change this to asynchronous processing,  
				//then this logic should be implemented
				int exitCode = SystemCall.execute(indexer.getUploadJobPath());
				logger.info("harness job finished, exit code:"+exitCode);
				if (HARNESS_SUCCESS_CODE == exitCode){
					indexer.indexAll();
					searcher.reInitSearcher();
				}
			} else {
				logger.error("WTF, indexer or searcher is null! ");
			}
			logger.debug("Executed upload processing job!");
		} catch (Exception e) {
			logger.error("Failed to execute upload processing job.", e);
		}
		
		
	}

	/*
	@Override
	protected void executeInternal(JobExecutionContext arg0)
			throws JobExecutionException {

		logger.info("job running...");

	}
	*/
}

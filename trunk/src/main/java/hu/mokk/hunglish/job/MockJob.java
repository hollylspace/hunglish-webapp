package hu.mokk.hunglish.job;

import hu.mokk.hunglish.domain.Upload;
import hu.mokk.hunglish.lucene.Indexer;
import hu.mokk.hunglish.lucene.Searcher;

import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.StatefulJob;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.quartz.QuartzJobBean;

public class MockJob { // extends QuartzJobBean implements StatefulJob {
	transient private static Log logger = LogFactory.getLog(MockJob.class);
	private static int HARNESS_SUCCESS_CODE = 0;

	@Autowired
	private TaskExecutor singleThreadExecutor;

	//@Scheduled(cron = "0 * * * * ?")
	public void doItPeriodically() {
		logger.info("trying to run doSomeAction() periodically ...");
		MyTask myTask = new MyTask();
		singleThreadExecutor.execute(myTask);
	}

	private class MyTask implements Runnable {

		@Override
		public void run() {
			doSomeAction();

		}

	}

	public void doItOnDemand() {
		logger.info("trying to run doSomeAction() on-demand ...");
		MyTask myTask = new MyTask();
		singleThreadExecutor.execute(myTask);
	}

	public void doSomeAction() {

		logger.info("mokk-job doing some action ...");

		try {
			Indexer indexer = Indexer.getInstance();
			Searcher searcher = Searcher.getInstance();
			
			if (indexer != null && searcher != null) {
				int exitCode = HARNESS_SUCCESS_CODE;
				long cnt = Upload.countUnprocessedUploads();
				
				logger.info("Executing upload processing job, upload count:"+cnt);
				if (cnt > 0 ){
					logger.info("harness job start ...:"+indexer.getUploadJobPath());
					exitCode = SystemCall.execute(indexer.getUploadJobPath());
					logger.info("harness job finished, exit code:" + exitCode);
				}
				if (HARNESS_SUCCESS_CODE == exitCode) {
					indexer.indexAll();
					searcher.reInitSearcher();
				}
				
			} else {
				logger.error("WTF, indexer or searcher is null! ");
			}
			logger.info("Executed upload processing job!");
		} catch (Exception e) {
			logger.error("Failed to execute upload processing job.", e);
		}

	}

}

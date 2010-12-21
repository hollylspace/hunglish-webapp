package hu.mokk.hunglish.job;

import hu.mokk.hunglish.lucene.Indexer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;

public class UploadJob implements Job {
	private Log log = LogFactory.getLog(UploadJob.class);
	@Autowired
	private Indexer indexer;

	@Override
	public void execute(JobExecutionContext jc) throws JobExecutionException {
		try {
			log.debug("Executing upload processing job ... ");
			SystemCall.execute(indexer.getUploadJobPath());
			log.debug("Executed upload processing job!");			
		} catch (Exception e) {
			log.error("Failed to execute upload processing job.", e);
		}
	}

}

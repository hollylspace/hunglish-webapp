package hu.mokk.hunglish.web;

import hu.mokk.hunglish.domain.Bisen;
import hu.mokk.hunglish.lucene.Indexer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.SchedulerException;
import org.springframework.transaction.annotation.Transactional;

public class UpvoteController {
	transient private static Log logger = LogFactory.getLog(UpvoteController.class);

	private void startUploadJob() throws SchedulerException {
		logger.info("trying to run job on demand...");
		Indexer.getInstance().getMockJob().doItOnDemand();
	}
	
	@Transactional
	public void upvote(Long id) throws SchedulerException{
		Bisen.upvote(id);
		logger.info("upvoted:"+id);
		startUploadJob();		
	}
	
	@Transactional
	public void downvote(Long id) throws SchedulerException{
		Bisen.downvote(id);
		logger.info("downvoted:"+id);
		startUploadJob();
	}
	
}

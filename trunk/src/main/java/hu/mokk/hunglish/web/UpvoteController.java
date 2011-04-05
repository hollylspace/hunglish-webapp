package hu.mokk.hunglish.web;

import hu.mokk.hunglish.domain.Bisen;
import hu.mokk.hunglish.job.MockJob;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

public class UpvoteController {
	transient private static Log logger = LogFactory.getLog(UpvoteController.class);

	@Autowired
	private MockJob mockJob;

	public void setMockJob(MockJob mockJob) {
		this.mockJob = mockJob;
	}

	
	private void startUploadJob() throws SchedulerException {
		logger.info("trying to run job on demand...");
		mockJob.doItOnDemand();
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

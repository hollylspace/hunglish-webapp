package hu.mokk.hunglish.web;

import hu.mokk.hunglish.domain.Bisen;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.transaction.annotation.Transactional;

public class UpvoteController {
	transient private static Log logger = LogFactory.getLog(UpvoteController.class);
	
	@Transactional
	public void upvote(Long id){
		Bisen.upvote(id);
		logger.info("upvoted:"+id);
	}
	
	@Transactional
	public void downvote(Long id){
		Bisen.downvote(id);
		logger.info("downvoted:"+id);
	}
	
}

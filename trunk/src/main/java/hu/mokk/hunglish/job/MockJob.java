package hu.mokk.hunglish.job;

import java.util.Date;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.StatefulJob;
import org.springframework.scheduling.quartz.QuartzJobBean;

public class MockJob { // extends QuartzJobBean implements StatefulJob {

	public void doSomeAction() {

		System.out.println(new Date().toString() + " - doing action...");
	}

	/*
	@Override
	protected void executeInternal(JobExecutionContext arg0)
			throws JobExecutionException {

		System.out.println("job running...");

	}
	*/
}

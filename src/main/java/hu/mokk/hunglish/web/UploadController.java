package hu.mokk.hunglish.web;

import hu.mokk.hunglish.domain.Author;
import hu.mokk.hunglish.domain.Genre;
import hu.mokk.hunglish.domain.Upload;
import hu.mokk.hunglish.job.UploadJob;
import hu.mokk.hunglish.lucene.Indexer;

import hu.mokk.hunglish.job.MockJob;

import java.io.File;
import java.sql.Timestamp;
import java.util.Date;

import javax.validation.Valid;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SimpleTrigger;
import org.quartz.Trigger;
import org.quartz.impl.StdSchedulerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.roo.addon.web.mvc.controller.RooWebScaffold;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@RooWebScaffold(path = "upload", automaticallyMaintainView = true, formBackingObject = Upload.class)
@RequestMapping("/upload/**")
@Controller
public class UploadController {

	transient private static Log logger = LogFactory
			.getLog(UploadController.class);

	@Autowired
	private Indexer indexer;

	@Autowired
	private Scheduler scheduler;
	
	@Autowired
	private JobDetail jobDetail;

	public void setJobDetail(JobDetail jobDetail) {
		this.jobDetail = jobDetail;
	}

	public void setScheduler(Scheduler scheduler) {
		this.scheduler = scheduler;
	}
	


	private String getUploadDir() {
		String uploadDir = indexer.getUploadDir();
		File dummy = null;
		try {
			dummy = new File(uploadDir);
		} catch (Exception e) {
			logger.error("uploaddir problem:" + uploadDir, e);
		}
		if ((dummy == null) || (!dummy.isDirectory())) {
			throw new IllegalArgumentException(
					"An upload directory is required");
		}
		return uploadDir;
	}

	private void startUploadJob() throws SchedulerException {
		// get the Quartz scheduler
		Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();

		// Define job instance
		JobDetail job = new JobDetail("job1", "group1", UploadJob.class);

		// Define a Trigger that will fire "now"
		Trigger trigger = new SimpleTrigger("trigger1", "group1", new Date());

		if (!scheduler.isStarted()) {
			scheduler.start();
		}

		// Schedule the job with the trigger
		logger.info("scheduling upload job ...");
		scheduler.scheduleJob(job, trigger);

		// scheduler.standby();
	}

	@RequestMapping(value = "/upload", method = RequestMethod.POST)
	public String create(@Valid Upload upload, BindingResult result,
			ModelMap modelMap) {
		if (upload == null)
			throw new IllegalArgumentException("An upload is required");

		//MockJob job = new MockJob();
		//job.doSomeAction();
		
		long startTime = System.currentTimeMillis();
		SimpleTrigger trigger  =  new SimpleTrigger("mySimpleTrigger", scheduler.DEFAULT_GROUP, new Date(startTime), null, 0, 0L);

        try {
        	System.out.println("triggered job manually.");
			scheduler.scheduleJob(jobDetail, trigger);
		} catch (SchedulerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// if (result.hasErrors()) {
		// for (ObjectError r : result.getAllErrors()){
		// //TODO
		// }
		// modelMap.addAttribute("upload", upload);
		// modelMap.addAttribute("authors", Author.findAllAuthorsWithDummy());
		// modelMap.addAttribute("genres", Genre.findAllGenresNoDummy());
		// return "upload/create";
		// }
		// String uploadDir = getUploadDir();
		// try{
		// upload.setHuOriginalFileName(upload.getHuFileData().getOriginalFilename());
		// upload.setEnOriginalFileName(upload.getEnFileData().getOriginalFilename());
		// upload.setHuOriginalFileSize(upload.getHuFileData().getSize());
		// upload.setEnOriginalFileSize(upload.getEnFileData().getSize());
		// upload.setCreatedTimestamp(new Timestamp((new Date()).getTime()));
		// upload.setIsProcessed("N");
		// upload.setApproved("N");
		// upload.setCopyright("C");
		//
		// upload.validate();
		//
		// upload.setOldDocid("");//not null for hunglish1, empty string for
		// hunglish2
		//
		// upload.persist();
		// String huFilePath = uploadDir+File.separator +
		// upload.getId()+".hu."+upload.getHuExtension();
		// File huFile = new File(huFilePath);
		// upload.getHuFileData().transferTo(huFile);
		// upload.setHuUploadedFilePath(huFile.getCanonicalPath());
		//
		// String enFilePath = uploadDir+File.separator +
		// upload.getId()+".en."+upload.getEnExtension();
		// File enFile = new File(enFilePath);
		// upload.getEnFileData().transferTo(enFile);
		// upload.setEnUploadedFilePath(enFile.getCanonicalPath());
		// upload.merge();
		// logger.info("upload saved in db. starting upload job ...");
		// startUploadJob();
		// logger.info("upload job started.");
		// } catch (Exception e) {
		// if (upload.getId() != null){
		// upload.remove();
		// }
		// result.reject("ERROR", e.getLocalizedMessage());
		// //TODO
		// logger.error("Error while creating upload", e);
		// //throw new IllegalArgumentException(e);
		// return "upload/create";
		// }
		// return "redirect:/upload/" + upload.getId();
		return "upload/create";
	}

	@RequestMapping(value = "/upload/form", method = RequestMethod.GET)
	public String createForm(ModelMap modelMap) {
		modelMap.addAttribute("upload", new Upload());
		modelMap.addAttribute("authors", Author.findAllAuthorsWithDummy());
		modelMap.addAttribute("genres", Genre.findAllGenresNoDummy());
		return "upload/create";
	}

	@RequestMapping(value = "/upload/{id}", method = RequestMethod.GET)
	public String show(@PathVariable("id") Long id, ModelMap modelMap) {
		if (id == null)
			throw new IllegalArgumentException("An Identifier is required");
		modelMap.addAttribute("upload", Upload.findUpload(id));
		return "upload/show";
	}

	@RequestMapping(value = "/upload", method = RequestMethod.GET)
	public String list(
			@RequestParam(value = "page", required = false) Integer page,
			@RequestParam(value = "size", required = false) Integer size,
			ModelMap modelMap) {
		if (page != null || size != null) {
			int sizeNo = size == null ? 10 : size.intValue();
			modelMap.addAttribute("uploads", Upload.findUploadEntries(
					page == null ? 0 : (page.intValue() - 1) * sizeNo, sizeNo));
			float nrOfPages = (float) Upload.countUploads() / sizeNo;
			modelMap.addAttribute(
					"maxPages",
					(int) ((nrOfPages > (int) nrOfPages || nrOfPages == 0.0) ? nrOfPages + 1
							: nrOfPages));
		} else {
			modelMap.addAttribute("uploads", Upload.findAllUploads());
		}
		return "upload/list";
	}

	@RequestMapping(method = RequestMethod.PUT)
	public String update(@Valid Upload upload, BindingResult result,
			ModelMap modelMap) {
		if (upload == null)
			throw new IllegalArgumentException("A upload is required");
		if (result.hasErrors()) {
			modelMap.addAttribute("upload", upload);
			modelMap.addAttribute("authors", Author.findAllAuthorsWithDummy());
			modelMap.addAttribute("genres", Genre.findAllGenresNoDummy());
			return "upload/update";
		}
		upload.merge();
		return "redirect:/upload/" + upload.getId();
	}

	@RequestMapping(value = "/upload/{id}/form", method = RequestMethod.GET)
	public String updateForm(@PathVariable("id") Long id, ModelMap modelMap) {
		if (id == null)
			throw new IllegalArgumentException("An Identifier is required");
		modelMap.addAttribute("upload", Upload.findUpload(id));
		modelMap.addAttribute("authors", Author.findAllAuthorsWithDummy());
		modelMap.addAttribute("genres", Genre.findAllGenresNoDummy());
		return "upload/update";
	}

	@RequestMapping(value = "/upload/{id}", method = RequestMethod.DELETE)
	public String delete(@PathVariable("id") Long id,
			@RequestParam(value = "page", required = false) Integer page,
			@RequestParam(value = "size", required = false) Integer size) {
		if (id == null)
			throw new IllegalArgumentException("An Identifier is required");
		Upload.findUpload(id).remove();
		return "redirect:/upload?page="
				+ ((page == null) ? "1" : page.toString()) + "&size="
				+ ((size == null) ? "10" : size.toString());
	}

}

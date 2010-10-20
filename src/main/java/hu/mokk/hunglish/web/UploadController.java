package hu.mokk.hunglish.web;

import hu.mokk.hunglish.domain.Author;
import hu.mokk.hunglish.domain.Genre;
import hu.mokk.hunglish.domain.Upload;

import java.io.File;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.roo.addon.web.mvc.controller.RooWebScaffold;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@RooWebScaffold(path = "upload", automaticallyMaintainView = true, formBackingObject = Upload.class)
@RequestMapping("/upload/**")
@Controller
public class UploadController {

	private String uploadDir = "fileUpload"; //TODO FIXME this should com from Spring config (property file)

		
	@RequestMapping(value = "/upload", method = RequestMethod.POST)
    public String create(@Valid Upload upload, BindingResult result, ModelMap modelMap) {
        if (upload == null) throw new IllegalArgumentException("A upload is required");
        if (result.hasErrors()) {
            modelMap.addAttribute("upload", upload);
            modelMap.addAttribute("authors", Author.findAllAuthors());
            modelMap.addAttribute("genres", Genre.findAllGenres());
            return "upload/create";
        }
        
        try{        	
	        upload.setHuOriginalName(upload.getHuFileData().getOriginalFilename());
	        upload.setEnOriginalName(upload.getEnFileData().getOriginalFilename());
	        
	        upload.validate();
	        
	        upload.persist();
	        
	        String huFilePath = uploadDir+File.separator + upload.getId()+"_HU."+upload.getHuExtension();
	        upload.getHuFileData().transferTo(new File(huFilePath));
	        upload.setHuFilePath(huFilePath);

	        String enFilePath = uploadDir+File.separator + upload.getId()+"_EN."+upload.getEnExtension();
	        upload.getEnFileData().transferTo(new File(enFilePath));
	        upload.setEnFilePath(enFilePath);
	        
	        upload.merge();
	        
        } catch (Exception e) {
        	result.reject("ERROR", e.getLocalizedMessage());
//TODO
e.printStackTrace();
//throw new RuntimeException(e);
        	return "upload/create";
		}
        return "redirect:/upload/" + upload.getId();
    }

	@RequestMapping(value = "/upload/form", method = RequestMethod.GET)
    public String createForm(ModelMap modelMap) {
        modelMap.addAttribute("upload", new Upload());
        modelMap.addAttribute("authors", Author.findAllAuthors());
        modelMap.addAttribute("genres", Genre.findAllGenres());
        return "upload/create";
    }

	@RequestMapping(value = "/upload/{id}", method = RequestMethod.GET)
    public String show(@PathVariable("id") Long id, ModelMap modelMap) {
        if (id == null) throw new IllegalArgumentException("An Identifier is required");
        modelMap.addAttribute("upload", Upload.findUpload(id));
        return "upload/show";
    }

	@RequestMapping(value = "/upload", method = RequestMethod.GET)
    public String list(@RequestParam(value = "page", required = false) Integer page, @RequestParam(value = "size", required = false) Integer size, ModelMap modelMap) {
        if (page != null || size != null) {
            int sizeNo = size == null ? 10 : size.intValue();
            modelMap.addAttribute("uploads", Upload.findUploadEntries(page == null ? 0 : (page.intValue() - 1) * sizeNo, sizeNo));
            float nrOfPages = (float) Upload.countUploads() / sizeNo;
            modelMap.addAttribute("maxPages", (int) ((nrOfPages > (int) nrOfPages || nrOfPages == 0.0) ? nrOfPages + 1 : nrOfPages));
        } else {
            modelMap.addAttribute("uploads", Upload.findAllUploads());
        }
        return "upload/list";
    }

	@RequestMapping(method = RequestMethod.PUT)
    public String update(@Valid Upload upload, BindingResult result, ModelMap modelMap) {
        if (upload == null) throw new IllegalArgumentException("A upload is required");
        if (result.hasErrors()) {
            modelMap.addAttribute("upload", upload);
            modelMap.addAttribute("authors", Author.findAllAuthors());
            modelMap.addAttribute("genres", Genre.findAllGenres());
            return "upload/update";
        }
        upload.merge();
        return "redirect:/upload/" + upload.getId();
    }

	@RequestMapping(value = "/upload/{id}/form", method = RequestMethod.GET)
    public String updateForm(@PathVariable("id") Long id, ModelMap modelMap) {
        if (id == null) throw new IllegalArgumentException("An Identifier is required");
        modelMap.addAttribute("upload", Upload.findUpload(id));
        modelMap.addAttribute("authors", Author.findAllAuthors());
        modelMap.addAttribute("genres", Genre.findAllGenres());
        return "upload/update";
    }

	@RequestMapping(value = "/upload/{id}", method = RequestMethod.DELETE)
    public String delete(@PathVariable("id") Long id, @RequestParam(value = "page", required = false) Integer page, @RequestParam(value = "size", required = false) Integer size) {
        if (id == null) throw new IllegalArgumentException("An Identifier is required");
        Upload.findUpload(id).remove();
        return "redirect:/upload?page=" + ((page == null) ? "1" : page.toString()) + "&size=" + ((size == null) ? "10" : size.toString());
    }

	public String getUploadDir() {
		return uploadDir;
	}

	public void setUploadDir(String uploadDir) {
		this.uploadDir = uploadDir;
	}
}

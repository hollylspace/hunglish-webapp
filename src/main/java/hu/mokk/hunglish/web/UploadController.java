package hu.mokk.hunglish.web;

import org.springframework.roo.addon.web.mvc.controller.RooWebScaffold;
import hu.mokk.hunglish.domain.Author;
import hu.mokk.hunglish.domain.Genre;
import hu.mokk.hunglish.domain.Upload;
import javax.validation.Valid;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.stereotype.Controller;

@RooWebScaffold(path = "upload", automaticallyMaintainView = true, formBackingObject = Upload.class)
@RequestMapping("/upload/**")
@Controller
public class UploadController {

	@RequestMapping(value = "/upload", method = RequestMethod.POST)
    public String create(@Valid Upload upload, BindingResult result, ModelMap modelMap) {
        if (upload == null) throw new IllegalArgumentException("A upload is required");
        if (result.hasErrors()) {
            modelMap.addAttribute("upload", upload);
            modelMap.addAttribute("authors", Author.findAllAuthors());
            modelMap.addAttribute("genres", Genre.findAllGenres());
            return "upload/create";
        }
        upload.persist();

        System.err.println("-------------------------------------------");
        System.err.println("Test upload name: " + upload.getHuFileData().getOriginalFilename());
        System.err.println("Test upload size: " + upload.getHuFileData().getFileItem().getSize());
        System.err.println("-------------------------------------------");        
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
}

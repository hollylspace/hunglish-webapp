package hu.mokk.hunglish.web;

import org.springframework.roo.addon.web.mvc.controller.RooWebScaffold;
import hu.mokk.hunglish.domain.Author;
import javax.validation.Valid;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.stereotype.Controller;

@RooWebScaffold(path = "author", automaticallyMaintainView = true, formBackingObject = Author.class)
@RequestMapping("/author/**")
@Controller
public class AuthorController {

	@RequestMapping(value = "/author", method = RequestMethod.POST)
    public String create(@Valid Author author, BindingResult result, ModelMap modelMap) {
        if (author == null) throw new IllegalArgumentException("A author is required");
        if (result.hasErrors()) {
            modelMap.addAttribute("author", author);
            return "author/create";
        }
        author.persist();
        return "redirect:/author/" + author.getId();
    }

	@RequestMapping(value = "/author/form", method = RequestMethod.GET)
    public String createForm(ModelMap modelMap) {
        modelMap.addAttribute("author", new Author());
        return "author/create";
    }

	@RequestMapping(value = "/author/{id}", method = RequestMethod.GET)
    public String show(@PathVariable("id") Long id, ModelMap modelMap) {
        if (id == null) throw new IllegalArgumentException("An Identifier is required");
        modelMap.addAttribute("author", Author.findAuthor(id));
        return "author/show";
    }

	@RequestMapping(value = "/author", method = RequestMethod.GET)
    public String list(@RequestParam(value = "page", required = false) Integer page, @RequestParam(value = "size", required = false) Integer size, ModelMap modelMap) {
        if (page != null || size != null) {
            int sizeNo = size == null ? 10 : size.intValue();
            modelMap.addAttribute("authors", Author.findAuthorEntries(page == null ? 0 : (page.intValue() - 1) * sizeNo, sizeNo));
            float nrOfPages = (float) Author.countAuthors() / sizeNo;
            modelMap.addAttribute("maxPages", (int) ((nrOfPages > (int) nrOfPages || nrOfPages == 0.0) ? nrOfPages + 1 : nrOfPages));
        } else {
            modelMap.addAttribute("authors", Author.findAllAuthors());
        }
        return "author/list";
    }

	@RequestMapping(method = RequestMethod.PUT)
    public String update(@Valid Author author, BindingResult result, ModelMap modelMap) {
        if (author == null) throw new IllegalArgumentException("A author is required");
        if (result.hasErrors()) {
            modelMap.addAttribute("author", author);
            return "author/update";
        }
        author.merge();
        return "redirect:/author/" + author.getId();
    }

	@RequestMapping(value = "/author/{id}/form", method = RequestMethod.GET)
    public String updateForm(@PathVariable("id") Long id, ModelMap modelMap) {
        if (id == null) throw new IllegalArgumentException("An Identifier is required");
        modelMap.addAttribute("author", Author.findAuthor(id));
        return "author/update";
    }

	/*
	@RequestMapping(value = "/author/{id}", method = RequestMethod.DELETE)
    public String delete(@PathVariable("id") Long id, @RequestParam(value = "page", required = false) Integer page, @RequestParam(value = "size", required = false) Integer size) {
        if (id == null) throw new IllegalArgumentException("An Identifier is required");
        Author.findAuthor(id).remove();
        return "redirect:/author?page=" + ((page == null) ? "1" : page.toString()) + "&size=" + ((size == null) ? "10" : size.toString());
    } */
	
}

package hu.mokk.hunglish.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.roo.addon.web.mvc.controller.RooWebScaffold;
import hu.mokk.hunglish.domain.Author;
import hu.mokk.hunglish.domain.Doc;
import hu.mokk.hunglish.domain.Genre;
import hu.mokk.hunglish.lucene.Searcher;

import javax.validation.Valid;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.stereotype.Controller;

@RooWebScaffold(path = "doc", automaticallyMaintainView = true, formBackingObject = Doc.class)
@RequestMapping("/doc/**")
@Controller
public class DocController {
	@Autowired
	private Searcher searcher;

	/*
	@RequestMapping(value = "/doc", method = RequestMethod.POST)
    public String create(@Valid Doc doc, BindingResult result, ModelMap modelMap) {
        if (doc == null) throw new IllegalArgumentException("A doc is required");
        if (result.hasErrors()) {
            modelMap.addAttribute("doc", doc);
            modelMap.addAttribute("authors", Author.findAllAuthors());
            modelMap.addAttribute("genres", Genre.findAllGenres());
            return "doc/create";
        }
        doc.persist();
        return "redirect:/doc/" + doc.getId();
    } */

	@RequestMapping(value = "/doc/form", method = RequestMethod.GET)
    public String createForm(ModelMap modelMap) {
        modelMap.addAttribute("doc", new Doc());
        modelMap.addAttribute("authors", Author.findAllAuthors());
        modelMap.addAttribute("genres", Genre.findAllGenres());
        return "doc/create";
    }

	@RequestMapping(value = "/doc/{id}", method = RequestMethod.GET)
    public String show(@PathVariable("id") Long id, ModelMap modelMap) {
        if (id == null) throw new IllegalArgumentException("An Identifier is required");
        modelMap.addAttribute("doc", Doc.findDoc(id));
        return "doc/show";
    }

	@RequestMapping(value = "/doc", method = RequestMethod.GET)
    public String list(@RequestParam(value = "page", required = false) Integer page, @RequestParam(value = "size", required = false) Integer size, ModelMap modelMap) {

		int sizeNo = size == null ? 10 : Math.min(searcher.getMaxResultSetSize(), size.intValue());
        modelMap.addAttribute("docs", Doc.findDocEntries(page == null ? 0 : (page.intValue() - 1) * sizeNo, sizeNo));
        float nrOfPages = (float) Doc.countDocs() / sizeNo;
        modelMap.addAttribute("maxPages", (int) ((nrOfPages > (int) nrOfPages || nrOfPages == 0.0) ? nrOfPages + 1 : nrOfPages));
        return "doc/list";
    }

	/*
	@RequestMapping(method = RequestMethod.PUT)
    public String update(@Valid Doc doc, BindingResult result, ModelMap modelMap) {
        if (doc == null) throw new IllegalArgumentException("A doc is required");
        if (result.hasErrors()) {
            modelMap.addAttribute("doc", doc);
            modelMap.addAttribute("authors", Author.findAllAuthors());
            modelMap.addAttribute("genres", Genre.findAllGenres());
            return "doc/update";
        }
        doc.merge();
        return "redirect:/doc/" + doc.getId();
    } */

	@RequestMapping(value = "/doc/{id}/form", method = RequestMethod.GET)
    public String updateForm(@PathVariable("id") Long id, ModelMap modelMap) {
        if (id == null) throw new IllegalArgumentException("An Identifier is required");
        modelMap.addAttribute("doc", Doc.findDoc(id));
        modelMap.addAttribute("authors", Author.findAllAuthors());
        modelMap.addAttribute("genres", Genre.findAllGenres());
        return "doc/update";
    }

	/*
	@RequestMapping(value = "/doc/{id}", method = RequestMethod.DELETE)
    public String delete(@PathVariable("id") Long id, @RequestParam(value = "page", required = false) Integer page, @RequestParam(value = "size", required = false) Integer size) {
        if (id == null) throw new IllegalArgumentException("An Identifier is required");
        Doc.findDoc(id).remove();
        return "redirect:/doc?page=" + ((page == null) ? "1" : page.toString()) + "&size=" + ((size == null) ? "10" : size.toString());
    } */
	
}

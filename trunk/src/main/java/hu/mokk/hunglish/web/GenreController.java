package hu.mokk.hunglish.web;

import org.springframework.roo.addon.web.mvc.controller.RooWebScaffold;
import hu.mokk.hunglish.domain.Genre;
import javax.validation.Valid;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.stereotype.Controller;

@RooWebScaffold(path = "genre", automaticallyMaintainView = true, formBackingObject = Genre.class)
@RequestMapping("/genre/**")
@Controller
public class GenreController {

	@RequestMapping(value = "/genre", method = RequestMethod.POST)
    public String create(@Valid Genre genre, BindingResult result, ModelMap modelMap) {
        if (genre == null) throw new IllegalArgumentException("A genre is required");
        if (result.hasErrors()) {
            modelMap.addAttribute("genre", genre);
            return "genre/create";
        }
        genre.persist();
        return "redirect:/genre/" + genre.getId();
    }

	@RequestMapping(value = "/genre/form", method = RequestMethod.GET)
    public String createForm(ModelMap modelMap) {
        modelMap.addAttribute("genre", new Genre());
        return "genre/create";
    }

	@RequestMapping(value = "/genre/{id}", method = RequestMethod.GET)
    public String show(@PathVariable("id") Long id, ModelMap modelMap) {
        if (id == null) throw new IllegalArgumentException("An Identifier is required");
        modelMap.addAttribute("genre", Genre.findGenre(id));
        return "genre/show";
    }

	@RequestMapping(value = "/genre", method = RequestMethod.GET)
    public String list(@RequestParam(value = "page", required = false) Integer page, @RequestParam(value = "size", required = false) Integer size, ModelMap modelMap) {
        if (page != null || size != null) {
            int sizeNo = size == null ? 10 : size.intValue();
            modelMap.addAttribute("genres", Genre.findGenreEntries(page == null ? 0 : (page.intValue() - 1) * sizeNo, sizeNo));
            float nrOfPages = (float) Genre.countGenres() / sizeNo;
            modelMap.addAttribute("maxPages", (int) ((nrOfPages > (int) nrOfPages || nrOfPages == 0.0) ? nrOfPages + 1 : nrOfPages));
        } else {
            modelMap.addAttribute("genres", Genre.findAllGenres());
        }
        return "genre/list";
    }

	@RequestMapping(method = RequestMethod.PUT)
    public String update(@Valid Genre genre, BindingResult result, ModelMap modelMap) {
        if (genre == null) throw new IllegalArgumentException("A genre is required");
        if (result.hasErrors()) {
            modelMap.addAttribute("genre", genre);
            return "genre/update";
        }
        genre.merge();
        return "redirect:/genre/" + genre.getId();
    }

	@RequestMapping(value = "/genre/{id}/form", method = RequestMethod.GET)
    public String updateForm(@PathVariable("id") Long id, ModelMap modelMap) {
        if (id == null) throw new IllegalArgumentException("An Identifier is required");
        modelMap.addAttribute("genre", Genre.findGenre(id));
        return "genre/update";
    }

	/*
	@RequestMapping(value = "/genre/{id}", method = RequestMethod.DELETE)
    public String delete(@PathVariable("id") Long id, @RequestParam(value = "page", required = false) Integer page, @RequestParam(value = "size", required = false) Integer size) {
        if (id == null) throw new IllegalArgumentException("An Identifier is required");
        Genre.findGenre(id).remove();
        return "redirect:/genre?page=" + ((page == null) ? "1" : page.toString()) + "&size=" + ((size == null) ? "10" : size.toString());
    } */
}

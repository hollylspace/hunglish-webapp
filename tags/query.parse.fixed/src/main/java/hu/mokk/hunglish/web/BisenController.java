package hu.mokk.hunglish.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.roo.addon.web.mvc.controller.RooWebScaffold;
import hu.mokk.hunglish.domain.Bisen;
import hu.mokk.hunglish.domain.Doc;
import hu.mokk.hunglish.lucene.Indexer;
import hu.mokk.hunglish.lucene.Searcher;

import javax.validation.Valid;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.stereotype.Controller;

@RooWebScaffold(path = "bisen", automaticallyMaintainView = true, formBackingObject = Bisen.class)
@RequestMapping("/bisen/**")
@Controller
public class BisenController {

	@Autowired
	private Searcher searcher;

	//private Integer maxResultSetSize = 100;

	/*
	 * @RequestMapping(value = "/bisen", method = RequestMethod.POST) public
	 * String create(@Valid Bisen bisen, BindingResult result, ModelMap
	 * modelMap) { if (bisen == null) throw new
	 * IllegalArgumentException("A bisen is required"); if (result.hasErrors())
	 * { modelMap.addAttribute("bisen", bisen); modelMap.addAttribute("docs",
	 * Doc.findAllDocs()); return "bisen/create"; } bisen.persist(); return
	 * "redirect:/bisen/" + bisen.getId(); }
	 */

	/*public void setMaxResultSetSize(int maxResultSetSize) {
		this.maxResultSetSize = maxResultSetSize;
	}*/

	@RequestMapping(value = "/bisen/form", method = RequestMethod.GET)
	public String createForm(ModelMap modelMap) {
		modelMap.addAttribute("bisen", new Bisen());
		modelMap.addAttribute("docs", Doc.findAllDocs());
		return "bisen/create";
	}

	@RequestMapping(value = "/bisen/{id}", method = RequestMethod.GET)
	public String show(@PathVariable("id") Long id, ModelMap modelMap) {
		if (id == null)
			throw new IllegalArgumentException("An Identifier is required");
		modelMap.addAttribute("bisen", Bisen.findBisen(id));
		return "bisen/show";
	}

	@RequestMapping(value = "/bisen", method = RequestMethod.GET)
	public String list(
			@RequestParam(value = "page", required = false) Integer page,
			@RequestParam(value = "size", required = false) Integer size,
			ModelMap modelMap) {
		
		int sizeNo = size == null ? 10 : Math.min(searcher.getMaxResultSetSize(), size.intValue());
		int pageNo = page == null ? 1 : page.intValue();
		modelMap.addAttribute("bisens",
				Bisen.findBisenEntries((pageNo - 1) * sizeNo, sizeNo));
		float nrOfPages = (float) Bisen.countBisens() / sizeNo;
		modelMap.addAttribute(
				"maxPages",
				(int) ((nrOfPages > (int) nrOfPages || nrOfPages == 0.0) ? nrOfPages + 1
						: nrOfPages));
		// NEVER!
		// modelMap.addAttribute("bisens", Bisen.findAllBisens());
		return "bisen/list";
	}

	/*
	 * @RequestMapping(method = RequestMethod.PUT) public String update(@Valid
	 * Bisen bisen, BindingResult result, ModelMap modelMap) { if (bisen ==
	 * null) throw new IllegalArgumentException("A bisen is required"); if
	 * (result.hasErrors()) { modelMap.addAttribute("bisen", bisen);
	 * modelMap.addAttribute("docs", Doc.findAllDocs()); return "bisen/update";
	 * } bisen.merge(); return "redirect:/bisen/" + bisen.getId(); }
	 */

	@RequestMapping(value = "/bisen/{id}/form", method = RequestMethod.GET)
	public String updateForm(@PathVariable("id") Long id, ModelMap modelMap) {
		if (id == null)
			throw new IllegalArgumentException("An Identifier is required");
		modelMap.addAttribute("bisen", Bisen.findBisen(id));
		modelMap.addAttribute("docs", Doc.findAllDocs());
		return "bisen/update";
	}

	/*
	 * @RequestMapping(value = "/bisen/{id}", method = RequestMethod.DELETE)
	 * public String delete(@PathVariable("id") Long id, @RequestParam(value =
	 * "page", required = false) Integer page, @RequestParam(value = "size",
	 * required = false) Integer size) { if (id == null) throw new
	 * IllegalArgumentException("An Identifier is required");
	 * Bisen.findBisen(id).remove(); return "redirect:/bisen?page=" + ((page ==
	 * null) ? "1" : page.toString()) + "&size=" + ((size == null) ? "10" :
	 * size.toString()); }
	 */

}

package hu.mokk.hunglish.web;

import hu.mokk.hunglish.domain.Bisen;

import org.springframework.roo.addon.web.mvc.controller.RooWebScaffold;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@RooWebScaffold(path = "search", automaticallyMaintainView = true, formBackingObject = Bisen.class)
@RequestMapping("/search/**")
@Controller
public class SearchController {

	@RequestMapping(value = "/search", method = RequestMethod.GET)
    public String search(
    		@RequestParam(value = "page", required = false) Integer page, 
    		@RequestParam(value = "size", required = false) Integer size, 
    		ModelMap modelMap) {
        int sizeNo = size == null ? 10 : size.intValue();
        int pageNo = page == null ? 1 : page.intValue();
        modelMap.addAttribute("bisens", Bisen.findBisenEntries((pageNo - 1) * sizeNo, sizeNo));
        //float nrOfPages = (float) Bisen.countBisens() / sizeNo;
        //modelMap.addAttribute("maxPages", (int) ((nrOfPages > (int) nrOfPages || nrOfPages == 0.0) ? nrOfPages + 1 : nrOfPages));
        return "search/search";
    }
}

package hu.mokk.hunglish.web;

import java.util.logging.Level;
import java.util.logging.Logger;

import hu.mokk.hunglish.domain.Bisen;
import hu.mokk.hunglish.lucene.SearchRequest;
import hu.mokk.hunglish.lucene.SearchResult;
import hu.mokk.hunglish.lucene.Searcher;

import org.springframework.beans.factory.annotation.Autowired;
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

	
	@Autowired
	private Searcher searcher;
	
	@RequestMapping(value = "/search", method = RequestMethod.GET)
    public String search(
    		@RequestParam(value = "start", required = false) Integer start, 
    		@RequestParam(value = "n", required = false) Integer size, 
    		@RequestParam(value = "ql", required = false) String huQuery,
    		@RequestParam(value = "qr", required = false) String enQuery,
    		@RequestParam(value = "source", required = false) String source,
    		ModelMap modelMap) {
        
System.out.println("fuck huQuery:"+huQuery);
System.out.println("fuck enQuery:"+enQuery);
        
		int sizeNo = size == null ? 10 : size.intValue();
        int startNo = start == null ? 1 : start.intValue();
        
		SearchRequest request = new SearchRequest();
		request.setEnQuery(enQuery);
		request.setHuQuery(huQuery);
        request.setMaxResults(sizeNo);
        request.setStartOffset(startNo);
        //request.setSourceId(source);
        request.setHunglishSyntax(false);
        modelMap.addAttribute("request", request);
        
        SearchResult result = searcher.search(request);
        modelMap.addAttribute("result", result);
        //modelMap.addAttribute("bisens", result.getHitList());
        //float nrOfPages = (float) Bisen.countBisens() / sizeNo;
        //modelMap.addAttribute("maxPages", (int) ((nrOfPages > (int) nrOfPages || nrOfPages == 0.0) ? nrOfPages + 1 : nrOfPages));
        
        request.setHunglishSyntax(true);
        SearchResult resultHunglishSyntax = searcher.search(request);
        modelMap.addAttribute("resultHunglishSyntax", resultHunglishSyntax);
        
        
        return "search/list";
    }
}

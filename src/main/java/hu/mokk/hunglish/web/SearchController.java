package hu.mokk.hunglish.web;

import hu.mokk.hunglish.domain.Bisen;
import hu.mokk.hunglish.domain.Genre;
import hu.mokk.hunglish.lucene.SearchRequest;
import hu.mokk.hunglish.lucene.SearchResult;
import hu.mokk.hunglish.lucene.Searcher;
import hu.mokk.hunglish.util.Pair;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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
	private static Log logger = LogFactory.getLog(SearchController.class);
	
	public static final int PAGE_SIZE = 20;
	
	@Autowired
	private Searcher searcher;

	@RequestMapping(value = "/search", method = RequestMethod.GET)
	public String search(
			//@RequestParam(value = "start", required = false) Integer start,
			//@RequestParam(value = "n", required = false) Integer size,
    		@RequestParam(value = "page", required = false) Integer page, 
    		@RequestParam(value = "size", required = false) Integer size, 
			// @Valid
			Bisen bisen,
			// BindingResult result,
			// @RequestParam(value = "ql", required = false) String huQuery,
			// @RequestParam(value = "qr", required = false) String enQuery,
			// @RequestParam(value = "source", required = false) String source,
			ModelMap modelMap) {

		modelMap.addAttribute("genres", Genre.findAllGenres());

        int sizeNo = size == null ? PAGE_SIZE : size.intValue();
        int pageNo = page == null ? 1 : page.intValue();
		SearchRequest request = new SearchRequest();
		
		request.setEnQuery(bisen.getEnSentence());
		request.setHuQuery(bisen.getHuSentence());
		if (bisen.getDoc() != null && bisen.getDoc().getGenre() != null
				&& bisen.getDoc().getGenre().getId() != null) {
			request.setGenreId(bisen.getDoc().getGenre().getId().toString());
		}
		
		request.setMaxResults(sizeNo);
		request.setStartOffset((pageNo-1)*sizeNo);
		
		request.setHunglishSyntax(true);
		modelMap.addAttribute("request", request);

		SearchResult result = searcher.search(request);
		
        float nrOfPages = (float) result.getTotalCount() / sizeNo;
        
        int maxPages = (int) ((nrOfPages > (int) nrOfPages || nrOfPages == 0.0) ? nrOfPages + 1 : nrOfPages);

        float nrOfPagesLimited = (float) searcher.getMaxDocuments() / sizeNo;
        int maxPagesLimited = (int) nrOfPagesLimited; 
        
        
        modelMap.addAttribute("maxPages", maxPages);
        modelMap.addAttribute("page", pageNo);
        
        String baseUrl = getBaseUrl(bisen) +"&size="+sizeNo;
        List<Pair<String, String>> linx = new ArrayList<Pair<String, String>>();
        for (int i = 1; i <= (int)Math.min(maxPagesLimited, maxPages); i++){
        	String url =baseUrl+"&page="+i;        	
        	linx.add(new Pair<String, String>(url, new Integer(i).toString()));
        }
        result.setPaginationLinks(linx);
        modelMap.addAttribute("result", result);
		
		return "search/list";
	}
	
	private String getBaseUrl(Bisen bisen){
		 String baseUrl = "?huSentence=";
		 if (bisen.getHuSentence() != null){
			 baseUrl += bisen.getHuSentence();
		 }
		 baseUrl += "&enSentence=";
		 if (bisen.getEnSentence() != null){
			 baseUrl += bisen.getEnSentence();
		 }
		 return baseUrl;
	}
}

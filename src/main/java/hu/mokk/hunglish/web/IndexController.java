package hu.mokk.hunglish.web;

import hu.mokk.hunglish.domain.Bisen;
import hu.mokk.hunglish.domain.Genre;

import java.io.UnsupportedEncodingException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.roo.addon.web.mvc.controller.RooWebScaffold;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@RooWebScaffold(path = "", automaticallyMaintainView = true, formBackingObject = Bisen.class)
@RequestMapping("/index/**")
@Controller
public class IndexController {
	private static Log logger = LogFactory.getLog(IndexController.class);
	
	public static final int PAGE_SIZE = 20;
	
	@RequestMapping(value = "/index", method = RequestMethod.GET)
	public String search(
			Bisen bisen, //this is a hack, this holds the search terms! TODO clear it up
			ModelMap modelMap) throws UnsupportedEncodingException {

		modelMap.addAttribute("genres", Genre.findAllGenres());

		return "index";
	}
	
}

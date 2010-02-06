/*
 * Created by Peter Halacsy <peter at halacsy.com>
 * 
 * This work is licensed under the Creative Commons 
 * Attribution License. To view a copy of this license, 
 * visit http://creativecommons.org/licenses/by/2.0/ 
 * or send a letter to Creative Commons, 559 Nathan Abbott Way, 
 * Stanford, California 94305, USA.
 * 
 * Created on May 9, 2005
 *
 */

package mokk.nlp.dictweb.handlers;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import mokk.nlp.bicorpus.SourceDB;
import mokk.nlp.bicorpus.index.BiCorpusSearcher;
import mokk.nlp.bicorpus.index.SearchRequest;
import mokk.nlp.dictweb.RequestHandler;
import mokk.nlp.irutil.SearchException;
import mokk.nlp.irutil.SearchResult;

import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.activity.Initializable;
import org.apache.avalon.framework.component.Component;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.logger.LogEnabled;
import org.apache.avalon.framework.logger.Logger;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.velocity.Template;
import org.apache.velocity.context.Context;

/**
 * @author hp
 * 
 *         Handle request for search of BiCorpus
 * 
 * @avalon.component
 * @avalon.service type=mokk.nlp.dictweb.RequestHandler
 * @x-avalon.info name=bisearch-handler
 * @x-avalon.lifestyle type=singleton
 * 
 */
public class BiCorpusSearchHandler implements RequestHandler, Component,
		LogEnabled, Configurable, Initializable, Serviceable, Disposable {

	public int defaultN = 10;
	public static final int defaultStart = 0;

	private BiCorpusSearcher m_searcher;
	private ServiceManager manager;

	private String m_searcherId = null;

	private Logger logger;

	private SourceDB sourceDb;

	private List availableSources = null;

	public void enableLogging(Logger logger) {
		this.logger = logger;
	}

	public void configure(Configuration config) throws ConfigurationException {
		m_searcherId = (String) config.getChild("searcher").getValue();

		defaultN = config.getChild("page-size").getValueAsInteger(defaultN);

		logger.info("using searcher: " + m_searcherId);

	}

	/**
	 * @avalon.dependency type="mokk.nlp.bicorpus.index.BiCorpusSearcher"
	 * @avalon.dependency type="mokk.nlp.bicorpus.SourceDB"
	 */
	public void service(ServiceManager manager) throws ServiceException {
		this.manager = manager;
	}

	public void initialize() throws Exception {
		m_searcher = (BiCorpusSearcher) manager.lookup(BiCorpusSearcher.ROLE
				+ "/" + m_searcherId);
		sourceDb = (SourceDB) manager.lookup(SourceDB.ROLE);

		availableSources = new ArrayList(sourceDb.getKnownSources());
		Collections.sort(availableSources);

	}

	public void dispose() {
		if (m_searcher != null) {
			manager.release(m_searcher);
		}
		if (sourceDb != null) {
			manager.release(sourceDb);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * mokk.nlp.bicorpus.servlet.RequestHandler#handleRequest(javax.servlet.
	 * http.HttpServletRequest, javax.servlet.http.HttpServletResponse,
	 * org.apache.velocity.context.Context)
	 */
	public String handleRequest(HttpServletRequest request,
			HttpServletResponse response, Context context) throws Exception {
		request.setCharacterEncoding("ISO-8859-2");
		response.setContentType("text/html; charset=iso-8859-2");

		SearchRequest searchRequest = parseParameters(request);

		if (searchRequest.getCommonQuery() == null
				&& searchRequest.getLeftQuery() == null
				&& searchRequest.getRightQuery() == null) {
			// no query specified
			return "index.vm";

		}

		SearchResult result = null;
		try {
			result = m_searcher.search(searchRequest);
		} catch (SearchException e) {
			logger.error("search exception", e);
			throw new ServletException("search exception", e);
		}
		context.put("request", searchRequest);
		context.put("result", result);
		context.put("sources", availableSources);
		context.put("source", sourceDb.get(searchRequest.getSourceId()));
		context.put("pager", getPager(request, searchRequest, result));
		Template template = null;

		return "result.vm";

	}

	public Pager getPager(HttpServletRequest request,
			SearchRequest searchRequest, SearchResult result) {
		StringBuffer baseQuery = request.getRequestURL();
		baseQuery.append("?");
		try {
			if (searchRequest.getCommonQuery() != null) {
				baseQuery.append("&q=").append(
						URLEncoder.encode(searchRequest.getCommonQuery(),
								"ISO-8859-2"));

			}
			baseQuery.append("ql=").append(
					URLEncoder.encode(searchRequest.getLeftQuery(),
							"ISO-8859-2"));
			if (searchRequest.getLeftQuery() != null) {
				baseQuery.append("&qr=").append(
						URLEncoder.encode(searchRequest.getRightQuery(),
								"ISO-8859-2"));

			}
			if (searchRequest.getRightQuery() != null) {
				baseQuery.append("&n=").append(searchRequest.getMaxResults());
			}
			if (searchRequest.getSourceId() != null) {
				baseQuery.append("&source=").append(
						URLEncoder.encode(searchRequest.getSourceId(),
								"ISO-8859-2"));
			}
		} catch (UnsupportedEncodingException e) {

			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (!searchRequest.isStemLeftQuery()) {
			baseQuery.append("&nostem_left=on");
		}
		if (!searchRequest.isStemRightQuery()) {
			baseQuery.append("&nostem_right=on");
		}
		Pager pager = new Pager(baseQuery.toString(), searchRequest
				.getStartOffset(), searchRequest.getMaxResults(), result
				.getTotalCount(), "start");
		return pager;
	}

	public SearchRequest parseParameters(HttpServletRequest request) {

		SearchRequest searchRequest = new SearchRequest();
		//TODO FIXME from config
		searchRequest.setExcludeDuplicates(true); 
		
		String sourceId = request.getParameter("source");
		if (sourceId == "" || sourceId == "all") {
			sourceId = null;
		}
		searchRequest.setSourceId(sourceId);
		String leftQuery = null;

		searchRequest.setCommonQuery(request.getParameter("q"));
		searchRequest.setLeftQuery(request.getParameter("ql"));
		searchRequest.setRightQuery(request.getParameter("qr"));

		int n = defaultN;
		int start = defaultStart;
		try {
			n = Integer.parseInt(request.getParameter("n"));
			start = Integer.parseInt(request.getParameter("start"));
		} catch (NumberFormatException nfe) {
			// do nothing but use default values
		}

		searchRequest.setStartOffset(start);
		searchRequest.setMaxResults(n);

		if (request.getParameter("nostem_left") != null) {
			searchRequest.setStemLeftQuery(false);
		} else {
			searchRequest.setStemLeftQuery(true);
		}
		if (request.getParameter("nostem_right") != null) {
			searchRequest.setStemRightQuery(false);
		} else {
			searchRequest.setStemRightQuery(true);
		}
		return searchRequest;
	}

}

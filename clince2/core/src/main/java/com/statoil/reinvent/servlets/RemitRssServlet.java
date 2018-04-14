package com.statoil.reinvent.servlets;

import java.io.IOException;
import java.io.InputStream;

import javax.jcr.Node;
import javax.jcr.Session;

import org.apache.commons.io.IOUtils;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.sling.SlingServlet;
import org.apache.jackrabbit.commons.JcrUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.statoil.reinvent.constants.StatoilConstants;
import com.statoil.reinvent.rss.RemitJsonParser;
import com.statoil.reinvent.rss.RssFeed;
import com.statoil.reinvent.rss.RssFeedWriter;
import com.statoil.reinvent.services.UserMapperService;

@SlingServlet(paths = { "/services/remitrss" }, methods = { "GET" })
public class RemitRssServlet extends SlingAllMethodsServlet {

	private static final long serialVersionUID = -187747633068043963L;

	private static final Logger log = LoggerFactory.getLogger(RemitRssServlet.class);

	@Reference
	private UserMapperService userMapperService;
	
	private ResourceResolver resourceResolver;

	@Override
	protected final void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response) throws IOException {
		try {
			resourceResolver = userMapperService.getJCRReadService();
			Session session = resourceResolver.adaptTo(Session.class);

			Node node = session.getNode(StatoilConstants.REMIT_FILE_DAM_PATH);
			InputStream inputStream = JcrUtils.readFile(node);
			String jsonString = IOUtils.toString(inputStream);

			RemitJsonParser remitJsonParser = new RemitJsonParser();
			RssFeed rssFeed = remitJsonParser.parse(jsonString);

			response.setContentType(StatoilConstants.Mime.TEXT_XML);
			RssFeedWriter rssFeedWriter = new RssFeedWriter(rssFeed, response.getWriter());
			rssFeedWriter.write();
		} catch (Exception e) {
			log.error("Exception: ", e);
		} finally {
			if (resourceResolver != null && resourceResolver.isLive()) {
				resourceResolver.close();
			}
		}
	}
}
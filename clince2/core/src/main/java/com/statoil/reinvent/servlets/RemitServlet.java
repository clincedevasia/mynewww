package com.statoil.reinvent.servlets;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.jcr.Node;
import javax.jcr.Session;
import javax.servlet.ServletException;

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
import com.statoil.reinvent.services.UserMapperService;

@SlingServlet(paths = { "/services/remit" }, methods = { "GET" })
public class RemitServlet extends SlingAllMethodsServlet {

	private static final long serialVersionUID = -1384724982780591563L;

	@Reference
	private UserMapperService userMapperService;

	private ResourceResolver resourceResolver;

	private static final Logger log = LoggerFactory.getLogger(RemitServlet.class);

	@Override
	protected final void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response)
			throws ServletException, IOException {
		try {
			resourceResolver = userMapperService.getJCRReadService();
			Session session = resourceResolver.adaptTo(Session.class);

			Node node = session.getNode(StatoilConstants.REMIT_FILE_DAM_PATH);
			InputStream inputStream = JcrUtils.readFile(node);
			response.setContentType(StatoilConstants.Mime.APPLICATION_JSON);
			OutputStream responseOutputStream = response.getOutputStream();

			int bytes;

			while ((bytes = inputStream.read()) != -1) {
				responseOutputStream.write(bytes);
			}
		} catch (Exception e) {
			log.error("Exception: " + e);
		} finally {
			if (resourceResolver != null && resourceResolver.isLive()) {
				resourceResolver.close();
			}
		}
	}
}
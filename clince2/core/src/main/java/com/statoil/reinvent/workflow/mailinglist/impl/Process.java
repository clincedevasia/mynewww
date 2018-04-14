package com.statoil.reinvent.workflow.mailinglist.impl;

import java.net.URI;
import java.net.URISyntaxException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.osgi.framework.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.day.cq.commons.Externalizer;
import com.day.cq.tagging.Tag;
import com.day.cq.wcm.api.Page;
import com.day.cq.workflow.WorkflowException;
import com.day.cq.workflow.WorkflowSession;
import com.day.cq.workflow.exec.WorkItem;
import com.day.cq.workflow.exec.WorkflowData;
import com.day.cq.workflow.exec.WorkflowProcess;
import com.day.cq.workflow.metadata.MetaDataMap;
import com.statoil.reinvent.services.UserMapperService;
import com.statoil.reinvent.servlets.LanguageResourceResolver;
import com.statoil.reinvent.workflow.mailinglist.BrandMasterFacade;

@Component
@Service
@Properties({
		@Property(name = Constants.SERVICE_DESCRIPTION, value = "Mailing list workflow process for sending e-mails to mailing list subscribers"),
		@Property(name = Constants.SERVICE_VENDOR, value = "Capgemini"),
		@Property(name = "process.label", value = "Mailing List") })

public class Process implements WorkflowProcess {

	private static final String PUSH_MAILS = "pushMails";

	private static final String TYPE_JCR_PATH = "JCR_PATH";

	private static final Logger logger = LoggerFactory.getLogger(Process.class);

	@Reference
	private UserMapperService userMapperService;

	@Reference
	private Externalizer externalizer;

	@Reference
	private BrandMasterFacade brandMasterFacade;

	private ResourceResolver resourceResolver;

	/**
	 * Executes a new Java process with the given WorkItem and WorkflowSession.
	 * 
	 * @param workItem
	 *            The WorkItem that defines the newly started JavaProcess.
	 * @param workflowSession
	 *            The WorkFlowSession that is used for starting the JavaProcess.
	 * @param metaDataMap
	 *            Process specific arguments can be passed here.
	 * @throws WorkflowException
	 *             Thrown in case something goes wrong during execution.
	 */
	@Override
	public void execute(WorkItem workItem, WorkflowSession workflowSession, MetaDataMap metaDataMap)
			throws WorkflowException {

		try {
			resourceResolver = userMapperService.getJCRReadService();

			WorkflowData workflowData = workItem.getWorkflowData();
			String payloadType = workflowData.getPayloadType();
			String payloadPath = workflowData.getPayload().toString();

			if (pushMailActivated(payloadPath)) {
				if (!payloadType.equals(TYPE_JCR_PATH)) {
					throw new IllegalArgumentException("The payload " + payloadType + " is not a path in the JCR.");
				}
				Map<String, String> emailParameters = getEmailParameters(workflowData);

				brandMasterFacade.distribute(emailParameters);
			} else {
				workflowSession.terminateWorkflow(workItem.getWorkflow());
				logger.info("Mailing list workflow is ended because pushMail option is unchecked in page properties");
			}
		} catch (Exception e) {
			logger.error("Failed to execute", e);
		} finally {
			if (resourceResolver != null && resourceResolver.isLive()) {
				resourceResolver.close();
			}
		}
	}

	private Tag[] getTags(WorkflowData workflowData) {
		String payloadPath = workflowData.getPayload().toString();
		Page page = Utils.getPage(payloadPath, resourceResolver);

		return page.getTags();
	}

	private String getNewsType(WorkflowData workflowData) {
		Tag[] tags = getTags(workflowData);

		// Statoil only specify one tag.
		if (tags.length != 1) {
			throw new IllegalArgumentException(
					"Tag count " + tags.length + " is different than 1 for " + workflowData.getPayload().toString());
		}

		String tagId = tags[0].getTagID();

		switch (tagId) {
		case "statoil-tags:general-news":
			return "Company";
		case "statoil-tags:stock-market-announcements":
			return "Stock";
		case "statoil-tags:crude-oil-assays":
			return "Crude";
		default:
			throw new IllegalArgumentException(
					"Unsupported tag: '" + tagId + "' for " + workflowData.getPayload().toString());
		}
	}

	private Map<String, String> getEmailParameters(WorkflowData workflowData) throws URISyntaxException {
		String payloadPath = workflowData.getPayload().toString();
		Page page = Utils.getPage(payloadPath, resourceResolver);
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
		String date = dateFormat.format(page.getLastModified().getTime());

		final String scheme = "https://";
		String publishLink = externalizer.publishLink(resourceResolver, StringUtils.EMPTY);
		URI uri = new URI(publishLink);
		String host = uri.getHost();

		String link = scheme + LanguageResourceResolver.map(host, payloadPath);
		Boolean isMagazineArticle = page.getTemplate().getName().equalsIgnoreCase("magazine-article");
		String ingress = isMagazineArticle ? Utils.setEmptyonNull(page.getDescription()) : Utils.getIngress(page);
		String newsType = isMagazineArticle ? "Magazine" : getNewsType(workflowData);
		String languageCode = page.getLanguage(true).getLanguage();

		Map<String, String> emailParameters = new HashMap<>();
		emailParameters.put("date", date);
		emailParameters.put("ingress", ingress);
		emailParameters.put("languageCode", languageCode);
		emailParameters.put("link", link);
		emailParameters.put("title", page.getTitle());
		emailParameters.put("newsType", newsType);

		return emailParameters;
	}

	private boolean pushMailActivated(String payloadPath) {
		Resource payloadResource = resourceResolver.getResource(payloadPath);
		if (payloadResource != null) {
			String pushMailValue = payloadResource.adaptTo(Page.class).getProperties().get(PUSH_MAILS, String.class);
			if (pushMailValue == null) {
				return false;
			}
		}
		return true;

	}
}

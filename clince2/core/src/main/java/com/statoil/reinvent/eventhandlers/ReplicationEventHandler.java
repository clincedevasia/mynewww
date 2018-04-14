package com.statoil.reinvent.eventhandlers;

import javax.jcr.Node;
import javax.jcr.Session;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.api.resource.ResourceResolver;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.day.cq.replication.ReplicationAction;
import com.day.cq.replication.ReplicationActionType;
import com.day.cq.replication.Replicator;
import com.statoil.reinvent.services.UserMapperService;

/**
 * Event handler for setting the `published` property automatically for pages
 * based on the news template. Another option was to use jcr:created, not
 * jcr:content/jcr:created, but all imported news use the `published` property
 * so we continue with this.
 */
@Component
@Service
@Property(name = "event.topics", value = ReplicationAction.EVENT_TOPIC)
public class ReplicationEventHandler implements EventHandler {

	@Reference
	private UserMapperService userMapperService;

	@Reference
	private Replicator replicator;
	private ResourceResolver resourceResolver;

	private static final String CQ_LAST_REPLICATED = "cq:lastReplicated";
	private static final String CQ_TEMPLATE = "cq:template";
	private static final String JCR_CONTENT = "jcr:content";
	private static final String NEWS_TEMPLATE = "/apps/statoil/templates/news";
	private static final String PUBLISHED = "published";

	private static final Logger logger = LoggerFactory.getLogger(ReplicationEventHandler.class);

	@Override
	public void handleEvent(Event event) {
		ReplicationAction action = ReplicationAction.fromEvent(event);

		if (action == null)
			return;

		logger.info("Replication action {} occured on {} ", action.getType().getName(), action.getPath());

		if (!action.getType().equals(ReplicationActionType.ACTIVATE)) {
			return;
		}

		try {
			resourceResolver = userMapperService.getJCRWriterService();
			Session session = resourceResolver.adaptTo(Session.class);
			Node node = session.getNode(action.getPath()).getNode(JCR_CONTENT);

			// Set `published` property for pages based on the news template if
			// not already set.
			if (node.getProperty(CQ_TEMPLATE).getString().equals(NEWS_TEMPLATE) && !node.hasProperty(PUBLISHED)
					&& node.hasProperty(CQ_LAST_REPLICATED)) {
				node.setProperty(PUBLISHED, node.getProperty(CQ_LAST_REPLICATED).getDate());
				session.save();

				// Replicate page again to publish `published` property.
				replicator.replicate(session, ReplicationActionType.ACTIVATE, node.getPath());
			}
		} catch (Exception e) {
			logger.error("Failed to handle event: " + e.getMessage());
		} finally {
			if (resourceResolver != null && resourceResolver.isLive()) {
				resourceResolver.close();
			}
		}
	}
}

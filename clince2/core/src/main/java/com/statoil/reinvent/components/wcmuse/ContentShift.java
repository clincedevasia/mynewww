package com.statoil.reinvent.components.wcmuse;

import javax.jcr.AccessDeniedException;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.lock.LockException;
import javax.jcr.nodetype.ConstraintViolationException;

import org.apache.sling.api.resource.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.day.cq.commons.jcr.JcrUtil;

/**
 * The Class ContentShift.
 */
public class ContentShift extends BaseComponent {

	/** The logger. */
	private static final Logger logger = LoggerFactory.getLogger(ContentShift.class);

	/** The src path. */
	private String srcPath = "";

	/** The dest path. */
	private String destPath = "";

	/** The live sync path. */
	private String liveSyncPath = "";

	/** The jcr_content. */
	private String jcr_content = "jcr:content";

	/** The page. */
	private String PAGE = "/content/ParentPageShiftConsole/jcr:content/par";

	/** The src path. */
	private String SRC_PATH = "srcPath";

	/** The dest path. */
	private String DEST_PATH = "destPath";

	/** The livesync path. */
	private String LIVESYNC_PATH = "liveSyncPath";

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.adobe.cq.sightly.WCMUse#activate()
	 */
	@Override
	protected void activated() throws Exception {
		srcPath = getProperties().get("srcPath", String.class);
		destPath = getProperties().get("destPath", String.class);
		liveSyncPath = getProperties().get("liveSyncPath", String.class);

		if (srcPath != null && destPath != null) {
			deleteDestCont(destPath, srcPath);
		}
		if (liveSyncPath != null) {
			deleteliveSyncPath(liveSyncPath);
		}

	}

	/**
	 * Delete live sync path.
	 *
	 * @param liveSyncPath
	 *            the live sync path Deleting the cq:LiveSyncConfig node from
	 *            the mentioned path
	 */
	public void deleteliveSyncPath(String liveSyncPath) {
		try {
			// Convert destPath into a node
			Resource destPath = getResourceResolver().getResource(liveSyncPath + "/jcr:content");
			logger.info("Live Sync Path " + destPath.getPath().toString());
			if (destPath != null) {

				Node destNode = destPath.adaptTo(Node.class);
				logger.info("Live Sync Node path " + destNode.getPath());
				if (destNode.hasNode("cq:LiveSyncConfig")) {
					logger.info("Live sync node has the node livesynconfig");
					destNode.getNode("cq:LiveSyncConfig").remove();
				}
				Session session = getRequest().getResourceResolver().adaptTo(Session.class);

				Resource pageRes = getResourceResolver().getResource(PAGE);
				Node pageNode = pageRes.adaptTo(Node.class);
				if (pageNode.hasProperty(LIVESYNC_PATH)) {
					pageNode.getProperty(LIVESYNC_PATH).setValue("");
				}

				// removing the saved values
				session.save();

			}
		} catch (LockException e) {
			System.err.println("LockException: " + e.getMessage());
		} catch (ConstraintViolationException e) {
			System.err.println("ConstraintViolationException " + e.getMessage());
		} catch (AccessDeniedException e) {
			System.err.println("AccessDeniedException " + e.getMessage());
		} catch (RepositoryException e) {
			System.err.println("RepositoryException " + e.getMessage());

		}

	}

	/**
	 * Gets the dest path.
	 *
	 * @return the dest path
	 */
	public String getDestPath() {
		return destPath;
	}

	/**
	 * Gets the src path.
	 *
	 * @return the src path
	 */
	public String getSrcPath() {
		return srcPath;
	}

	/**
	 * Gets the live sync path.
	 *
	 * @return the live sync path
	 */
	public String getLiveSyncPath() {
		return liveSyncPath;
	}

	/**
	 * Sets the live sync path.
	 *
	 * @param liveSyncPath
	 *            the new live sync path
	 */
	public void setLiveSyncPath(String liveSyncPath) {
		this.liveSyncPath = liveSyncPath;
	}

	/**
	 * Delete dest cont.
	 *
	 * @param deleteDestCont
	 *            the delete dest cont
	 * @param srcPath
	 *            the src path deleteDestCont method deletes the content from
	 *            destination node and replaces it with content from Source node
	 */
	public void deleteDestCont(String deleteDestCont, String srcPath) {
		try {
			// Convert destPath into a node
			Resource destRes = getResourceResolver().getResource(deleteDestCont + "/jcr:content");
			Resource destParent = getResourceResolver().getResource(deleteDestCont);
			Resource srcRes = getResourceResolver().getResource(srcPath + "/jcr:content");

			logger.info("destRes " + destRes.getPath().toString());
			logger.info("srcRes path " + srcRes.getPath());

			if (srcRes != null && destRes != null) {

				Node destNode = destRes.adaptTo(Node.class);
				Node destParentNode = destParent.adaptTo(Node.class);
				Node srcNode = srcRes.adaptTo(Node.class);
				logger.info("destNode path " + destNode.getPath());
				logger.info("destNode path " + destNode.getPath());
				destNode.remove();
				Session session = getRequest().getResourceResolver().adaptTo(Session.class);
				session.save();
				JcrUtil.copy(srcNode, destParentNode, jcr_content);
				session.save();

				// removing the saved values

				Resource pageRes = getResourceResolver().getResource(PAGE);
				Node pageNode = pageRes.adaptTo(Node.class);
				if (pageNode.hasProperty(SRC_PATH)) {
					pageNode.getProperty(SRC_PATH).setValue("");
				}
				if (pageNode.hasProperty(DEST_PATH)) {
					pageNode.getProperty(DEST_PATH).setValue("");
				}
				session.save();

			}
		} catch (LockException e) {
			System.err.println("LockException: " + e.getMessage());
		} catch (ConstraintViolationException e) {
			System.err.println("ConstraintViolationException " + e.getMessage());
		} catch (AccessDeniedException e) {
			System.err.println("AccessDeniedException " + e.getMessage());
		} catch (RepositoryException e) {
			System.err.println("RepositoryException " + e.getMessage());

		}

	}

}
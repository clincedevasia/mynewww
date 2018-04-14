package com.statoil.reinvent.components.wcmuse;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NewsArchiveDownloads extends BaseComponent {

	private final Logger LOG = LoggerFactory.getLogger(this.getClass());

	private boolean hasDownloads;

	private List<Download> downloads;

	@Override
	protected void activated() throws Exception {
		LOG.debug("Inside News archive downloads component");
		Node node = getResource().adaptTo(Node.class);
		hasDownloads = node.getParent().hasNode("downloadsInfo");
		downloads = new ArrayList<>();
		if (hasDownloads) {
			PropertyIterator propertyIterator = node.getParent().getNode("downloadsInfo").getProperties();
			while (propertyIterator.hasNext()) {
				Property nextProperty = propertyIterator.nextProperty();
				if (!(nextProperty.getName().startsWith("jcr")||(nextProperty.getName().startsWith("cq")))) {
					downloads.add(new Download(nextProperty.getName(), nextProperty.getString()));
				}
			}
		}
	}

	public boolean isHasDownloads() {
		return hasDownloads;
	}

	public List<Download> getDownloads() {
		return downloads;
	}

	public static class Download {

		private String fileName;
		private String description;
		private String extension;
		private String size;

		public Download(String fileName, String description) {
			this.fileName = "downloads/" + fileName;
			int lastDash = description.lastIndexOf("-");
			this.description = description.substring(0, lastDash - 1);
			String[] extArr = this.fileName.split("\\.");
			this.extension = extArr[extArr.length - 1];
			this.size = description.substring(lastDash + 2, description.length());
		}

		public String getFileName() {
			return fileName;
		}

		public String getSize() {
			return size;
		}

		public String getDescription() {
			return description;
		}

		public String getExtension() {
			return extension.toUpperCase();
		}
	}

}
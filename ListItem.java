package com.statoil.reinvent.components.list;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.day.cq.dam.api.Asset;
import com.day.cq.tagging.Tag;
import com.day.cq.wcm.api.Page;
import com.google.gson.JsonObject;
import com.statoil.reinvent.utils.ImageUtil;
import com.statoil.reinvent.utils.PageUtil;

/**
 * Wrapper for elements in a list
 */
public class ListItem {

	private static final Logger logger = LoggerFactory.getLogger(ListItem.class);

	private static final String THUMBNAIL_RENDITION = ImageUtil.RENDITION_LIST;

	private static final String ASSET_THUMBNAIL_EXT = "/_jcr_content/renditions/cq5dam.thumbnail.620.620.png.transform/list/image.png";

	private Page item;
	protected String itemName;
	private String itemSubtitle;
	private String itemDescription;
	private Date lastModifiedDate;
	private String image;
	private String path;
	private Calendar calendar;
	private Set<String> currentTags;
	private boolean external;
	protected static final DateFormat formatter = new SimpleDateFormat("MM/dd/yy HH:mm");
	private boolean panelPage;

	private String extension;
	private String filesize;

	public ListItem(JsonObject jsonObject) {
		this(jsonObject, null);
	}

	public ListItem(JsonObject jsonObject, ResourceResolver resourceResolver) {
		this(jsonObject.get("title").getAsString(), jsonObject.get("description").getAsString(),
				jsonObject.get("image").getAsString(), jsonObject.get("link").getAsString());
		if (resourceResolver != null) {
			this.image = getInternalImage(jsonObject.get("image").getAsString(), resourceResolver);
		}
	}

	private String getInternalImage(String image, ResourceResolver resourceResolver) {
		Resource resource = resourceResolver.getResource(image);
		if (resource != null) {
			return ImageUtil.getSrc(image, THUMBNAIL_RENDITION, false);
		} else {
			return image;
		}
	}

	public ListItem(Page page) throws RepositoryException {
		currentTags = new HashSet<>();
		item = page;
		itemName = page.getTitle();
		itemSubtitle = page.getTitle();
		itemDescription = page.getDescription();
		addTags(page);
		lastModifiedDate = getLastModifiedDate(page);
		path = page.getPath() + ".html";
		image = findBannerImage(page);
		panelPage = PageUtil.isPanelPage(page);
	}

	public ListItem(Page page, JsonObject jsonObject, ResourceResolver resourceResolver)
			throws RepositoryException {
		this(page);
		String title = jsonObject.get("title").getAsString();
		String image = jsonObject.get("image").getAsString();
		String description = jsonObject.get("description").getAsString();
		if (StringUtils.isNotBlank(title)) {
			this.itemName = title;
		}
		if (StringUtils.isNotBlank(image)) {
			this.image = getInternalImage(image, resourceResolver);
		}
		if (StringUtils.isNotBlank(description)) {
			this.itemDescription = description;
		}
	}

	public ListItem(String title, Asset asset, boolean image) {
		this(title, null, asset.getPath() + ASSET_THUMBNAIL_EXT, asset.getPath());
		filesize = FileUtils.byteCountToDisplaySize(asset.getOriginal().getSize());
		extension = FilenameUtils.getExtension(asset.getPath());
		if (!image) {
			this.image = null;
		}
	}

	public ListItem(String title, String description, String image, String url) {
		this.external = true;
		this.itemName = title;
		this.itemDescription = description;
		this.image = image;
		this.path = url;
		this.currentTags = new HashSet<>();
	}

	private void addTags(Page page) {
		for (Tag tag : page.getTags()) {
			currentTags.add(tag.getTitle(page.getLanguage(true)));
		}
	}

	private String findBannerImage(Page page) {
		String bannerImage = null;
		try {
			Node pageNode = page.getContentResource().adaptTo(Node.class);
			Node bannerNode = pageNode.getNode("topbanner/banner");
			Property imageProperty = bannerNode.getProperty("fileReference");
			if (imageProperty != null) {
				bannerImage = ImageUtil.getSrc(imageProperty.getString(), THUMBNAIL_RENDITION, false);
			}
		} catch (RepositoryException e) {
			logger.warn(String.format("Could not find banner image for %s", page.getPath()), e);
		}
		if (bannerImage == null || bannerImage.isEmpty()) {
			bannerImage = page.getProperties().get("bannerImage", String.class);
			return ImageUtil.getSrc(bannerImage, THUMBNAIL_RENDITION, false);
		} else {
			return bannerImage;
		}
	}

	private Date getLastModifiedDate(Page page) throws RepositoryException {
		Node contentNode = page.getContentResource().adaptTo(Node.class);

		if (contentNode.hasProperty("published")) {
			calendar = contentNode.getProperty("published").getDate();
			return calendar.getTime();
		} else if (contentNode.hasProperty("cq:lastModified")) {
			calendar = contentNode.getProperty("cq:lastModified").getDate();
			return calendar.getTime();
		} else if (contentNode.hasProperty("jcr:created")) {
			calendar = contentNode.getProperty("jcr:created").getDate();
			return calendar.getTime();
		}

		return null;
	}

	public Date getLastModifiedDate() {
		return lastModifiedDate;
	}

	public String getModifiedDateString() {
		Date lastModifiedDate = getLastModifiedDate();

		if (lastModifiedDate == null) {
			return null;
		}

		return formatter.format(lastModifiedDate);
	}

	public String getPath() {
		return path;
	}

	public String getImage() {
		return image;
	}

	public Page getItem() {
		return item;
	}

	public List<String> getCurrentTags() {
		return new ArrayList<>(currentTags);
	}

	public String getItemSubtitle() {
		return itemSubtitle;
	}

	public String getItemDescription() {
		return itemDescription;
	}

	public String getItemName() {
		return itemName;
	}

	public boolean isExternal() {
		return external;
	}

	public String getExtension() {
		return extension;
	}

	public String getFilesize() {
		return filesize;
	}

	public Calendar getCalendar() {
		return calendar;
	}

	public boolean isPanelPage() {
		return panelPage;
	}
}

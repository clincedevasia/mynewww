package com.statoil.reinvent.models;

import java.util.Date;

public class MagazineListViewModel {

	private LinkModel linkModel;
	private ImageModel imageModel;
	private Date lastModified;
	
	public LinkModel getLinkModel() {
		return linkModel;
	}
	public void setLinkModel(LinkModel linkModel) {
		this.linkModel = linkModel;
	}
	public ImageModel getImageModel() {
		return imageModel;
	}
	public void setImageModel(ImageModel imageModel) {
		this.imageModel = imageModel;
	}
	public Date getLastModified() {
		return lastModified;
	}
	public void setLastModified(Date lastModified) {
		this.lastModified = lastModified;
	}
	
	
}

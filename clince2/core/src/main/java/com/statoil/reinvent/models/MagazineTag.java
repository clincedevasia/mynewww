package com.statoil.reinvent.models;

public class MagazineTag {
	
	private String tagTitle;
	private String tagId;
	
	public MagazineTag(String tagTitle, String tagId) {
		super();
		this.tagTitle = tagTitle;
		this.tagId = tagId;
	}
	public String getTagTitle() {
		return tagTitle;
	}
	public void setTagTitle(String tagTitle) {
		this.tagTitle = tagTitle;
	}
	public String getTagId() {
		return tagId;
	}
	public void setTagId(String tagId) {
		this.tagId = tagId;
	}
}

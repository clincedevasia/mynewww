package com.statoil.reinvent.models;

import com.statoil.reinvent.components.sling.models.ImageRenditions;

public class CarouselProperties {

	private String pagePath;
	private String imageLink;
	private String slideTitle;
	private String slideDescription;
	private String buttonText;
	private String buttonColor;
	private String contentPlacement;
	private String textColor;
	private String shadowColor;
	private ImageRenditions imageRenditions;
	
	public String getPagePath() {
		return pagePath;
	}
	public void setPagePath(String pagePath) {
		this.pagePath = pagePath;
	}
	public String getImageLink() {
		return imageLink;
	}
	public void setImageLink(String imageLink) {
		this.imageLink = imageLink;
	}
	public String getSlideTitle() {
		return slideTitle;
	}
	public void setSlideTitle(String slideTitle) {
		this.slideTitle = slideTitle;
	}
	public String getSlideDescription() {
		return slideDescription;
	}
	public void setSlideDescription(String slideDescription) {
		this.slideDescription = slideDescription;
	}
	public String getButtonText() {
		return buttonText;
	}
	public void setButtonText(String buttonText) {
		this.buttonText = buttonText;
	}
	public String getButtonColor() {
		return buttonColor;
	}
	public void setButtonColor(String buttonColor) {
		this.buttonColor = buttonColor;
	}
	public String getContentPlacement() {
		return contentPlacement;
	}
	public void setContentPlacement(String contentPlacement) {
		this.contentPlacement = contentPlacement;
	}
	public String getTextColor() {
		return textColor;
	}
	public void setTextColor(String textColor) {
		this.textColor = textColor;
	}
	public String getShadowColor() {
		return shadowColor;
	}
	public void setShadowColor(String shadowColor) {
		this.shadowColor = shadowColor;
	}
	public ImageRenditions getImageRenditions() {
		return imageRenditions;
	}
	public void setImageRenditions(ImageRenditions imageRenditions) {
		this.imageRenditions = imageRenditions;
	}
	
}

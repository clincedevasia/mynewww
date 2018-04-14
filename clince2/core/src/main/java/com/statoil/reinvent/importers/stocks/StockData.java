package com.statoil.reinvent.importers.stocks;

import com.statoil.reinvent.utils.PropertiesUtil;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

public class StockData {
	private String ricCode;
	private String pre;
	private String post;
	private String sub;
	private String number;
	private String footerText;

	public StockData(String ricCode, String pre, String post, String sub, String number, String footerText) {
		this.ricCode = ricCode;
		this.pre = pre;
		this.post = post;
		this.sub = sub;
		this.number = number;
		this.footerText = footerText;
	}

	public StockData(Node node) throws RepositoryException {
		this(node.getName(),
				PropertiesUtil.getString(node, "pre"),
				PropertiesUtil.getString(node, "post"),
				PropertiesUtil.getString(node, "sub"),
				PropertiesUtil.getString(node, "number"),
				PropertiesUtil.getString(node, "footerText"));
	}

	public String getRicCode() {
		return ricCode;
	}
	public String getPre() {
		return pre;
	}
	public String getPost() {
		return post;
	}
	public void setPost(String post) {
		this.post = post;
	}
	public String getSub() {
		return sub;
	}
	public String getNumber() {
		return number;
	}
	public String getFooterText() {
		return footerText;
	}
}

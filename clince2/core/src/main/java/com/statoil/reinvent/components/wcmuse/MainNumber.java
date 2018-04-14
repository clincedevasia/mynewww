package com.statoil.reinvent.components.wcmuse;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.statoil.reinvent.editor.fields.CheckBox;

public class MainNumber extends BaseComponent {

	private static final Logger LOG = LoggerFactory.getLogger(MainNumber.class);
	private String id = "";
	private boolean countupCheckbox;
	private String countup = "";

	@Override
	protected void activated() throws Exception {
		LOG.debug("Inside mainnumbers component wcmuse class");
		Node node = getResource().adaptTo(Node.class);
		try {
			this.id = node.getName();
		} catch (RepositoryException e) {
			e.printStackTrace();
		}
		countupCheckbox = CheckBox.getValue(getProperties(), "countup");
		if (countupCheckbox) {
			countup = "countup ";
		}
	}

	public String getCountup() {
		return countup;
	}

	public String getId() {
		return id;
	}

}

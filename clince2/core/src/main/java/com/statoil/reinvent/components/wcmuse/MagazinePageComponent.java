package com.statoil.reinvent.components.wcmuse;

import java.util.ArrayList;
import java.util.List;

import com.day.cq.tagging.Tag;
import com.statoil.reinvent.utils.GenericUtil;

public class MagazinePageComponent extends BaseComponent {

	private List<Tag> tagList = new ArrayList<>();

	@Override
	protected void activated() throws Exception {
		tagList = GenericUtil.getAllTagsOfChildPages(currentPage);
	}

	public List<Tag> getTagList() {
		return tagList;
	}

}

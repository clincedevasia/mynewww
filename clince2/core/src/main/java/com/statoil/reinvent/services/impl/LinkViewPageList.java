package com.statoil.reinvent.services.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;

import com.statoil.reinvent.services.AbstractPageList;
import com.statoil.reinvent.utils.GenericUtil;


@Component
@Service(value=AbstractPageList.class)
public class LinkViewPageList extends AbstractPageList {

	@Override
	public List<?> display() {
		return super.pages.stream()
				.map(e-> GenericUtil.constructPageLinkModel(e))
				.collect(Collectors.toList());
	}

}

package com.statoil.reinvent.components.wcmuse;

import com.day.cq.wcm.api.Page;

/**
 * Created by kristoffer on 20/05/16.
 */
public class CookieAlert extends BaseComponent {

	private String message;

	@Override
	protected void activated() throws Exception {
		Page rootPage = getRootPage();
		message = rootPage.getProperties().get("cookieMessage", String.class);
	}
	
	public String getMessage() {
		return message;
	}

	private Page getRootPage() throws Exception {
		return getCurrentPage().getAbsoluteParent(2);

	}

}

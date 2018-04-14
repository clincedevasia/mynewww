package com.statoil.reinvent.components.wcmuse;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class Date extends BaseComponent {

	private Locale locale;
	private Calendar date;

	private List<String> classList = new ArrayList<String>();

	@Override
	protected void activated() throws Exception {
		locale = currentPage.getLanguage(true);
		classList.add("date-module");
		this.date = currentPage.getProperties().get("published", Calendar.class);
	}

	public String getDate() {
		if (date != null) {
			return getFormattedDate(date);
		}
		return null;
	}

	private String getFormattedDate(Calendar cal) {
		DateFormat formatter = DateFormat.getDateInstance(DateFormat.LONG, locale);
		String formattedValue = formatter.format(cal.getTime());
		return formattedValue;
	}

	public List<String> getClassList() {
		return classList;
	}

}
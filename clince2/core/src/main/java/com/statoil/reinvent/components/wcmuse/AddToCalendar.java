package com.statoil.reinvent.components.wcmuse;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AddToCalendar extends BaseComponent {

	private static final Logger LOG = LoggerFactory.getLogger(AddToCalendar.class);
	private static final String DATE_TIME_FORMAT = "yyyyMMdd'T'HHmmss";

	private Locale locale;

	private String text;
	private String margin;
	private String title;
	private String location;
	private Date startDateTime;
	private Date endDateTime;

	@Override
	protected void activated() throws Exception {
		LOG.debug("Inside addtocalendar component wcmuse class");
		locale = currentPage.getLanguage(true);
		// Button.
		text = properties.get("text", String.class);
		margin = properties.get("spaceBelow", String.class);
		// Event.
		title = properties.get("title", String.class);
		location = properties.get("location", String.class);
		String startString = properties.get("start", String.class);
		startDateTime = convertToDate(startString);
		String endString = properties.get("end", String.class);
		endDateTime = convertToDate(endString);

	}

	private Date convertToDate(String dateTimeString) throws ParseException {
		try {
			DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
			return dateFormat.parse(dateTimeString);
		} catch (ParseException e) {
			return null;
		}
	}

	public String getText() {
		if (StringUtils.isBlank(text)) {
			return "Add to Calendar";
		}

		return text;
	}

	public String getMargin() {
		if (StringUtils.isBlank(margin)) {
			return StringUtils.EMPTY;
		}

		return margin;
	}

	public String getTitle() {

		return title;
	}

	public String getLocation() {

		return location;

	}

	public String getStart() {
		if (startDateTime == null) {
			return null;
		}

		return new SimpleDateFormat(DATE_TIME_FORMAT, locale).format(startDateTime);
	}

	public String getEnd() {
		if (endDateTime == null) {
			return null;
		}

		return new SimpleDateFormat(DATE_TIME_FORMAT, locale).format(endDateTime);
	}

	public String getUrl() {
		String Url = null;

		try {
			Url = "/services/ics" + "?summary=" + URLEncoder.encode(getTitle(), "UTF-8") + "&description="
					+ "&location=" + URLEncoder.encode(getLocation(), "UTF-8") + "&timeZone=" + "&start=" + getStart()
					+ "&end=" + getEnd();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			LOG.error("Exception: ", e);
		}

		return Url;

	}

}

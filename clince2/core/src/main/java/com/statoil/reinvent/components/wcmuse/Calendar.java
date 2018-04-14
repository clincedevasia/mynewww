package com.statoil.reinvent.components.wcmuse;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.SortedSet;
import java.util.TimeZone;
import java.util.TreeSet;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.day.cq.tagging.Tag;
import com.day.cq.wcm.api.Page;
import com.google.gson.JsonObject;
import com.statoil.reinvent.editor.fields.Multifield;

public class Calendar extends BaseComponent {

	private static final Logger LOG = LoggerFactory.getLogger(Calendar.class);
	private static final String DATE_TIME_FORMAT = "yyyyMMdd'T'HHmmss";

	private List<CalendarEvent> events = new ArrayList<>();
	private String filter;
	private Locale locale;
	private String search;
	private String spaceBelow;
	private String title;
	private String viewMode;

	@Override
	protected void activated() throws Exception {
		LOG.debug("Inside calendar component wcmuse class");
		locale = currentPage.getLanguage(true);
		events = buildEvents(locale);
		filter = properties.get("filter", String.class);
		search = properties.get("search", String.class);
		spaceBelow = properties.get("spaceBelow", String.class);
		title = properties.get("title", String.class);
		viewMode = properties.get("viewMode", String.class);
	}

	public Set<String> getCountries() {
		if (events.isEmpty()) {
			return null;
		}

		SortedSet<String> countries = new TreeSet<>();

		for (CalendarEvent event : events) {
			String location = event.getLocation();
			String country = location.substring(location.lastIndexOf(",") + 1).trim();
			countries.add(country);
		}

		return countries;
	}

	public List<CalendarEvent> getEvents() {
		return events;
	}

	public String getFilter() {
		if (StringUtils.isBlank(filter)) {
			return "enabled"; // Default.
		}

		return filter;
	}

	public Locale getLocale() {
		return locale;
	}

	public String getSearch() {
		if (StringUtils.isBlank(search)) {
			return "enabled"; // Default.
		}

		return search;
	}

	public String getSpaceBelow() {
		return spaceBelow;
	}

	public Set<String> getTags() {
		if (events.isEmpty()) {
			return null;
		}

		SortedSet<String> tags = new TreeSet<>();

		for (CalendarEvent event : events) {
			for (String tag : event.getTags()) {
				tags.add(tag);
			}
		}

		return tags;
	}

	public String getTitle() {

		return title;
	}

	public String getViewMode() {
		if (StringUtils.isBlank(viewMode)) {
			return "list-view"; // Default.
		}

		return viewMode;
	}

	private List<JsonObject> getJsonObjects(String relativePath) {
		return Multifield.getObjects(getProperties(), relativePath);
	}

	private String getJsonObjectString(JsonObject jsonObject, String property) {
		return jsonObject.has(property) ? jsonObject.get(property).getAsString() : StringUtils.EMPTY;
	}

	private Date convertToDate(String dateTimeString) throws ParseException {
		try {
			DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
			return dateFormat.parse(dateTimeString);
		} catch (ParseException e) {
			return null;
		}
	}

	private List<CalendarEvent> buildEvents(Locale locale) throws ParseException {
		List<CalendarEvent> events = new ArrayList<>();
		List<JsonObject> jsonObjects = getJsonObjects("events");

		for (JsonObject jsonObject : jsonObjects) {
			String title = getJsonObjectString(jsonObject, "title");
			String location = getJsonObjectString(jsonObject, "location");
			String readMore = getJsonObjectString(jsonObject, "readMore");
			String startDateTimeString = getJsonObjectString(jsonObject, "start");
			Date startDateTime = convertToDate(startDateTimeString);
			String endDateTimeString = getJsonObjectString(jsonObject, "end");
			Date endDateTime = convertToDate(endDateTimeString);
			String UTCValue = getJsonObjectString(jsonObject, "timeZone");

			events.add(new CalendarEvent(title, readMore, location, startDateTime, endDateTime, locale, UTCValue));
		}

		return events;
	}

	public class CalendarEvent {

		private static final String EMPTY_TEXT = "empty";
		private java.util.Calendar startCalendar = java.util.Calendar.getInstance();
		private java.util.Calendar endCalendar = java.util.Calendar.getInstance();
		private Page page;
		private boolean hasReadMore = false;
		private final String EUROPE_OSLO = "Europe/Oslo";

		public CalendarEvent(String title, String readMore, String location, Date startDateTime, Date endDateTime,
				Locale locale, String UTCValue) {
			this.title = title;
			this.readMore = readMore;
			this.location = location;
			this.startDateTime = startDateTime;
			startCalendar.setTime(startDateTime);
			this.endDateTime = endDateTime;
			endCalendar.setTime(endDateTime);
			this.timeZone = getTimeZone(EUROPE_OSLO, startDateTime);
			this.locale = locale;
			this.UTCValue = UTCValue;

			if (StringUtils.isNotBlank(readMore)) {
				page = getPageManager().getPage(readMore);

				if (page != null) {
					hasReadMore = true;
				}
			}
		}

		public List<String> getTags() {
			if (!hasReadMore) {
				return new ArrayList<>();
			}

			ArrayList<String> tags = new ArrayList<>();

			for (Tag tag : page.getTags()) {
				tags.add(tag.getTitle(locale));
			}

			return tags;
		}

		private String title;

		public String getTitle() {

			return title;

		}

		private String readMore;

		public String getReadMore() {
			if (hasReadMore) {
				return readMore;
			}

			return null;
		}

		private String location;

		public String getLocation() {

			return location;
		}

		private Locale locale;
		private Date startDateTime;
		private Date endDateTime;
		private String timeZone;
		private String UTCValue;

		public int getStartDay() {
			return startCalendar.get(java.util.Calendar.DAY_OF_MONTH);
		}

		public String getStartMonth() {
			return new SimpleDateFormat("MMMM", locale).format(startDateTime);
		}

		public String getStartTime() {
			SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm");
			return simpleDateFormat.format(startCalendar.getTime());
		}

		public String getEndTime() {
			SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm");
			return simpleDateFormat.format(endCalendar.getTime());
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

		private String getTimeZone(String ID, Date date) {
			return TimeZone.getTimeZone(ID).inDaylightTime(date) ? "CEST" : "CET";
		}

		public String getTimeZone() {
			return StringUtils.isNotBlank(UTCValue) && !UTCValue.equalsIgnoreCase(EMPTY_TEXT) ? UTCValue : timeZone;
		}

		public String getUrl() {

			String Url = null;

			try {
				Url = "/services/ics" + "?summary=" + URLEncoder.encode(getTitle(), "UTF-8") + "&description="
						+ "&location=" + URLEncoder.encode(getLocation(), "UTF-8") + "&timeZone=" + "&start="
						+ getStart() + "&end=" + getEnd();
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				LOG.error("Exception: ", e);
			}

			return Url;
		}
	}

}

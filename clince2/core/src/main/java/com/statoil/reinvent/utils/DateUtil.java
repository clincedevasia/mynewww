package com.statoil.reinvent.utils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import javax.script.Bindings;

import org.apache.sling.scripting.sightly.pojo.Use;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.day.cq.wcm.api.Page;

/**
 * Created by kristoffer on 04/08/15.
 */
public class DateUtil implements Use {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	private Calendar calendar;
	private Locale locale;

	public DateUtil() {

	}

	@Override
	public void init(Bindings bindings) {

		Page page = (Page) bindings.get("page");
		locale = page.getLanguage(true);

		// parameters are passed as bindings
		String dateString = (String) bindings.get("calendar");
		// month + "/" + day + "/" + year + " " + hourOfDay + ":" + minutes;

		DateFormat formatter = new SimpleDateFormat("MM/dd/yy HH:mm");
		Date date = null;
		try {
			date = formatter.parse(dateString);
		} catch (ParseException e) {
			logger.debug("Exception when formatting date", e);
		}
		calendar = Calendar.getInstance(locale);
		calendar.setTime(date);
	}

	public String getFormattedDate() {
		return this.getFormattedDate(calendar, locale);
	}

	public static String getFormattedDate(Calendar cal, Locale locale) {
		DateFormat formatter = DateFormat.getDateInstance(DateFormat.LONG, locale);
		String formattedValue = formatter.format(cal.getTime());
		return formattedValue;
	}

}

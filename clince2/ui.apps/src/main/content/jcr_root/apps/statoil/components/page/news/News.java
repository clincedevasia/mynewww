package apps.statoil.components.page.news;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import org.apache.sling.api.resource.ValueMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adobe.cq.sightly.WCMUsePojo;
import com.day.cq.wcm.api.Page;

public class News extends WCMUsePojo {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final int ONE_MINUTE = 60 * 1000;
    private final String TIME_ZONE_OSLO_EUROPE = "Europe/Oslo";

    private String created;
    private String createdTimeZone;

    private String lastModified;
    private String lastModifiedTimeZone;

    public String getCreated() {
        return created;
     }
    public String getCreatedTimeZone() {
         return createdTimeZone;
     }

    public String getLastModified() {
        return lastModified;
     }
    public String getLastModifiedTimeZone() {
         return lastModifiedTimeZone;
     }

    private Locale locale;

    @Override
    public void activate() throws Exception {
        Page page = getCurrentPage();
        locale = page.getLanguage(true);

        DateFormat dateFormatter = DateFormat.getDateInstance(DateFormat.LONG, locale);
        // Locale.GERMANY as we should always use a 24H clock and Locale.NORWAY does not exist.
        DateFormat timeFormatter = DateFormat.getTimeInstance(DateFormat.SHORT, Locale.GERMANY);

        ValueMap valueMap = page.getProperties();

        // `published` is set by {@link com.statoil.reinvent.eventhandlers.ReplicationEventHandler}.
        Date createdDate = valueMap.get("published", Date.class);
        Calendar createdCalendar = Calendar.getInstance();
        createdCalendar.setTime(createdDate);

        // When activating (publishing) a node (page), a new version of the node is created, so jcr:content/jcr:created
        // can be used as the last modified timestamp.
        Date lastModifiedDate = valueMap.get("jcr:created", Date.class);

        Calendar lastModifiedCalendar = Calendar.getInstance();
        lastModifiedCalendar.setTime(lastModifiedDate);

        // When `published` was specified manually (before introducing ReplicationEventHandler), it did not have a time
        // set, so we check whether there is a time to display.
        if (isTimeSpecified(createdCalendar)) {
            created = dateFormatter.format(createdCalendar.getTime()) + " " + timeFormatter.format(createdCalendar.getTime());
            createdTimeZone = getTimeZone(TIME_ZONE_OSLO_EUROPE, createdDate);

            // Check if more than a minutes difference between created and last modified as there is no reason to
            // display last modified if the same time as created.
            if (lastModifiedDate.getTime() - createdDate.getTime() >= ONE_MINUTE) {
                lastModified = dateFormatter.format(lastModifiedCalendar.getTime()) + " " + timeFormatter.format(lastModifiedCalendar.getTime());
                lastModifiedTimeZone = getTimeZone(TIME_ZONE_OSLO_EUROPE, lastModifiedDate);
            }
        } else {
            created = dateFormatter.format(createdCalendar.getTime());

            // If no time is specified for created, we only display last modified if on another day as published
            // and initial created would always be on the same day.
            if (!isSameDay(createdCalendar, lastModifiedCalendar)) {
                lastModified = dateFormatter.format(lastModifiedCalendar.getTime()) + " " + timeFormatter.format(lastModifiedCalendar.getTime());
                lastModifiedTimeZone = getTimeZone(TIME_ZONE_OSLO_EUROPE, lastModifiedDate);
            }
        }
    }

    /**
     * Returns <code>true</code> if a time, i.e. hh:mm:ss.sss, is specified for the date.
     *
     * Previously the <code>published</code> property was specified manually with a date picker, which specified the
     * time as 00:00:00.000.
     *
     * @return <code>true</code> if a time, i.e. hh:mm:ss.sss, is specified for the date.
     *         <code>false</code> otherwise
     */
    private boolean isTimeSpecified(Calendar calendar) {
        return !(calendar.get(Calendar.HOUR) == 0 && calendar.get(Calendar.MINUTE) == 0 && calendar.get(calendar.SECOND) == 0 && calendar.get(Calendar.MILLISECOND) == 0);
    }

    /**
     * Returns <code>true</code> if the calendars' time is on the same day.
     *
     * @return <code>true</code> if the calendars' time is on the same day.
     *         <code>false</code> otherwise
     */
    private boolean isSameDay(Calendar calendar1, Calendar calendar2) {
        return calendar1.get(Calendar.YEAR) == calendar2.get(Calendar.YEAR) &&
               calendar1.get(Calendar.DAY_OF_YEAR) == calendar2.get(Calendar.DAY_OF_YEAR);
    }

    /**
     * Returns <code>CEST</code> if the time zone is in daylight saving time.
     *
     * @return <code>CEST</code> if the time zone is in daylight saving time.
     *         <code>CET</code> otherwise
     */
    private String getTimeZone(String ID, Date date) {
        return TimeZone.getTimeZone(ID).inDaylightTime(date) ? "CEST" : "CET";
    }

    public String getLanguage() {
        return locale.getLanguage();
    }
}
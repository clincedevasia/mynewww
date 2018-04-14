package com.statoil.reinvent.components.wcmuse;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import org.apache.sling.api.resource.ValueMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NewsArchivePageComponent extends BaseComponent {
    private final static Logger LOG = LoggerFactory.getLogger(NewsArchivePageComponent.class);

    private String created;
    private String createdTimeZone;


	@Override
	protected void activated() throws Exception {
		LOG.debug("Inside News Wcm page component");
        SimpleDateFormat outputFormatter = new SimpleDateFormat("MMMM d, yyyy, HH:mm", currentPage.getLanguage(true));
        outputFormatter.setTimeZone(TimeZone.getTimeZone("CET"));
        ValueMap valueMap = currentPage.getProperties();

        // `published` is set either manually or by `Add Published Property` workflow.
        Date createdDate = valueMap.get("published", Date.class);
        Calendar createdCalendar = Calendar.getInstance();
        createdCalendar.setTime(createdDate);
        created = outputFormatter.format(createdCalendar.getTime());
        createdTimeZone = getTimeZone("Europe/Oslo", createdDate);
	}
	
    // Unsuccessful in getting TimeZone to play nicely with CEST, so wrote this little helper.
    private String getTimeZone(String ID, Date date) {
        return TimeZone.getTimeZone(ID).inDaylightTime(date) ? "CEST" : "CET";
    }
    
    public String getCreated() {
        return created;
    }
    public String getCreatedTimeZone() {
        return createdTimeZone;
    }

}

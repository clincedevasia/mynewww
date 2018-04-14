package com.statoil.reinvent.rss;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.apache.commons.lang3.StringUtils;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class RemitJsonParser {

    private RssFeed buildRssFeed() {
        RssFeed rssFeed = new RssFeed(
                "REMIT Statoil.com",
                "https://www.statoil.com/en/what-we-do/remit.html",
                "Short description of the website.",
                StringUtils.EMPTY,
                StringUtils.EMPTY,
                StringUtils.EMPTY);

        return rssFeed;
    }

    private String buildDateString(String timeOfDayLocal, String dateLocal, String time) throws ParseException {
        if (StringUtils.isBlank(time)) {
            return StringUtils.EMPTY;
        }

        String seconds = time.substring(time.length() - 3, time.length() - 1);
        timeOfDayLocal = timeOfDayLocal + ":" + seconds;

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd.MM.yyyy HH:m:ss");
        Date date = simpleDateFormat.parse(dateLocal + " " + timeOfDayLocal);
        simpleDateFormat.applyPattern("M/dd/yyyy h:mm:ss a");
        String dateString = simpleDateFormat.format(date);

        return dateString;
    }

    private String buildDescription(String publishedDate, String category, String updated, String publishedBy, String eventStartDateString, String expectedEventEndDateString, String actualEventEndDateString, String volumeImpact, String comments) {

        String description =
                "<p><q>PUBLISHED(CET):&nbsp;&nbsp;" + publishedDate + "</q></p>\n" +
                " <p><b>" + category + "&nbsp;&nbsp;" + updated + "&nbsp;&nbsp; " + publishedBy + "</b></p>\n" +
                " <p><b>EVENT START(CET):&nbsp;&nbsp;</b>" + eventStartDateString + "\n" +
                " <b>&nbsp;&nbsp;EXPECTED END(CET):&nbsp;&nbsp;</b>" + expectedEventEndDateString + "\n" +
                " <b>&nbsp;&nbsp;EVENT END(CET):&nbsp;&nbsp;</b>" + actualEventEndDateString + "</p>\n" +
                " <p><b>VOLUME IMPACT(MSm3/day):&nbsp;&nbsp;</b>" + volumeImpact + "</p>\n" +
                " <p><b>COMMENTS:&nbsp;&nbsp;</b>" + comments + "</p>";

        return description;
    }

    private String buildPubDate(String publishedDate) throws ParseException {
        if (StringUtils.isBlank(publishedDate)) {
            return StringUtils.EMPTY;
        }

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("M/dd/yyyy h:mm:ss a", Locale.US);
        Date date = simpleDateFormat.parse(publishedDate);
        simpleDateFormat.applyPattern("EEE, dd MMM YYYY HH:mm:ss");
        String dateString = simpleDateFormat.format(date) + " GMT";

        return dateString;
    }

    private RssItem buildRssItem(JsonObject message) throws ParseException {
        String title = message.get("ID").getAsString();
        String link = "https://www.statoil.com/en/what-we-do/remit.html";
        String category = message.get("IsPlannedEvent").getAsBoolean() ? "PLANNED EVENT" : "UNPLANNED EVENT";
        String updated = message.get("Version").getAsInt() > 1 ? " UPDATED AND " : StringUtils.EMPTY;

        String publishedDateString = buildDateString(
                message.get("MessagePublishedTimeOfDayLocal").getAsString(),
                message.get("MessagePublishedDateLocal").getAsString(),
                message.get("MessagePublishedTime").getAsString());

        String publishedBy = message.get("IsPublishedByGassco").getAsBoolean() ? "PUBLISHED BY GASSCO" : StringUtils.EMPTY;

        String eventStartDateString = buildDateString(
                message.get("EventStartTimeOfDayLocal").getAsString(),
                message.get("EventStartDateLocal").getAsString(),
                message.get("EventStartTime").getAsString());

        String expectedEventEndDateString = buildDateString(
                message.get("ExpectedEventEndTimeOfDayLocal").getAsString(),
                message.get("ExpectedEventEndDateLocal").getAsString(),
                message.get("ExpectedEventEndTime").getAsString());

        String actualEventEndDateString = buildDateString(
                message.get("ActualEventEndTimeOfDayLocal").getAsString(),
                message.get("ActualEventEndDateLocal").getAsString(),
                message.get("ActualEventEndTime").getAsString());

        String volumeImpact = message.get("Volume").getAsString();

        String comments = message.get("Comments").getAsString();

        String description = buildDescription(
                publishedDateString,
                category,
                updated,
                publishedBy,
                eventStartDateString,
                expectedEventEndDateString,
                actualEventEndDateString,
                volumeImpact,
                comments);

        String pubDate = buildPubDate(publishedDateString);

        RssItem rssItem = new RssItem(
                title,
                link,
                category,
                description,
                pubDate
        );

        return rssItem;
    }

    public RssFeed parse(String json) throws ParseException {
        RssFeed rssFeed = buildRssFeed();
        
        
        JsonObject jsonObject = new Gson().fromJson(json, JsonObject.class);
        JsonArray list = jsonObject.getAsJsonObject("Data").getAsJsonArray("List");
        
        
        
        for (int listIndex = 0; listIndex < list.size(); listIndex++) {
            JsonObject listItem  = list.get(listIndex).getAsJsonObject();

            JsonObject currentMessage = listItem.getAsJsonObject("CurrentMessage");

            RssItem currentMessageRssItem = buildRssItem(currentMessage);
            rssFeed.addItem(currentMessageRssItem);

            JsonArray messageHistory = listItem.getAsJsonArray("MessageHistory");

            for (int messageHistoryIndex = 0; messageHistoryIndex < messageHistory.size(); messageHistoryIndex++) {
                JsonObject messageHistoryItem = messageHistory.get(messageHistoryIndex).getAsJsonObject();

                RssItem messageHistoryRssItem = buildRssItem(messageHistoryItem);
                rssFeed.addItem(messageHistoryRssItem);
            }
        }

        return rssFeed;
    }
}

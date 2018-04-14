package com.statoil.reinvent.rss;

public class RssItem {

    String title;
    String link;
    String category;
    String description;
    String pubDate;

    public RssItem(String title, String link, String category, String description, String pubDate) {
        this.title = title;
        this.link = link;
        this.category = category;
        this.description = description;
        this.pubDate = pubDate;
    }

    public String getTitle() {
        return title;
    }

    public String getLink() {
        return link;
    }

    public String getCategory() {
        return category;
    }

    public String getDescription() {
        return description;
    }

    public String getPubDate() {
        return pubDate;
    }

    @Override
    public String toString() {
        return "RssItem [title=" + title + ", link=" + link + ", category=" + category + ", description=" + description + ", pubDate=" + pubDate + "]";
    }

}

package com.statoil.reinvent.components.wcmuse;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.day.cq.dam.api.Asset;
import com.google.gson.JsonObject;
import com.statoil.reinvent.components.list.DownloadListItem;
import com.statoil.reinvent.components.list.ListItem;
import com.statoil.reinvent.editor.fields.CheckBox;
import com.statoil.reinvent.editor.fields.Multifield;

public class DownloadList extends BaseComponent {

    private static final Logger LOG = LoggerFactory.getLogger(DownloadList.class);

    private String title;

    private boolean showImages;

    private List<ListItem> list;

    private Locale language;

    private String space;
    
    @Override
	protected void activated() throws Exception {
    	LOG.debug("Inside downloadlist component wcmuse class");
        this.title = properties.get("jcr:moduleTitle", String.class);
        this.showImages = CheckBox.getValue(getProperties(), "showImages");
        this.language = getCurrentPage().getLanguage(true);
        this.list = createDownloadsList();
        space = getProperties().get("space", String.class);
	}
    
    private List<ListItem> createDownloadsList(){
        List<JsonObject> pages = Multifield.getObjects(getProperties(), "downloads");
        List<ListItem> listItems = new ArrayList<>();
        for (JsonObject pageJson : pages) {
            listItems.add(createDownloadListItem(pageJson, showImages));
        }
        List<ListItem> listItems1 = pages.stream().map( e-> createDownloadListItem(e, showImages )).collect(Collectors.toList());;
        return listItems;
    }

    private ListItem createDownloadListItem(JsonObject jsonObject, boolean image)  {
        Asset asset = getResourceResolver().getResource(jsonObject.get("file").getAsString()).adaptTo(Asset.class);
        String title = null;
        if (jsonObject.has("title")) {
            title = jsonObject.get("title").getAsString();
        }
        if (StringUtils.isBlank(title)) {
            title = asset.getMetadataValue("dc:title");
        }
        return new DownloadListItem(title, asset, image);
    }

    public String getSpace(){
        return space;
    }

    public String getLanguage() {
        return language.getLanguage();
    }

    public List<ListItem> getList() {
        return list;
    }

    public boolean isEmpty(){
        return list == null || list.isEmpty();
    }

    public String getTitle() {
        return title;
    }

	
}
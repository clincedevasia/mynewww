package com.statoil.reinvent.components.wcmuse;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.apache.commons.lang.StringUtils;
import org.apache.sling.api.resource.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.day.cq.wcm.api.components.IncludeOptions;
import com.google.gson.JsonObject;
import com.statoil.reinvent.components.table.TableItem;
import com.statoil.reinvent.editor.fields.Multifield;


public class TableRow extends BaseComponent {

    private static final Logger LOG = LoggerFactory.getLogger(TableRow.class);

    private List<TableItem> itemListing;
    private List<String> headers;

    @Override
	protected void activated() throws Exception {
    	LOG.debug("Inside tablerow component wcmuse class");
    	itemListing = new ArrayList<>();
        List<JsonObject> jsonObjects = Multifield.getObjects(getProperties(), "tabitems");
        for(JsonObject jsonObject : jsonObjects) {
            String path = null;
            String title = null;
            if (jsonObject.has("tabValue")) {
                path = jsonObject.get("tabValue").getAsString();
            }
            if (jsonObject.has("title")) {
                title = jsonObject.get("title").getAsString();
            }
            TableItem tableItem = createTableItem(title, path);
            if (tableItem != null) {
                itemListing.add(tableItem);
            }
        }

        // Exclude wrapper when not in edit mode. Prevents breaking of table
        if(!getWcmMode().isEdit()) {
            IncludeOptions opts = IncludeOptions.getOptions(getRequest(), true);
            opts.setDecorationTagName("");
        }
        headers = getHeadings();
		
	}

    private List<String> getHeadings() throws RepositoryException {
        Node node = getResource().adaptTo(Node.class);
        Node tableNode = node.getParent().getParent();
        List<JsonObject> headings = Multifield.getObjects(tableNode, "columnheaders");
        List<String> headingsList = new ArrayList<>();
        for (JsonObject jsonObject : headings) {
            headingsList.add(jsonObject.get("title").getAsString());
        }
        return headingsList;
    }

    private TableItem createTableItem(String title, String path) {
        if (StringUtils.isNotBlank(title) && StringUtils.isNotBlank(path)) {
            if (path.startsWith("http")) {
                return new TableItem(title, path);
            } else {
                Resource resource = getResourceResolver().getResource(path);
                return new TableItem(title, resource);
            }
        } else if(StringUtils.isNotBlank(path)) {
            Resource resource = getResourceResolver().getResource(path);
            if (resource != null) {
                return new TableItem(resource);
            } else {
                return new TableItem(path);
            }
        }
        return null;
    }

    public List<String> getHeaders() {
        return headers;
    }

    public List<TableItem> getItemListing(){
        return itemListing;
    }

}